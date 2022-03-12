package app.accrescent.client.util

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import app.accrescent.client.data.RepoDataRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InvalidObjectException
import java.net.URL
import java.security.GeneralSecurityException
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import javax.inject.Inject
import javax.net.ssl.HttpsURLConnection

private const val TAG = "ApkDownloader"

class ApkDownloader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repoDataRepository: RepoDataRepository,
) {
    suspend fun downloadApp(appId: String): List<File> {
        Log.i(TAG, "Downloading app $appId")
        val appInfo = repoDataRepository.getAppRepoData(appId)
        val downloadDir =
            File("${context.cacheDir.absolutePath}/apps/$appId/${appInfo.versionCode}")
        val baseDownloadUri = "$REPOSITORY_URL/apps/$appId/${appInfo.versionCode}"
        downloadDir.mkdirs()

        val baseApk = File(downloadDir.absolutePath, "base.apk")
        downloadToFile("$baseDownloadUri/base.apk", baseApk)

        val packageName = context
            .packageManager
            .getPackageArchiveInfo(baseApk.absolutePath, 0)
            ?.packageName
            ?: throw InvalidObjectException("base.apk is not a valid APK")
        if (packageName != appId) {
            throw InvalidObjectException("app ID $packageName does not match expected value $appId")
        }

        val requiredSigners = repoDataRepository.getAppSigners(appId)
        if (requiredSigners.isEmpty()) {
            throw IllegalStateException("no app signers found")
        } else if (!verifySigners(baseApk, requiredSigners)) {
            throw GeneralSecurityException("app not signed by required signer(s)")
        }

        val apks = mutableListOf<File>()
        apks += baseApk

        if (appInfo.abiSplits.isNotEmpty()) {
            var abiSupported = false
            for (abi in Build.SUPPORTED_ABIS) {
                if (appInfo.abiSplits.contains(abi)) {
                    Log.d(TAG, "Preferred ABI: $abi")
                    val abiSplit = File(downloadDir.absolutePath, "split.$abi.apk")
                    downloadToFile("$baseDownloadUri/split.$abi.apk", abiSplit)
                    apks += abiSplit
                    abiSupported = true
                    break
                }
            }
            if (!abiSupported) {
                throw NoSuchElementException("your device's ABIs are not supported")
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
                val densitySplit = File(downloadDir.absolutePath, "split.$densityClass.apk")
                downloadToFile("$baseDownloadUri/split.$densityClass.apk", densitySplit)
                apks += densitySplit
            } else {
                throw NoSuchElementException("your device's screen density is not supported")
            }
        }

        // Play Store normally handles installing the correct language split when the user changes
        // their device language. Since we don't have that level of integration, we only attempt to
        // install the split for the device language at time of install.
        if (appInfo.langSplits.isNotEmpty()) {
            val deviceLang = Resources.getSystem().configuration.locales[0].language
            if (appInfo.langSplits.contains(deviceLang)) {
                Log.d(TAG, "Preferred language: $deviceLang")
                val langSplit = File(downloadDir.absolutePath, "split.$deviceLang.apk")
                downloadToFile("$baseDownloadUri/split.$deviceLang.apk", langSplit)
                apks += langSplit
            } else {
                throw NoSuchElementException("your device's language is not supported")
            }
        }

        return apks
    }

    private fun downloadToFile(uri: String, file: File) {
        val connection = URL(uri).openConnection() as HttpsURLConnection

        connection.connect()

        val data = connection.inputStream
        val buf = ByteArray(DEFAULT_BUFFER_SIZE)
        val outFile = FileOutputStream(file, false)

        var bytes = data.read(buf)
        while (bytes >= 0) {
            outFile.write(buf, 0, bytes)
            bytes = data.read(buf)
        }

        outFile.close()
        connection.disconnect()
    }

    private fun verifySigners(apk: File, requiredSigners: List<String>): Boolean {
        val signingInfo = context
            .packageManager
            .getPackageArchiveInfo(apk.absolutePath, PackageManager.GET_SIGNING_CERTIFICATES)
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
        val rawCert = ByteArrayInputStream(signature.toByteArray())
        val subjectPublicKeyInfo = CertificateFactory
            .getInstance("X.509")
            .generateCertificate(rawCert)
            .publicKey
            .encoded

        return MessageDigest
            .getInstance("SHA-256")
            .digest(subjectPublicKeyInfo)
            .joinToString("") { "%02x".format(it) }
    }

    companion object {
        const val REPOSITORY_URL = "https://store.accrescent.app"
    }
}
