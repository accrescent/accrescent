package app.accrescent.client.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import app.accrescent.client.Accrescent
import app.accrescent.client.R
import app.accrescent.client.data.AppInstallStatuses
import app.accrescent.client.data.AppListingPagingSource
import app.accrescent.client.data.InstallStatus
import app.accrescent.client.data.RepoDataRepository
import app.accrescent.client.util.getPackageInstallStatus
import build.buf.gen.accrescent.directory.v1.DirectoryServiceGrpcKt
import build.buf.gen.accrescent.directory.v1.getAppPackageInfoRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import java.io.FileNotFoundException
import java.net.ConnectException
import java.net.UnknownHostException
import java.security.GeneralSecurityException

private const val PAGE_SIZE = 50

@HiltViewModel
class UpdatableAppsViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val directoryService: DirectoryServiceGrpcKt.DirectoryServiceCoroutineStub,
    private val repoDataRepository: RepoDataRepository,
    appInstallStatuses: AppInstallStatuses,
) : AndroidViewModel(context as Application) {
    val appListings = Pager(
        config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
        pagingSourceFactory = { AppListingPagingSource(directoryService) }
    )
        .flow
        .cachedIn(viewModelScope)
        .combine(snapshotFlow { appInstallStatuses.statuses.toMap() }) { listings, statuses ->
            listings.filter { listing ->
                val status = statuses[listing.appId] ?: InstallStatus.LOADING
                status == InstallStatus.UPDATABLE
            }
        }

    // Initialize install status for apps as they're added
    init {
        val flow = appListings.onEach { listings ->
            listings.map { listing ->
                val latestVersionCode = try {
                    directoryService
                        .getAppPackageInfo(getAppPackageInfoRequest { appId = listing.appId })
                        .packageInfo
                        .versionCode
                        .toLong()
                } catch (_: Exception) {
                    null
                }
                appInstallStatuses.statuses[listing.appId] =
                    context.packageManager.getPackageInstallStatus(listing.appId, latestVersionCode)
            }
        }
        viewModelScope.launch {
            flow.collect()
        }
    }

    var isRefreshing by mutableStateOf(false)
        private set
    var error: String? by mutableStateOf(null)

    fun refreshRepoData() {
        viewModelScope.launch {
            error = null
            isRefreshing = true

            val context = getApplication<Accrescent>().applicationContext

            try {
                repoDataRepository.fetchRepoData()
            } catch (e: ConnectException) {
                error = context.getString(R.string.network_error, e.message)
            } catch (e: FileNotFoundException) {
                error = context.getString(R.string.failed_download_repodata, e.message)
            } catch (e: GeneralSecurityException) {
                error = context.getString(R.string.failed_verify_repodata, e.message)
            } catch (e: SerializationException) {
                error = context.getString(R.string.failed_decode_repodata, e.message)
            } catch (e: UnknownHostException) {
                error = context.getString(R.string.unknown_host_error, e.message)
            }

            isRefreshing = false
        }
    }
}
