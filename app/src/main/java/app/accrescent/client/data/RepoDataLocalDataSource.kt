package app.accrescent.client.data

import app.accrescent.client.data.db.App
import app.accrescent.client.data.db.AppDao
import app.accrescent.client.data.db.SigningCert
import app.accrescent.client.data.db.SigningCertDao
import javax.inject.Inject

class RepoDataLocalDataSource @Inject constructor(
    private val appDao: AppDao,
    private val signingCertDao: SigningCertDao,
) {
    suspend fun saveApps(vararg apps: App) = appDao.insertApps(*apps)

    suspend fun saveSigningCerts(vararg signingCerts: SigningCert) {
        signingCertDao.insertSigningCerts(*signingCerts)
    }

    suspend fun getApp(appId: String) = appDao.get(appId)

    fun getApps() = appDao.getAll()

    suspend fun getAppMinVersionCode(appId: String) = appDao.getMinVersionCode(appId)

    suspend fun appExists(appId: String) = appDao.exists(appId)

    suspend fun deleteRemovedApps(idsToKeep: List<String>) = appDao.deleteRemoved(idsToKeep)

    suspend fun deleteRemovedSigningCerts(appId: String, hashesToKeep: List<String>) {
        signingCertDao.deleteRemoved(appId, hashesToKeep)
    }

    fun getAppSigners(appId: String) = signingCertDao.getSignersForApp(appId)
}
