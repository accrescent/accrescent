package app.accrescent.client.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import app.accrescent.client.data.db.App
import app.accrescent.client.data.db.SigningKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.GeneralSecurityException
import javax.inject.Inject

class RepoDataRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repoDataRemoteDataSource: RepoDataRemoteDataSource,
    private val repoDataLocalDataSource: RepoDataLocalDataSource,
    private val timestampDataStore: DataStore<Preferences>,
) {
    suspend fun fetchRepoData() {
        val repoData = repoDataRemoteDataSource.fetchRepoData()
        val timestampKey = longPreferencesKey("timestamp")
        val storedTimestamp = timestampDataStore.data.map { it[timestampKey] ?: 0 }.first()

        if (repoData.timestamp >= storedTimestamp) {
            timestampDataStore.edit { it[timestampKey] = repoData.timestamp }
        } else {
            throw GeneralSecurityException("repodata timestamp less than saved value")
        }

        // This approach is suboptimal since we're replacing existing app entries instead of
        // updating them
        val apps = repoData
            .apps
            .map { (appId, app) -> App(appId, app.name, app.minVersionCode, app.iconHash) }
        repoDataLocalDataSource.saveApps(*apps.toTypedArray())
        repoDataLocalDataSource.deleteRemovedApps(apps.map { it.id })

        val signingKeys = repoData.apps.map { (appId, app) ->
            app.signingKeyHashes.map { SigningKey(appId, it) }
        }.flatten()
        repoDataLocalDataSource.saveSigningKeys(*signingKeys.toTypedArray())
        repoData.apps.map { (appId, app) -> Pair(appId, app.signingKeyHashes) }
            .forEach { repoDataLocalDataSource.deleteRemovedSigningKeys(it.first, it.second) }
    }

    fun getApps() = repoDataLocalDataSource.getApps()

    suspend fun getAppRepoData(appId: String) = repoDataRemoteDataSource.fetchAppRepoData(appId)

    suspend fun appExists(appId: String) = repoDataLocalDataSource.appExists(appId)

    fun getAppSigners(appId: String) = repoDataLocalDataSource.getAppSigners(appId)
}
