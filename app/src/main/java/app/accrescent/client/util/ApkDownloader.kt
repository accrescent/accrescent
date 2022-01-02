package app.accrescent.client.util

import android.content.Context
import app.accrescent.client.data.RepoDataRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.security.GeneralSecurityException
import java.security.MessageDigest
import javax.inject.Inject
import javax.net.ssl.HttpsURLConnection

class ApkDownloader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repoDataRepository: RepoDataRepository
) {
    suspend fun downloadApp(appId: String): List<File> {
        val maintainer = repoDataRepository.getAppMaintainer(appId)!!
        val version = repoDataRepository.getAppVersion(appId) ?: withContext(Dispatchers.IO) {
            repoDataRepository.fetchSubRepoData(maintainer.username)
            repoDataRepository.getAppVersion(appId)!!
        }
        val packages = repoDataRepository.getPackagesForApp(appId)
        val downloadDir = File("${context.cacheDir.absolutePath}/packages/$appId/$version")
        downloadDir.mkdirs()

        val apks = emptyList<File>().toMutableList()
        for (appPackage in packages) {
            val downloadFile = File(downloadDir.absolutePath, appPackage.file)
            apks += downloadFile
            val downloadUri =
                "${REPOSITORY_URL}/${maintainer.username}/$appId/$version/${appPackage.file}"

            downloadToFile(URL(downloadUri), downloadFile)

            if (!verifyHash(downloadFile, appPackage.hash)) {
                throw GeneralSecurityException("package hash didn't match expected value")
            }
        }

        return apks
    }

    private fun downloadToFile(url: URL, file: File) {
        val connection = url.openConnection() as HttpsURLConnection

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

    private fun verifyHash(file: File, expectedHash: String): Boolean {
        val fileHash = MessageDigest
            .getInstance("SHA-256")
            .digest(file.readBytes())
            .joinToString("") { "%02x".format(it) }

        return fileHash == expectedHash
    }

    companion object {
        const val REPOSITORY_URL = "https://store.accrescent.app"
    }
}
