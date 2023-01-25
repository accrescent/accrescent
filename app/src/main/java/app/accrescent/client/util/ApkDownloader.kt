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

        val baseDownloadUri = "$REPOSITORY_URL/apps/$appId/$version"

        val baseApk = newTemporaryFile()
        URL("$baseDownloadUri/base.apk").openHttpConnection()
            .use { it.downloadTo(baseApk.descriptor) }

        val packageInfo = context
            .packageManager
            .getPackageArchiveInfoForFd(baseApk.getFd(), 0)
            ?: throw InvalidObjectException(context.getString(R.string.base_apk_not_valid))
        val packageName = packageInfo.packageName
        if (packageName != appId) {
            val msg = context.getString(R.string.app_id_mismatch, packageName, appId)
            throw GeneralSecurityException(msg)
        }
        val packageVersion = packageInfo.longVersionCode
        if (packageVersion != version) {
            val msg = context.getString(R.string.version_code_mismatch, version, packageVersion)
            throw GeneralSecurityException(msg)
        }
        if (packageInfo.versionName != appInfo.version) {
            throw GeneralSecurityException(
                context.getString(
                    R.string.version_mismatch,
                    appInfo.version,
                    packageInfo.versionName
                )
            )
        }

        val requiredSigners = repoDataRepository.getAppSigners(appId)
        if (requiredSigners.isEmpty()) {
            throw IllegalStateException(context.getString(R.string.no_app_signers, requiredSigners))
        } else if (!verifySigners(baseApk, requiredSigners)) {
            val msg = context.getString(R.string.app_signer_mismatch, requiredSigners)
            throw GeneralSecurityException(msg)
        }

        val apks = mutableListOf<Apk>()
        apks += Apk("base.apk", baseApk)

        if (appInfo.abiSplits.isNotEmpty()) {
            var abiSupported = false
            for (abi in Build.SUPPORTED_ABIS) {
                if (appInfo.abiSplits.contains(abi)) {
                    Log.d(TAG, "Preferred ABI: $abi")
                    val abiSplit = newTemporaryFile()
                    val splitName = "split.$abi.apk"
                    URL("$baseDownloadUri/$splitName").openHttpConnection()
                        .use { it.downloadTo(abiSplit.descriptor) }
                    apks += Apk(splitName, abiSplit)
                    abiSupported = true
                    break
                }
            }
            if (!abiSupported) {
                throw NoSuchElementException(context.getString(R.string.device_abi_unsupported))
            }
        }

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
                val densitySplit = newTemporaryFile()
                val splitName = "split.$densityClass.apk"
                URL("$baseDownloadUri/$splitName").openHttpConnection()
                    .use { it.downloadTo(densitySplit.descriptor) }
                apks += Apk(splitName, densitySplit)
            } else {
                throw NoSuchElementException(context.getString(R.string.device_density_unsupported))
            }
        }

        // Opportunistically install the split for the device language at time of install
        if (appInfo.langSplits.isNotEmpty()) {
            val deviceLang = Resources.getSystem().configuration.locales[0].language
            if (appInfo.langSplits.contains(deviceLang)) {
                Log.d(TAG, "Preferred language: $deviceLang")
                val langSplit = newTemporaryFile()
                val splitName = "split.$deviceLang.apk"
                URL("$baseDownloadUri/$splitName").openHttpConnection()
                    .use { it.downloadTo(langSplit.descriptor) }
                apks += Apk(splitName, langSplit)
            } else {
                Log.d(TAG, "Preferred language APK not available, using default app language")
            }
        }

        return apks
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

    private fun signatureToCertHash(signature: Signature): String {
        return MessageDigest
            .getInstance("SHA-256")
            .digest(signature.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}
