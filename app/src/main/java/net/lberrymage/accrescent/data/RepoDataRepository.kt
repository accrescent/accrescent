package net.lberrymage.accrescent.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.GeneralSecurityException
import javax.inject.Inject

class RepoDataRepository @Inject constructor(
    private val repoDataRemoteDataSource: RepoDataRemoteDataSource,
    private val repoDataLocalDataSource: RepoDataLocalDataSource,
    private val rootTimestampDataStore: DataStore<Preferences>,
) {
    suspend fun fetchLatestRepoData() {
        val repoData = repoDataRemoteDataSource.fetchLatestRepoData()
        val rootTimestampKey = longPreferencesKey("root_timestamp")
        val storedTimestamp = rootTimestampDataStore.data.map { it[rootTimestampKey] ?: 0 }.first()

        if (repoData.timestamp >= storedTimestamp) {
            rootTimestampDataStore.edit { it[rootTimestampKey] = repoData.timestamp }
        } else {
            throw GeneralSecurityException("repodata timestamp less than saved value")
        }

        repoDataLocalDataSource.saveDevelopers(*repoData.developers.toTypedArray())
        repoDataLocalDataSource.deleteRemovedDevelopers(repoData.developers.map { it.username })
    }

    fun getPublicKey(username: String) = repoDataLocalDataSource.getPublicKey(username)
}
