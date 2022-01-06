package app.accrescent.client.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.accrescent.client.data.RepoDataRepository
import app.accrescent.client.util.PackageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repoDataRepository: RepoDataRepository,
    private val packageManager: PackageManager
) : ViewModel() {
    val apps = repoDataRepository.getApps()

    fun refreshRepoData() {
        viewModelScope.launch {
            repoDataRepository.fetchRepoData()
        }
    }

    fun installApp(appId: String) {
        viewModelScope.launch {
            packageManager.downloadAndInstall(appId)
        }
    }
}
