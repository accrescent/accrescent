package net.lberrymage.accrescent.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.lberrymage.accrescent.data.DeveloperRepository
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    @Inject
    lateinit var developerRepository: DeveloperRepository

    var publicKey by mutableStateOf("")
        private set

    fun refreshDevelopers() {
        viewModelScope.launch {
            val developer = developerRepository.fetchLatestDevelopers()
            publicKey = developer.publicKey
        }
    }
}