package app.accrescent.client.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.accrescent.client.data.RepoDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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
