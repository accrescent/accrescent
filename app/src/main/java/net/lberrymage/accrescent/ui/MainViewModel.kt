package net.lberrymage.accrescent.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.lberrymage.accrescent.data.DeveloperRepository
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val developerRepository: DeveloperRepository) :
    ViewModel() {
    val anonymousPublicKey = developerRepository.getPublicKey("anonymous")

    fun refreshDevelopers() {
        viewModelScope.launch {
            developerRepository.fetchLatestDevelopers()
        }
    }
}
