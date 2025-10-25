// SPDX-FileCopyrightText: © 2025 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.data.appmanager

import android.content.Context
import android.content.pm.PackageInstaller
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

@Singleton
class InstallSessionRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val packageInstaller = context.packageManager.packageInstaller
    private val sessionInfoMap = MutableStateFlow(getSessionInfoMap())
    private val sessionResults = MutableStateFlow(emptyMap<Int, InstallSessionResult>())

    private val sessionCallback = object : PackageInstaller.SessionCallback() {
        override fun onActiveChanged(sessionId: Int, active: Boolean) = updateSessionInfo(sessionId)
        override fun onBadgingChanged(sessionId: Int) = updateSessionInfo(sessionId)
        override fun onCreated(sessionId: Int) = updateSessionInfo(sessionId)
        override fun onFinished(sessionId: Int, success: Boolean) = updateSessionInfo(sessionId)
        override fun onProgressChanged(sessionId: Int, progress: Float) =
            updateSessionInfo(sessionId)
    }

    init {
        packageInstaller.registerSessionCallback(sessionCallback)
    }

    fun close() {
        packageInstaller.unregisterSessionCallback(sessionCallback)
    }

    fun getSessionStateForApp(appId: String): Flow<InstallSessionState?> {
        return combine(sessionInfoMap, sessionResults) { infoMap, results ->
            infoMap
                .values
                .filter { session -> session.appPackageName == appId }
                .maxByOrNull { session ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        session.createdMillis
                    } else {
                        // We don't have a good alternative to createdMillis below API 30, so app
                        // install session states may be incorrect on API 29 and below when stale
                        // (i.e., unabandoned, inactive, uncommitted) sessions exist.
                        0
                    }
                }
                ?.let { session ->
                    val result = results[session.sessionId]
                    if (result == null) {
                        if (session.isCommitted && session.isActive) {
                            InstallSessionState.InProgress
                        } else {
                            null
                        }
                    } else {
                        InstallSessionState.Completed(session.sessionId, result)
                    }
                }
        }
    }

    fun setSessionResult(sessionId: Int, result: InstallSessionResult) {
        sessionResults.update { results ->
            results + (sessionId to result)
        }
    }

    fun clearSessionResultsForApp(appId: String) {
        val sessionIdsToClear = getSessionIdsForApp(appId)

        sessionResults.update { results ->
            results.filterNot { sessionIdsToClear.contains(it.key) }
        }
    }

    fun clearSessionsForApp(appId: String) {
        val sessionIdsToClear = getSessionIdsForApp(appId)

        sessionInfoMap.update { sessions ->
            sessions - sessionIdsToClear
        }
    }

    private fun getSessionIdsForApp(appId: String): Set<Int> {
        return sessionInfoMap
            .value
            .filterValues { it.appPackageName == appId }
            .values
            .mapTo(mutableSetOf()) { it.sessionId }
    }

    private fun updateSessionInfo(sessionId: Int) {
        packageInstaller.getSessionInfo(sessionId)?.let { sessionInfo ->
            sessionInfoMap.update { sessions ->
                sessions + (sessionInfo.sessionId to sessionInfo)
            }
        }
    }

    private fun getSessionInfoMap(): Map<Int, PackageInstaller.SessionInfo> {
        return packageInstaller.mySessions.associateBy { it.sessionId }
    }
}
