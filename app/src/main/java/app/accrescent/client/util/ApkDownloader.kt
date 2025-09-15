package app.accrescent.client.util

import android.app.ActivityManager
import android.app.sdksandbox.SdkSandboxManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.content.pm.SigningInfo
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.GLES20
import android.opengl.GLUtils
import android.os.Build
import android.os.ext.SdkExtensions
import android.util.Log
import app.accrescent.client.R
import app.accrescent.client.data.Apk
import app.accrescent.client.data.DownloadProgress
import app.accrescent.client.data.RepoDataRepository
import build.buf.gen.accrescent.directory.v1.AppDownloadInfo
import build.buf.gen.accrescent.directory.v1.CompatibilityLevel
import build.buf.gen.accrescent.directory.v1.DeviceAttributes
import build.buf.gen.accrescent.directory.v1.DirectoryServiceGrpcKt
import build.buf.gen.accrescent.directory.v1.PackageInfo
import build.buf.gen.accrescent.directory.v1.deviceAttributes
import build.buf.gen.accrescent.directory.v1.getAppDownloadInfoRequest
import build.buf.gen.android.bundle.deviceSpec
import build.buf.gen.android.bundle.sdkRuntime
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import java.io.InvalidObjectException
import java.net.URL
import java.security.GeneralSecurityException
import java.security.MessageDigest
import java.util.Locale
import android.content.pm.PackageInfo as AndroidPackageInfo

private const val TAG = "ApkDownloader"

private const val EGL_ERROR_CONFIG_NOT_POPULATED = "EGL config was not populated"
private const val EGL_ERROR_CREATE_CONTEXT = "failed to create a new EGL rendering context"
private const val EGL_ERROR_NO_DISPLAY_AVAILIABLE = "no display connection is available"

