package app.accrescent.client.data

import app.accrescent.client.data.db.*
import javax.inject.Inject

class RepoDataLocalDataSource @Inject constructor(
    private val appDao: AppDao,
    private val developersDao: DeveloperDao,
    private val packageDao: PackageDao,
) {
    suspend fun saveApps(vararg apps: App) = appDao.insertApps(*apps)

    suspend fun saveDevelopers(vararg developers: Developer) =
        developersDao.insertDevelopers(*developers)

    suspend fun savePackages(vararg packages: Package) = packageDao.insertPackages(*packages)

    fun getApps() = appDao.getAll()

    suspend fun getAppVersion(appId: String) = appDao.getVersion(appId)

    suspend fun getAppMaintainer(appId: String) = developersDao.getMaintainer(appId)

    fun getPublicKey(username: String) = developersDao.getPublicKey(username)

    suspend fun getPackagesForApp(appId: String) = packageDao.getForApp(appId)

    suspend fun updateApps(vararg apps: App) = appDao.updateApps(*apps)

    suspend fun deleteRemovedApps(idsToKeep: List<String>) = appDao.deleteRemoved(idsToKeep)

    suspend fun deleteRemovedDevelopers(usernamesToKeep: List<String>) =
        developersDao.deleteRemoved(usernamesToKeep)

    suspend fun deleteRemovedPackages(appId: String, filesToKeep: List<String>) =
        packageDao.deleteRemoved(appId, filesToKeep)
}
