package app.accrescent.client.util

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import app.accrescent.client.R
import app.accrescent.client.data.DownloadProgress
import app.accrescent.client.data.REPOSITORY_URL
import app.accrescent.client.data.RepoDataRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InvalidObjectException
import java.net.URL
import java.security.GeneralSecurityException
import java.security.MessageDigest
import javax.inject.Inject
import javax.net.ssl.HttpsURLConnection

private const val TAG = "ApkDownloader"

class ApkDownloader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repoDataRepository: RepoDataRepository,
) {
    suspend fun downloadApp(
        appId: String,
        onProgressUpdate: (DownloadProgress) -> Unit
    ): List<File> {
        Log.i(TAG, "Downloading app $appId")
        val appInfo = repoDataRepository.getAppRepoData(appId)

        val version = appInfo.versionCode
        val minVersion = repoDataRepository.getAppMinVersionCode(appId)
        if (version < minVersion) {
            val msg = context.getString(R.string.version_downgrade, version, minVersion)
            throw GeneralSecurityException(msg)
        }

        val downloadDir = File("${context.cacheDir.absolutePath}/apps/$appId/$version")
        val baseDownloadUri = "$REPOSITORY_URL/apps/$appId/$version"
        downloadDir.mkdirs()

        var abiSplit: String? = null
        var densitySplit: String? = null
        var langSplit: String? = null

        // Get ABI split name if applicable
        if (appInfo.abiSplits.isNotEmpty()) {
            for (abi in Build.SUPPORTED_ABIS) {
                if (appInfo.abiSplits.contains(abi)) {
                    Log.d(TAG, "Preferred ABI: $abi")
                    abiSplit = "split.$abi.apk"
                    break
                }
            }
            if (abiSplit == null) {
                throw NoSuchElementException(context.getString(R.string.device_abi_unsupported))
            }
        }

        // Get density split name if applicable
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
                densitySplit = "split.$densityClass.apk"
            } else {
                throw NoSuchElementException(context.getString(R.string.device_density_unsupported))
            }
        }

        // Opportunistically get device language split name at time of install if applicable
        if (appInfo.langSplits.isNotEmpty()) {
            val deviceLang = Resources.getSystem().configuration.locales[0].language
            if (appInfo.langSplits.contains(deviceLang)) {
                Log.d(TAG, "Preferred language: $deviceLang")
                langSplit = "split.$deviceLang.apk"
            } else {
                Log.d(TAG, "Preferred language APK not available, using default app language")
            }
        }

        // Download APKs
        val apkFileNames = mutableListOf<String>()
        apkFileNames.add("base.apk")
        abiSplit?.let { apkFileNames.add(it) }
        densitySplit?.let { apkFileNames.add(it) }
        langSplit?.let { apkFileNames.add(it) }

        val apks = mutableListOf<File>()
        val downloadProgress = DownloadProgress(0, 0)

        coroutineScope {
            withContext(Dispatchers.IO) {
                apkFileNames.forEach {
                    launch {
                        val file = File(downloadDir.absolutePath, it)
                        downloadToFile(
                            "$baseDownloadUri/$it",
                            file,
                            onProgressUpdate,
                            downloadProgress,
                        )
                        // Ensure the base APK is the first element. The rest of this method and
                        // PackageManager.installApp() both expect this.
                        if (it == "base.apk") {
                            apks.add(0, file)
                        } else {
                            apks.add(file)
                        }
                    }
                }
            }
        }

        // Verify package info is as expected
        val packageInfo = context
            .packageManager
            .getPackageArchiveInfoCompat(apks[0].absolutePath, 0)
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

        // Verify signers
        val requiredSigners = repoDataRepository.getAppSigners(appId)
        if (requiredSigners.isEmpty()) {
            throw IllegalStateException(context.getString(R.string.no_app_signers, requiredSigners))
        } else if (!verifySigners(apks[0], requiredSigners)) {
            val msg = context.getString(R.string.app_signer_mismatch, requiredSigners)
            throw GeneralSecurityException(msg)
        }

        return apks
    }

    private suspend fun downloadToFile(
        uri: String,
        file: File,
        onProgressUpdate: (DownloadProgress) -> Unit,
        totalDownloadProgress: DownloadProgress,
    ) {
        return withContext(Dispatchers.IO) {
            val connection = URL(uri).openConnection() as HttpsURLConnection
            val size = connection.getHeaderField("Content-length").toLongOrNull() ?: 0
            totalDownloadProgress.total += size

            connection.connect()

            val data = connection.inputStream
            val buf = ByteArray(DEFAULT_BUFFER_SIZE)
            val outFile = FileOutputStream(file, false)

            var totalBytesRead: Long = 0
            var bytes = data.read(buf)
            while (bytes >= 0) {
                outFile.write(buf, 0, bytes)
                bytes = data.read(buf)
                totalBytesRead += bytes

                totalDownloadProgress.part += bytes
                onProgressUpdate(totalDownloadProgress)
            }

            outFile.close()
            connection.disconnect()
        }
    }

    private fun verifySigners(apk: File, requiredSigners: List<String>): Boolean {
        @Suppress("DEPRECATION")
        val flags = PackageManager.GET_SIGNING_CERTIFICATES or PackageManager.GET_SIGNATURES
        val signingInfo = context
            .packageManager
            .getPackageArchiveInfoCompat(apk.absolutePath, flags)
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
