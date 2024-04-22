package app.accrescent.client.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.accrescent.client.BuildConfig
import app.accrescent.client.R
import app.accrescent.client.data.DownloadProgress
import app.accrescent.client.data.InstallStatus

@Composable
fun AppDetailsScreen(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: AppDetailsViewModel = hiltViewModel(),
) {
    val installStatus = viewModel.installStatuses[viewModel.uiState.appId]
    val downloadProgress = viewModel.downloadProgresses[viewModel.uiState.appId]

    when {
        viewModel.uiState.isFetchingData -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.size(72.dp))
            }
        }

        viewModel.uiState.appExists -> AppDetails(
            id = viewModel.uiState.appId,
            name = viewModel.uiState.appName,
            versionName = viewModel.uiState.versionName,
            versionCode = viewModel.uiState.versionCode,
            shortDescription = viewModel.uiState.shortDescription,
            installStatus = installStatus ?: InstallStatus.LOADING,
            onInstallClicked = { viewModel.installApp(viewModel.uiState.appId) },
            onUninstallClicked = { viewModel.uninstallApp(viewModel.uiState.appId) },
            onOpenClicked = { viewModel.openApp(viewModel.uiState.appId) },
            onOpenAppInfoClicked = { viewModel.openAppInfo(viewModel.uiState.appId) },
            downloadProgress = downloadProgress,
            modifier,
        )

        else -> AppNotFoundError(modifier)
    }

    if (viewModel.uiState.error != null) {
        LaunchedEffect(snackbarHostState) {
            snackbarHostState.showSnackbar(message = viewModel.uiState.error!!)
            viewModel.uiState.error = null
        }
    }
}

@Composable
fun AppDetails(
    id: String,
    name: String,
    versionName: String,
    versionCode: Long,
    shortDescription: String,
    installStatus: InstallStatus,
    onInstallClicked: () -> Unit,
    onUninstallClicked: () -> Unit,
    onOpenClicked: () -> Unit,
    onOpenAppInfoClicked: () -> Unit,
    downloadProgress: DownloadProgress?,
    modifier: Modifier = Modifier,
) {
    var waitingForSize by remember { mutableStateOf(false) }

    Column(
        modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppIcon(id, Modifier.size(128.dp))
        Spacer(Modifier.size(8.dp))
        Text(name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Text(
            text = shortDescription,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp),
        )
        Column(Modifier.width(256.dp), verticalArrangement = Arrangement.Center) {
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.version, versionName),
                style = MaterialTheme.typography.titleSmall,
                fontFamily = FontFamily.Monospace,
            )
            Text(
                stringResource(R.string.version_code, versionCode),
                style = MaterialTheme.typography.titleSmall,
                fontFamily = FontFamily.Monospace,
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            when (installStatus) {
                InstallStatus.INSTALLED,
                InstallStatus.UPDATABLE,
                InstallStatus.DISABLED -> OutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 6.dp),
                    onClick = { onUninstallClicked() },
                ) {
                    Text(stringResource(R.string.uninstall))
                }

                else -> Unit
            }
            if (!(installStatus == InstallStatus.INSTALLED && id == BuildConfig.APPLICATION_ID)) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 6.dp),
                    enabled = downloadProgress == null && !waitingForSize,
                    onClick = {
                        when (installStatus) {
                            InstallStatus.INSTALLABLE,
                            InstallStatus.UPDATABLE -> {
                                waitingForSize = true
                                onInstallClicked()
                            }

                            InstallStatus.DISABLED -> onOpenAppInfoClicked()
                            InstallStatus.INSTALLED -> onOpenClicked()
                            InstallStatus.LOADING,
                            InstallStatus.UNKNOWN -> Unit
                        }
                    },
                ) {
                    when (installStatus) {
                        InstallStatus.INSTALLABLE ->
                            Text(stringResource(R.string.install))

                        InstallStatus.UPDATABLE ->
                            Text(stringResource(R.string.update))

                        InstallStatus.DISABLED ->
                            Text(stringResource(R.string.enable))

                        InstallStatus.INSTALLED ->
                            Text(stringResource(R.string.open))

                        InstallStatus.LOADING ->
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 3.dp
                            )

                        InstallStatus.UNKNOWN ->
                            Text(stringResource(R.string.unknown))
                    }
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (waitingForSize && downloadProgress == null) {
                CircularProgressIndicator(modifier = Modifier.size(96.dp))
                // Spacer to align this indicator with the other when it appears
                Text("", Modifier.padding(top = 16.dp))
            } else if (downloadProgress != null) {
                waitingForSize = false

                CircularProgressIndicator(
                    modifier = Modifier.size(96.dp),
                    progress = { downloadProgress.part.toFloat() / downloadProgress.total },
                )

                val partMb = "%.1f".format(downloadProgress.part.toFloat() / 1_000_000)
                val totalMb = "%.1f".format(downloadProgress.total.toFloat() / 1_000_000)

                Text("$partMb MB / $totalMb MB", Modifier.padding(top = 16.dp))
            } else {
                Spacer(modifier = Modifier.size(96.dp))
                Text("", Modifier.padding(top = 16.dp))
            }
        }
        Text(id)
    }
}

@Composable
fun AppNotFoundError(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.error,
            ),
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.cant_find_app),
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