class ApkDownloader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val directoryService: DirectoryServiceGrpcKt.DirectoryServiceCoroutineStub,
    private val repoDataRepository: RepoDataRepository,
) {
    suspend fun downloadApp(
        appId: String,
        onProgressUpdate: (DownloadProgress) -> Unit = {},
    ): List<Apk> {
        Log.i(TAG, "Downloading app $appId")
        val currentVersionCode = try {
            context.packageManager.getPackageInfo(appId, 0).longVersionCode
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
        val request = getAppDownloadInfoRequest {
            this.appId = appId
            deviceAttributes = getDeviceAttributes()
            if (currentVersionCode != null) {
                baseVersionCode = currentVersionCode
            }
        }
        val response = directoryService.getAppDownloadInfo(request)

        if (response.compatibility.level != CompatibilityLevel.COMPATIBILITY_LEVEL_COMPATIBLE) {
            throw NoSuchElementException(context.getString(R.string.app_incompatible_with_device))
        } else if (!response.hasAppDownloadInfo()) {
            throw NoSuchElementException(context.getString(R.string.app_incompatible_with_device))
        }

        val version = response.appDownloadInfo.packageInfo.versionCode
        val minVersion = repoDataRepository.getAppMinVersionCode(appId)
        if (version < minVersion) {
            val msg = context.getString(R.string.version_downgrade, version, minVersion)
            throw GeneralSecurityException(msg)
        }

        val apks = downloadApks(response.appDownloadInfo, onProgressUpdate)

        verifyPackageInfo(appId, response.appDownloadInfo.packageInfo, apks.map { it.file })

        // Verify app signers
        val requiredSigners = repoDataRepository.getAppSigners(appId)
        if (requiredSigners.isEmpty()) {
            throw IllegalStateException(context.getString(R.string.no_app_signers, requiredSigners))
        } else if (!verifySigners(apks.map { it.file }, requiredSigners)) {
            val msg = context.getString(R.string.app_signer_mismatch, requiredSigners)
            throw GeneralSecurityException(msg)
        }

        return apks
    }

    private fun verifyPackageInfo(appId: String, expected: PackageInfo, apks: List<TemporaryFile>) {
        var packageInfo: AndroidPackageInfo? = null

        for (apk in apks) {
            packageInfo = context
                .packageManager
                .getPackageArchiveInfoForFd(apk.getFd(), 0)
            if (packageInfo != null) {
                break
            }
        }
        if (packageInfo == null) {
            throw InvalidObjectException(context.getString(R.string.base_apk_not_valid))
        }

        if (packageInfo.packageName != appId) {
            val msg = context.getString(R.string.app_id_mismatch, packageInfo.packageName, appId)
            throw GeneralSecurityException(msg)
        }
        if (packageInfo.longVersionCode != expected.versionCode) {
            val msg = context.getString(
                R.string.version_code_mismatch,
                expected.versionCode,
                packageInfo.longVersionCode,
            )
            throw GeneralSecurityException(msg)
        }
        if (packageInfo.versionName != expected.versionName) {
            throw GeneralSecurityException(
                context.getString(
                    R.string.version_mismatch,
                    expected.versionName,
                    packageInfo.versionName
                )
            )
        }
    }

    private fun verifySigners(apks: List<TemporaryFile>, requiredSigners: List<String>): Boolean {
        @Suppress("DEPRECATION")
        val flags = PackageManager.GET_SIGNING_CERTIFICATES or PackageManager.GET_SIGNATURES
        var signingInfo: SigningInfo? = null
        for (apk in apks) {
            signingInfo = context
                .packageManager
                .getPackageArchiveInfoForFd(apk.getFd(), flags)
                ?.signingInfo
            if (signingInfo != null) {
                break
            }
        }
        if (signingInfo == null) {
            return false
        }

        if (signingInfo.hasMultipleSigners()) {
            val signers = signingInfo.apkContentsSigners.map { signatureToCertHash(it) }

            Log.d(TAG, "Required app signers:")
            for (requiredSigner in requiredSigners) {
                Log.d(TAG, requiredSigner)
                if (!signers.contains(requiredSigner)) {
                    return false
                }
            }

            return true
        } else {
            Log.d(TAG, "Required app signer: ${requiredSigners[0]}")

            return signingInfo
                .signingCertificateHistory
                .map { signatureToCertHash(it) }
                .contains(requiredSigners[0])
        }
    }

    private fun getDeviceAttributes(): DeviceAttributes {
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
        val reqGlEsVersionHexFormat = HexFormat {
            upperCase = false
            number {
                prefix = "0x"
                removeLeadingZeros = true
            }
        }

        return context.packageManager.systemAvailableFeatures.map { feature ->
            if (feature.name == null) {
                "reqGlEsVersion=${feature.reqGlEsVersion.toHexString(reqGlEsVersionHexFormat)}"
            } else if (feature.version == 0) {
                feature.name
            } else {
                "${feature.name}=${feature.version}"
            }
        }
    }

    private fun getGlExtensions(): Result<List<String>> {
        val display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (display == EGL14.EGL_NO_DISPLAY) {
            Log.e(TAG, EGL_ERROR_NO_DISPLAY_AVAILIABLE)

            return Result.failure(IllegalStateException(EGL_ERROR_NO_DISPLAY_AVAILIABLE))
        }

        val initialized = EGL14.eglInitialize(display, null, 0, null, 0)
        if (!initialized) {
            val eglErrorString = GLUtils.getEGLErrorString(EGL14.eglGetError())
            val errorString = "could not initialize an EGL display connection: $eglErrorString"
            Log.e(TAG, errorString)

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
            Log.e(TAG, errorString)

            return Result.failure(IllegalStateException(errorString))
        }
        val config = configList[0] ?: run {
            Log.e(TAG, EGL_ERROR_CONFIG_NOT_POPULATED)

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
            Log.e(TAG, EGL_ERROR_CREATE_CONTEXT)

            return Result.failure(IllegalStateException(EGL_ERROR_CREATE_CONTEXT))
        }

        val dummySurface = EGL14.eglCreatePbufferSurface(display, config, null, 0)
        if (dummySurface == EGL14.EGL_NO_SURFACE) {
            val eglErrorString = GLUtils.getEGLErrorString(EGL14.eglGetError())
            val errorString = "failed to create an EGL pixel buffer surface: $eglErrorString"
            Log.e(TAG, errorString)

            return Result.failure(IllegalStateException(errorString))
        }
        val makeCurrentResult = EGL14.eglMakeCurrent(display, dummySurface, dummySurface, eglContext)
        if (!makeCurrentResult) {
            val eglErrorString = GLUtils.getEGLErrorString(EGL14.eglGetError())
            val errorString = "failed to attach an EGL rendering context to surfaces: $eglErrorString"
            Log.e(TAG, errorString)

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
            Log.w(TAG, "failed to release EGL context: $eglErrorString")
        }
        val destroySurfaceResult = EGL14.eglDestroySurface(display, dummySurface)
        if (!destroySurfaceResult) {
            val eglErrorString = GLUtils.getEGLErrorString(EGL14.eglGetError())
            Log.w(TAG, "failed to destroy EGL surface: $eglErrorString")
        }
        val destroyContextResult = EGL14.eglDestroyContext(display, eglContext)
        if (!destroyContextResult) {
            val eglErrorString = GLUtils.getEGLErrorString(EGL14.eglGetError())
            Log.w(TAG, "failed to destroy EGL rendering context: $eglErrorString")
        }
        val terminateResult = EGL14.eglTerminate(display)
        if (!terminateResult) {
            val eglErrorString = GLUtils.getEGLErrorString(EGL14.eglGetError())
            Log.w(TAG, "failed to terminate EGL display connection: $eglErrorString")
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

private fun downloadApks(
    downloadInfo: AppDownloadInfo,
    onProgressUpdate: (DownloadProgress) -> Unit = {},
): List<Apk> {
    val totalBytesToDownload = downloadInfo.splitDownloadInfoList.sumOf { it.downloadSize.toLong() }
    var totalBytesDownloaded = 0L

    val urls = downloadInfo.splitDownloadInfoList.map { it.url }

    val apks = mutableListOf<Apk>()
    val connections = mutableListOf<HttpConnection>()
    for (url in urls) {
        val conn = URL(url).openHttpConnection()
        connections += conn
    }

    for ((url, conn) in urls.zip(connections)) {
        val apk = newTemporaryFile()
        conn.use {
            it.downloadTo(apk.descriptor) { bytes ->
                totalBytesDownloaded += bytes
                onProgressUpdate(DownloadProgress(totalBytesDownloaded, totalBytesToDownload))
            }
        }
        apks += Apk(url.toByteArray().toHexString(), apk)
    }

    return apks
}

private fun signatureToCertHash(signature: Signature): String {
    return MessageDigest
        .getInstance("SHA-256")
        .digest(signature.toByteArray())
        .joinToString("") { "%02x".format(it) }
}