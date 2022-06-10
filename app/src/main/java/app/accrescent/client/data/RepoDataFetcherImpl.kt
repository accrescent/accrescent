package app.accrescent.client.data

import app.accrescent.client.data.net.AppRepoData
import app.accrescent.client.data.net.RepoData
import app.accrescent.client.util.verifySignature
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.net.URL
import java.security.GeneralSecurityException
import javax.inject.Inject
import javax.net.ssl.HttpsURLConnection

private val format = Json { ignoreUnknownKeys }

class RepoDataFetcherImpl @Inject constructor() : RepoDataFetcher {
    override fun fetchRepoData(): RepoData {
        val repoDataFile = fetchFileString(URL(REPOSITORY_URL + REPODATA_PATH))
        val signature = fetchFileString(URL("$REPOSITORY_URL$REPODATA_PATH.sig"))

        if (!verifySignature(REPODATA_PUBKEY, repoDataFile.toByteArray(), signature)) {
            throw GeneralSecurityException("signature verification failed")
        }

        return format.decodeFromString(repoDataFile)
    }

    override fun fetchAppRepoData(appId: String): AppRepoData {
        val repoDataFile = fetchFileString(URL("$REPOSITORY_URL/apps/$appId$REPODATA_PATH"))

        return format.decodeFromString(repoDataFile)
    }

    private fun fetchFileString(url: URL): String {
        val connection = url.openConnection() as HttpsURLConnection

        connection.connect()

        val data = connection.inputStream
        val buf = ByteArray(DEFAULT_BUFFER_SIZE)
        val outBuf = ByteArrayOutputStream()

        var bytes = data.read(buf)
        while (bytes >= 0) {
            outBuf.write(buf, 0, bytes)
            bytes = data.read(buf)
        }

        outBuf.close()
        connection.disconnect()

        return outBuf.toString()
    }

    companion object {
        const val REPODATA_PATH = "/repodata.json"
        const val REPODATA_PUBKEY = "RWT8aZ/NdUmXCPqQ0we7UyCe34q1xRfncBFVK5dI3ok9BkL1bFF3mgh3"
    }
}
