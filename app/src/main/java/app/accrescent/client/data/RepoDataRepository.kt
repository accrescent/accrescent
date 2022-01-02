package app.accrescent.client.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import app.accrescent.client.data.db.App
import app.accrescent.client.data.db.Package
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
    suspend fun fetchLatestRepoData() {
        val repoData = repoDataRemoteDataSource.fetchLatestRepoData()
        val timestampKey = longPreferencesKey("root")
        val storedTimestamp = timestampDataStore.data.map { it[timestampKey] ?: 0 }.first()

        if (repoData.timestamp >= storedTimestamp) {
            timestampDataStore.edit { it[timestampKey] = repoData.timestamp }
        } else {
            throw GeneralSecurityException("repodata timestamp less than saved value")
        }

        repoDataLocalDataSource.saveDevelopers(*repoData.developers.values.toTypedArray())
        repoDataLocalDataSource.deleteRemovedDevelopers(repoData.developers.values.map { it.username })
        repoDataLocalDataSource.saveApps(*repoData.apps.entries.map {
            App(it.key, repoData.developers[it.value]!!.username)
        }.toTypedArray())
        repoDataLocalDataSource.deleteRemovedApps(repoData.apps.keys.toList())

        context
            .packageManager
            .getInstalledPackages(0)
            .map { it.packageName }
            .mapNotNull { repoDataLocalDataSource.getAppMaintainer(it) }
            .map { fetchSubRepoData(it.username) }
    }

    suspend fun fetchSubRepoData(developer: String) {
        val publicKey = repoDataLocalDataSource.getPublicKey(developer)
        val repoData =
            repoDataRemoteDataSource.fetchSubRepoData(Developer(developer, publicKey.first()!!))
        val timestampKey = longPreferencesKey(developer)
        val storedTimestamp = timestampDataStore.data.map { it[timestampKey] ?: 0 }.first()

        if (repoData.timestamp >= storedTimestamp) {
            timestampDataStore.edit { it[timestampKey] = repoData.timestamp }
        } else {
            throw GeneralSecurityException("repodata timestamp less than saved value")
        }

        repoDataLocalDataSource.updateApps(*repoData.apps.entries.map {
            App(it.key, developer, it.value.versionCode)
        }.toTypedArray())
        for (app in repoData.apps.entries) {
            repoDataLocalDataSource.savePackages(*app.value.packages.map {
                Package(app.key, it.file, it.hash)
            }.toTypedArray())

            val filesToKeep = app.value.packages.map { it.file }.toList()
            repoDataLocalDataSource.deleteRemovedPackages(app.key, filesToKeep)
        }
    }

    fun getApps() = repoDataLocalDataSource.getApps()

    suspend fun getAppVersion(appId: String) = repoDataLocalDataSource.getAppVersion(appId)

    suspend fun getAppMaintainer(appId: String) = repoDataLocalDataSource.getAppMaintainer(appId)

    fun getPublicKey(username: String) = repoDataLocalDataSource.getPublicKey(username)

    suspend fun getPackagesForApp(appId: String) = repoDataLocalDataSource.getPackagesForApp(appId)
}
