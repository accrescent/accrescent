package app.accrescent.client.data

import android.content.Context
import app.accrescent.client.R
import app.accrescent.client.data.net.AppRepoData
import app.accrescent.client.data.net.RepoData
import app.accrescent.client.util.verifySignature
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL
import java.security.GeneralSecurityException
import javax.inject.Inject

private val format = Json { ignoreUnknownKeys = true }

class RepoDataFetcherImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient,
) : RepoDataFetcher {
    override fun fetchRepoData(): RepoData {
        val repoDataUrl = "$REPOSITORY_URL/repodata.$PUBKEY_VERSION.json"

        val repoDataFile = fetchFileString(URL(repoDataUrl))
        val signature = fetchFileString(URL("$repoDataUrl.sig"))

        if (!verifySignature(REPODATA_PUBKEY, repoDataFile.toByteArray(), signature)) {
            throw GeneralSecurityException(context.getString(R.string.sig_verify_failed))
        }

        return format.decodeFromString(repoDataFile)
    }

    override fun fetchAppRepoData(appId: String): AppRepoData {
        val repoDataFile = fetchFileString(URL("$REPOSITORY_URL/apps/$appId/repodata.json"))

        return format.decodeFromString(repoDataFile)
    }

    private fun fetchFileString(url: URL): String {
        val request = Request(url.toString().toHttpUrl())

        return okHttpClient
            .newCall(request)
            .execute()
            .use { it.body.string() }
    }
}
