package app.accrescent.client.util

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import app.accrescent.client.R
import app.accrescent.client.data.Apk
import app.accrescent.client.data.REPOSITORY_URL
import app.accrescent.client.data.RepoDataRepository
import app.accrescent.client.data.net.AppRepoData
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InvalidObjectException
import java.net.URL
import java.security.GeneralSecurityException
import java.security.MessageDigest
import javax.inject.Inject

private const val TAG = "ApkDownloader"

class ApkDownloader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repoDataRepository: RepoDataRepository,
) {
    suspend fun downloadApp(appId: String): List<Apk> {
        Log.i(TAG, "Downloading app $appId")
        val appInfo = repoDataRepository.getAppRepoData(appId)

        val version = appInfo.versionCode
        val minVersion = repoDataRepository.getAppMinVersionCode(appId)
        if (version < minVersion) {
            val msg = context.getString(R.string.version_downgrade, version, minVersion)
            throw GeneralSecurityException(msg)
        }

        val apkNames = resolveApkNames(appInfo)
        val apks = downloadApks("$REPOSITORY_URL/apps/$appId/$version", apkNames)
        val baseApk = apks[0].file

        verifyPackageInfo(appId, appInfo, baseApk)

        // Verify app signers
        val requiredSigners = repoDataRepository.getAppSigners(appId)
        if (requiredSigners.isEmpty()) {
            throw IllegalStateException(context.getString(R.string.no_app_signers, requiredSigners))
        } else if (!verifySigners(baseApk, requiredSigners)) {
            val msg = context.getString(R.string.app_signer_mismatch, requiredSigners)
            throw GeneralSecurityException(msg)
        }

        return apks
    }

    private fun resolveApkNames(appInfo: AppRepoData): List<String> {
        val apkNames = mutableListOf("base.apk")

        // Resolve ABI split
        if (appInfo.abiSplits.isNotEmpty()) {
            var abiSupported = false
            for (abi in Build.SUPPORTED_ABIS) {
                if (appInfo.abiSplits.contains(abi)) {
                    Log.d(TAG, "Preferred ABI: $abi")
                    apkNames += "split.$abi.apk"
                    abiSupported = true
                    break
                }
            }
            if (!abiSupported) {
                throw NoSuchElementException(context.getString(R.string.device_abi_unsupported))
            }
        }

        // Resolve density split
        if (appInfo.densitySplits.isNotEmpty()) {
            val screenDensity = context.resources.displayMetrics.densityDpi
            val densityClass = when {
                screenDensity <= DisplayMetrics.DENSITY_LOW -> "ldpi"
                screenDensity <= DisplayMetrics.DENSITY_MEDIUM -> "mdpi"
                screenDensity <= DisplayMetrics.DENSITY_TV -> "tvdpi"
                screenDensity <= DisplayMetrics.DENSITY_HIGH -> "hdpi"
                screenDensity <= DisplayMetrics.DENSITY_XHIGH -> "xhdpi"
                screenDensity <= DisplayMetrics.DENSITY_XXHIGH -> "xxhdpi"
                else -> "xxxhdpi"
            }
            if (appInfo.densitySplits.contains(densityClass)) {
                Log.d(TAG, "Preferred screen density: $densityClass")
                apkNames += "split.$densityClass.apk"
            } else {
                throw NoSuchElementException(context.getString(R.string.device_density_unsupported))
            }
        }

        // Opportunistically resolve language split
        if (appInfo.langSplits.isNotEmpty()) {
            val deviceLang = Resources.getSystem().configuration.locales[0].language
            if (appInfo.langSplits.contains(deviceLang)) {
                Log.d(TAG, "Preferred language: $deviceLang")
                apkNames += "split.$deviceLang.apk"
            } else {
                Log.d(TAG, "Preferred language APK not available, using default app language")
            }
        }

        return apkNames
    }

    private fun verifyPackageInfo(appId: String, expected: AppRepoData, apk: TemporaryFile) {
        val packageInfo = context
            .packageManager
            .getPackageArchiveInfoForFd(apk.getFd(), 0)
            ?: throw InvalidObjectException(context.getString(R.string.base_apk_not_valid))

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
        if (packageInfo.versionName != expected.version) {
            throw GeneralSecurityException(
                context.getString(
                    R.string.version_mismatch,
                    expected.version,
                    packageInfo.versionName
                )
            )
        }
    }

    private fun verifySigners(apk: TemporaryFile, requiredSigners: List<String>): Boolean {
        @Suppress("DEPRECATION")
        val flags = PackageManager.GET_SIGNING_CERTIFICATES or PackageManager.GET_SIGNATURES
        val signingInfo = context
            .packageManager
            .getPackageArchiveInfoForFd(apk.getFd(), flags)
            ?.signingInfo
            ?: return false

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
}

private fun downloadApks(baseDownloadUri: String, names: List<String>): List<Apk> {
    val apks = mutableListOf<Apk>()
    for (name in names) {
        val apk = newTemporaryFile()
        URL("$baseDownloadUri/$name")
            .openHttpConnection()
            .use { it.downloadTo(apk.descriptor) }
        apks += Apk(name, apk)
    }

    return apks
}

private fun signatureToCertHash(signature: Signature): String {
    return MessageDigest
        .getInstance("SHA-256")
        .digest(signature.toByteArray())
        .joinToString("") { "%02x".format(it) }
}