// SPDX-FileCopyrightText: © 2021 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import app.accrescent.client.data.AppListingPagingSource
import build.buf.gen.accrescent.appstore.v1.AppServiceGrpcKt
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

// Taken from the default in app store API 1.0.1
private const val PAGE_SIZE = 50

@HiltViewModel
class AllAppsViewModel @Inject constructor(
    private val appService: AppServiceGrpcKt.AppServiceCoroutineStub,
) : ViewModel() {
    val appListings = Pager(
        config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
        pagingSourceFactory = { AppListingPagingSource(appService) },
    )
        .flow
        .cachedIn(viewModelScope)
}
