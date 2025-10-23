package app.accrescent.client.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.room.withTransaction
import app.accrescent.client.R
import app.accrescent.client.data.db.App
import app.accrescent.client.data.db.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.GeneralSecurityException
import kotlin.math.max

class RepoDataRepository @Inject constructor(
    private val appDatabase: AppDatabase,
    @ApplicationContext private val context: Context,
    private val repoDataRemoteDataSource: RepoDataRemoteDataSource,
    private val repoDataLocalDataSource: RepoDataLocalDataSource,
    private val timestampDataStore: DataStore<Preferences>,
) {
    suspend fun fetchRepoData() {
        val repoData = repoDataRemoteDataSource.fetchRepoData()
        val timestampKey = longPreferencesKey("timestamp")
        val storedTimestamp =
            timestampDataStore.data.map { max(it[timestampKey] ?: 0, MIN_TIMESTAMP) }.first()

        if (repoData.timestamp >= storedTimestamp) {
            timestampDataStore.edit { it[timestampKey] = repoData.timestamp }
        } else {
            throw GeneralSecurityException(context.getString(R.string.repodata_timestamp_less))
        }

        // This approach is suboptimal since we're replacing existing app entries instead of
        // updating them
        val apps = repoData
            .apps
            .map { (appId, app) ->
                App(
                    id = appId,
                    minVersionCode = app.minVersionCode,
                    signingCertHash = app.signingCertHash,
                )
            }

        appDatabase.withTransaction {
            repoDataLocalDataSource.deleteAllApps()
            repoDataLocalDataSource.saveApps(*apps.toTypedArray())
        }
    }

    suspend fun getAppMinVersionCode(appId: String): Long? {
        return repoDataLocalDataSource.getAppMinVersionCode(appId)
    }

    suspend fun getAppSigner(appId: String) = repoDataLocalDataSource.getAppSigner(appId)
}
