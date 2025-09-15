package app.accrescent.client.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.accrescent.client.data.AppInstallStatuses
import app.accrescent.client.data.RepoDataRepository
import app.accrescent.client.util.getPackageInstallStatus
import build.buf.gen.accrescent.directory.v1.DirectoryServiceGrpcKt
import build.buf.gen.accrescent.directory.v1.getAppPackageInfoRequest
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AppStatusChangeBroadcastReceiver : BroadcastReceiver() {
    @Inject
    lateinit var appInstallStatuses: AppInstallStatuses

    @Inject
    lateinit var directoryService: DirectoryServiceGrpcKt.DirectoryServiceCoroutineStub

    @Inject
    lateinit var repoDataRepository: RepoDataRepository

    private val scope = CoroutineScope(SupervisorJob())

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult: PendingResult = goAsync()

        when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED,
            Intent.ACTION_PACKAGE_CHANGED,
            Intent.ACTION_PACKAGE_REMOVED,
            Intent.ACTION_PACKAGE_FULLY_REMOVED -> {
                scope.launch {
                    val appId = intent.data?.schemeSpecificPart ?: run {
                        pendingResult.finish()
                        return@launch
                    }

                    // We don't care about apps Accrescent doesn't serve
                    if (!repoDataRepository.appExists(appId)) {
                        return@launch
                    }

                    val versionCode = try {
                        val request = getAppPackageInfoRequest { this.appId = appId }
                        directoryService.getAppPackageInfo(request).packageInfo.versionCode
                    } catch (e: Exception) {
                        null
                    }
                    val status = context.packageManager.getPackageInstallStatus(appId, versionCode)
                    appInstallStatuses.statuses[appId] = status

                    pendingResult.finish()
                }
            }
        }
    }
}
