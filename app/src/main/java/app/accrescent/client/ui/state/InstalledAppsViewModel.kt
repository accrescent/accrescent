// SPDX-FileCopyrightText: © 2021 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

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
import app.accrescent.client.data.AppListingPagingSource
import build.buf.gen.accrescent.appstore.v1.AppServiceGrpcKt
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

private const val LOG_TAG = "InstalledAppsViewModel"

// Taken from the default in app store API 1.0.1
private const val PAGE_SIZE = 50

@HiltViewModel
class InstalledAppsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appService: AppServiceGrpcKt.AppServiceCoroutineStub,
) : ViewModel() {
    private val installedAppIds = MutableStateFlow(getInstalledAppIds().toSet())
    val appListings = Pager(
        config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
        pagingSourceFactory = { AppListingPagingSource(appService) },
    )
        .flow
        .cachedIn(viewModelScope)
        .combine(installedAppIds) { pagingData, installedAppIds ->
            pagingData.filter { appListing ->
                installedAppIds.contains(appListing.appId)
            }
        }

    private val packageEventReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val receivedAppId = intent.data?.schemeSpecificPart ?: run {
                Log.e(LOG_TAG, "no package data for intent $intent")
                return
            }

            when (intent.action) {
                Intent.ACTION_PACKAGE_ADDED -> installedAppIds.update { appIds ->
                    appIds + receivedAppId
                }

                Intent.ACTION_PACKAGE_FULLY_REMOVED,
                Intent.ACTION_PACKAGE_REMOVED -> installedAppIds.update { appIds ->
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
            addDataScheme("package")
        }
        ContextCompat.registerReceiver(
            context,
            packageEventReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
    }

    override fun onCleared() {
        context.unregisterReceiver(packageEventReceiver)
    }

    /**
     * Returns all installed applications on the device.
     *
     * @return the list of app IDs corresponding to the installed applications on the device
     */
    private fun getInstalledAppIds(): List<String> {
        return context.packageManager.getInstalledApplications(0).map { it.packageName }
    }
}
