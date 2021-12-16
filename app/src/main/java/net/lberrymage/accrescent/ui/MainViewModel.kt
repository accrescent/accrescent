package net.lberrymage.accrescent.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.lberrymage.accrescent.data.MetadataRepository
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    @Inject
    lateinit var metadataRepository: MetadataRepository

    var metadataText by mutableStateOf("")
        private set

    fun refreshMetadata() {
        viewModelScope.launch {
            val metadata = metadataRepository.fetchLatestMetadata()
            metadataText = metadata.data
        }
    }
}