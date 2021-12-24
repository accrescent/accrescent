package net.lberrymage.accrescent.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.lberrymage.accrescent.data.RepoDataRepository
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repoDataRepository: RepoDataRepository) :
    ViewModel() {
    val anonymousPublicKey = repoDataRepository.getPublicKey("anonymous")

    fun refreshDevelopers() {
        viewModelScope.launch {
            repoDataRepository.fetchLatestRepoData()
        }
    }
}
