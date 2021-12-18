package net.lberrymage.accrescent.data

import javax.inject.Inject

class DevelopersLocalDataSource @Inject constructor(private val developersDao: DeveloperDao) {
    suspend fun saveDevelopers(vararg developers: Developer) =
        developersDao.insertDevelopers(*developers)

    fun getPublicKey(username: String) = developersDao.getPublicKey(username)
}
