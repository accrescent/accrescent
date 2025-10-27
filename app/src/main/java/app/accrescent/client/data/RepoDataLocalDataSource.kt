// SPDX-FileCopyrightText: © 2021 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.data

import app.accrescent.client.data.db.App
import app.accrescent.client.data.db.AppDao
import jakarta.inject.Inject

class RepoDataLocalDataSource @Inject constructor(private val appDao: AppDao) {
    suspend fun saveApps(vararg apps: App) = appDao.insertApps(*apps)

    suspend fun getAppMinVersionCode(appId: String) = appDao.getMinVersionCode(appId)

    suspend fun deleteAllApps() = appDao.deleteAll()

    suspend fun getAppSigner(appId: String) = appDao.getSigningCertHash(appId)
}
