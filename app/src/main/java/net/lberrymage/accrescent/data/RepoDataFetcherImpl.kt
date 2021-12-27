package net.lberrymage.accrescent.data

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.lberrymage.accrescent.util.verifySignature
import java.io.ByteArrayOutputStream
import java.net.URL
import java.security.GeneralSecurityException
import javax.inject.Inject
import javax.net.ssl.HttpsURLConnection

class RepoDataFetcherImpl @Inject constructor() : RepoDataFetcher {
    override fun fetchLatestRepoData(): RepoData {
        val repoDataFile = fetchFileString(URL(REPOSITORY_URL + DEVELOPERS_PATH))
        val signature = fetchFileString(URL("$REPOSITORY_URL$DEVELOPERS_PATH.sig"))

        if (!verifySignature(DEVELOPERS_PUBKEY, repoDataFile.toByteArray(), signature)) {
            throw GeneralSecurityException("signature verification failed")
        }

        return Json.decodeFromString(repoDataFile)
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
        const val REPOSITORY_URL = "https://store.accrescent.app"
        const val DEVELOPERS_PATH = "/repodata.json"
        const val DEVELOPERS_PUBKEY = "untrusted comment: signify public key\n" +
                "RWS0w+cSbvRMas9nUV/VXldWZ7M2QYSUSQ6vrKA5MehEeD3N8tIfIxT5\n"
    }
}
