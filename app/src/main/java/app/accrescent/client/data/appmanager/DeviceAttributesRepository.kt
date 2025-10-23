package app.accrescent.client.data.appmanager

import android.app.ActivityManager
import android.app.sdksandbox.SdkSandboxManager
import android.content.Context
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.GLES20
import android.opengl.GLUtils
import android.os.Build
import android.os.ext.SdkExtensions
import android.util.Log
import build.buf.gen.accrescent.appstore.v1.DeviceAttributes
import build.buf.gen.accrescent.appstore.v1.deviceAttributes
import build.buf.gen.android.bundle.deviceSpec
import build.buf.gen.android.bundle.sdkRuntime
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import java.util.Locale

private const val EGL_ERROR_CONFIG_NOT_POPULATED = "EGL config was not populated"
private const val EGL_ERROR_CREATE_CONTEXT = "failed to create a new EGL rendering context"
private const val EGL_ERROR_NO_DISPLAY_AVAILABLE = "no display connection is available"
private const val LOG_TAG = "DeviceAttributesRepository"

class DeviceAttributesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private companion object {
        private val REQ_GL_ES_VERSION_HEX_FORMAT = HexFormat {
            upperCase = false
            number {
                prefix = "0x"
                removeLeadingZeros = true
            }
        }
    }

    fun getDeviceAttributes(): DeviceAttributes {
        return deviceAttributes {
            spec = deviceSpec {
                supportedAbis.addAll(Build.SUPPORTED_ABIS.asIterable())
                supportedLocales.addAll(Locale.getAvailableLocales().map { it.toLanguageTag() })
                deviceFeatures.addAll(getDeviceFeatures())
                // If an error occurs retrieving the device's supported OpenGL extension strings, we
                // have no better choice than to assume the device supports none by defaulting to an
                // empty list.
                glExtensions.addAll(getGlExtensions().getOrDefault(emptyList()))
                screenDensity = context.resources.displayMetrics.densityDpi
                sdkVersion = Build.VERSION.SDK_INT
                codename = Build.VERSION.CODENAME
                sdkRuntime = sdkRuntime {
                    supported = isSdkRuntimeSupported()
                }
                ramBytes = getDeviceRamBytes()
                buildBrand = Build.BRAND
                buildDevice = Build.DEVICE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    socManufacturer = Build.SOC_MANUFACTURER
                    socModel = Build.SOC_MODEL
                }
            }
        }
    }

    private fun getDeviceFeatures(): List<String> {
        return context.packageManager.systemAvailableFeatures.map { feature ->
            if (feature.name == null) {
                "reqGlEsVersion=${feature.reqGlEsVersion.toHexString(REQ_GL_ES_VERSION_HEX_FORMAT)}"
            } else {
                if (feature.version == 0) {
                    feature.name
                } else {
                    "${feature.name}=${feature.version}"
                }
            }
        }
    }

    private fun getGlExtensions(): Result<List<String>> {
        val display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (display == EGL14.EGL_NO_DISPLAY) {
            Log.e(LOG_TAG, EGL_ERROR_NO_DISPLAY_AVAILABLE)

            return Result.failure(IllegalStateException(EGL_ERROR_NO_DISPLAY_AVAILABLE))
        }

        val initialized = EGL14.eglInitialize(display, null, 0, null, 0)
        if (!initialized) {
            val eglErrorString = GLUtils.getEGLErrorString(EGL14.eglGetError())
            val errorString = "could not initialize an EGL display connection: $eglErrorString"
            Log.e(LOG_TAG, errorString)

            return Result.failure(IllegalStateException(errorString))
        }

        val configAttributeList = intArrayOf(EGL14.EGL_NONE)
        val configList = arrayOfNulls<EGLConfig>(1)
        val configCount = IntArray(1)
        val chooseConfigResult = EGL14.eglChooseConfig(
            display,
            configAttributeList,
            0,
            configList,
            0,
            1,
            configCount,
            0,
        )
        if (!chooseConfigResult) {
            val eglErrorString = GLUtils.getEGLErrorString(EGL14.eglGetError())
            val errorString = "failed to choose an EGL configuration: $eglErrorString"
            Log.e(LOG_TAG, errorString)

            return Result.failure(IllegalStateException(errorString))
        }
        val config = configList[0] ?: run {
            Log.e(LOG_TAG, EGL_ERROR_CONFIG_NOT_POPULATED)

            return Result.failure(IllegalStateException(EGL_ERROR_CONFIG_NOT_POPULATED))
        }

        // Request OpenGL ES 2.0 to support retrieving OpenGL ES extensions on as many devices as
        // possible. See https://developer.android.com/about/dashboards#OpenGL.
        val contextAttributeList = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION,
            2,
            EGL14.EGL_NONE,
        )
        val eglContext = EGL14
            .eglCreateContext(display, config, EGL14.EGL_NO_CONTEXT, contextAttributeList, 0)
        if (eglContext == EGL14.EGL_NO_CONTEXT) {
            Log.e(LOG_TAG, EGL_ERROR_CREATE_CONTEXT)

            return Result.failure(IllegalStateException(EGL_ERROR_CREATE_CONTEXT))
        }

        val dummySurface = EGL14.eglCreatePbufferSurface(display, config, null, 0)
        if (dummySurface == EGL14.EGL_NO_SURFACE) {
            val eglErrorString = GLUtils.getEGLErrorString(EGL14.eglGetError())
            val errorString = "failed to create an EGL pixel buffer surface: $eglErrorString"
            Log.e(LOG_TAG, errorString)

            return Result.failure(IllegalStateException(errorString))
        }
        val makeCurrentResult = EGL14.eglMakeCurrent(display, dummySurface, dummySurface, eglContext)
        if (!makeCurrentResult) {
            val eglErrorString = GLUtils.getEGLErrorString(EGL14.eglGetError())
            val errorString = "failed to attach an EGL rendering context to surfaces: $eglErrorString"
            Log.e(LOG_TAG, errorString)

            return Result.failure(IllegalArgumentException(errorString))
        }

        // Retrieve the device's supported OpenGL extensions
        val glExtensions = GLES20.glGetString(GLES20.GL_EXTENSIONS).trim().split(' ')

        // Clean up
        val releaseContextResult = EGL14.eglMakeCurrent(
            display,
            EGL14.EGL_NO_SURFACE,
            EGL14.EGL_NO_SURFACE,
            EGL14.EGL_NO_CONTEXT,
        )
        if (!releaseContextResult) {
            val eglErrorString = GLUtils.getEGLErrorString(EGL14.eglGetError())
            Log.w(LOG_TAG, "failed to release EGL context: $eglErrorString")
        }
        val destroySurfaceResult = EGL14.eglDestroySurface(display, dummySurface)
        if (!destroySurfaceResult) {
            val eglErrorString = GLUtils.getEGLErrorString(EGL14.eglGetError())
            Log.w(LOG_TAG, "failed to destroy EGL surface: $eglErrorString")
        }
        val destroyContextResult = EGL14.eglDestroyContext(display, eglContext)
        if (!destroyContextResult) {
            val eglErrorString = GLUtils.getEGLErrorString(EGL14.eglGetError())
            Log.w(LOG_TAG, "failed to destroy EGL rendering context: $eglErrorString")
        }
        val terminateResult = EGL14.eglTerminate(display)
        if (!terminateResult) {
            val eglErrorString = GLUtils.getEGLErrorString(EGL14.eglGetError())
            Log.w(LOG_TAG, "failed to terminate EGL display connection: $eglErrorString")
        }

        return Result.success(glExtensions)
    }

    private fun isSdkRuntimeSupported(): Boolean {
        return if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ||
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            SdkExtensions.getExtensionVersion(SdkExtensions.AD_SERVICES) >= 3
        ) {
            SdkSandboxManager.getSdkSandboxState() ==
                    SdkSandboxManager.SDK_SANDBOX_STATE_ENABLED_PROCESS_ISOLATION
        } else {
            false
        }
    }

    private fun getDeviceRamBytes(): Long {
        val activityManager = context.getSystemService(ActivityManager::class.java)
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        return memoryInfo.totalMem
    }
}
