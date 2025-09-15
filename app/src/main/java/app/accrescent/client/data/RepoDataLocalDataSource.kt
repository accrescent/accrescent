package app.accrescent.client.data

import app.accrescent.client.data.db.App
import app.accrescent.client.data.db.AppDao
import app.accrescent.client.data.db.SigningCert
import app.accrescent.client.data.db.SigningCertDao
import jakarta.inject.Inject

class RepoDataLocalDataSource @Inject constructor(
    private val appDao: AppDao,
    private val signingCertDao: SigningCertDao,
) {
    suspend fun saveApps(vararg apps: App) = appDao.insertApps(*apps)

    suspend fun saveSigningCerts(vararg signingCerts: SigningCert) {
        signingCertDao.insertSigningCerts(*signingCerts)
    }

    suspend fun getAppMinVersionCode(appId: String) = appDao.getMinVersionCode(appId)

    suspend fun appExists(appId: String) = appDao.exists(appId)

    suspend fun deleteAllApps() = appDao.deleteAll()

    fun getAppSigners(appId: String) = signingCertDao.getSignersForApp(appId)
}
