package app.accrescent.client.data

import app.accrescent.client.data.db.App
import app.accrescent.client.data.db.AppDao
import app.accrescent.client.data.db.SigningKey
import app.accrescent.client.data.db.SigningKeyDao
import javax.inject.Inject

class RepoDataLocalDataSource @Inject constructor(
    private val appDao: AppDao,
    private val signingKeyDao: SigningKeyDao,
) {
    suspend fun saveApps(vararg apps: App) = appDao.insertApps(*apps)

    suspend fun saveSigningKeys(vararg signingKeys: SigningKey) {
        signingKeyDao.insertSigningKeys(*signingKeys)
    }

    fun getApps() = appDao.getAll()

    suspend fun appExists(appId: String) = appDao.exists(appId)

    suspend fun deleteRemovedApps(idsToKeep: List<String>) = appDao.deleteRemoved(idsToKeep)

    suspend fun deleteRemovedSigningKeys(appId: String, hashesToKeep: List<String>) {
        signingKeyDao.deleteRemoved(appId, hashesToKeep)
    }

    fun getAppSigners(appId: String) = signingKeyDao.getSignersForApp(appId)
}
