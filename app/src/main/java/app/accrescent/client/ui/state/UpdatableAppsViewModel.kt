package app.accrescent.client.ui.state

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.filter
import app.accrescent.client.core.Outcome
import app.accrescent.client.data.AppListingPagingSource
import app.accrescent.client.data.appmanager.AppManager
import build.buf.gen.accrescent.appstore.v1.AppServiceGrpcKt
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val LOG_TAG = "UpdatableAppsViewModel"

// Taken from the default in app store API 1.0.1
private const val PAGE_SIZE = 50

@HiltViewModel
class UpdatableAppsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appManager: AppManager,
    private val appService: AppServiceGrpcKt.AppServiceCoroutineStub,
) : ViewModel() {
    private val updatableAppIds = MutableStateFlow(emptyList<String>())
    val appListings = Pager(
        config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
        pagingSourceFactory = { AppListingPagingSource(appService) },
    )
        .flow
        .cachedIn(viewModelScope)
        .combine(updatableAppIds) { pagingData, updatableAppIds ->
            pagingData.filter { appListing ->
                updatableAppIds.contains(appListing.appId)
            }
        }

    private val packageEventReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val receivedAppId = intent.data?.schemeSpecificPart ?: run {
                Log.e(LOG_TAG, "no package data for intent $intent")
                return
            }

            when (intent.action) {
                Intent.ACTION_PACKAGE_ADDED,
                Intent.ACTION_PACKAGE_REPLACED -> {
                    viewModelScope.launch {
                        updateUpdatabilityForApp(receivedAppId)
                    }
                }

                Intent.ACTION_PACKAGE_FULLY_REMOVED,
                Intent.ACTION_PACKAGE_REMOVED -> updatableAppIds.update { appIds ->
                    appIds - receivedAppId
                }
            }
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        ContextCompat.registerReceiver(
            context,
            packageEventReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )

        loadUpdatableAppIds()
    }

    fun loadUpdatableAppIds() {
        viewModelScope.launch {
            for (appId in getAppIdsSelfResponsibleForUpdating()) {
                updateUpdatabilityForApp(appId)
            }
        }
    }

    override fun onCleared() {
        context.unregisterReceiver(packageEventReceiver)
    }

    private suspend fun updateUpdatabilityForApp(appId: String) {
        val isUpdateAvailable = when (val result = appManager.isUpdateAvailable(appId)) {
            // Act as if there is no update available if we encounter an error
            is Outcome.Err -> false
            is Outcome.Ok -> result.value
        }
        if (isUpdateAvailable) {
            updatableAppIds.update { appIds ->
                appIds + appId
            }
        }
    }

    /**
     * Returns all applications on the device we're responsible for updating.
     *
     * @return the list of app IDs corresponding to the installed applications on the device
     */
    private fun getAppIdsSelfResponsibleForUpdating(): List<String> {
        return context
            .packageManager
            .getInstalledApplications(0)
            .map { it.packageName }
            .filter { appManager.selfResponsibleForUpdatingPackage(it) }
    }
}
