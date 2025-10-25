// SPDX-FileCopyrightText: © 2021 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.accrescent.client.data.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.days

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
) : ViewModel() {
    private companion object {
        private val MIN_TIME_BETWEEN_DONATE_REQUESTS = 90.days
    }

    fun shouldShowDonateRequest(): Flow<Boolean> {
        return preferencesManager.donateRequestLastSeen.map { lastSeen ->
            val timeSinceLastDonateRequest = System.currentTimeMillis() - lastSeen

            timeSinceLastDonateRequest > MIN_TIME_BETWEEN_DONATE_REQUESTS.inWholeMilliseconds
        }
    }

    fun updateDonateRequestLastSeen() {
        viewModelScope.launch {
            preferencesManager.setDonateRequestLastSeen(System.currentTimeMillis())
        }
    }
}
