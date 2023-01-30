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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.accrescent.client.BuildConfig
import app.accrescent.client.R
import app.accrescent.client.data.DownloadProgress
import app.accrescent.client.data.InstallStatus
import app.accrescent.client.util.isPrivileged

@Composable
fun AppDetailsScreen(
    snackbarHostState: SnackbarHostState,
    viewModel: AppDetailsViewModel = viewModel(),
) {
    val context = LocalContext.current
    val installStatus = viewModel.installStatuses[viewModel.uiState.appId]
    val downloadProgress = viewModel.downloadProgresses[viewModel.uiState.appId]
    val requireUserAction by viewModel.requireUserAction.collectAsState(!context.isPrivileged())

    var installConfirmDialog by remember { mutableStateOf(false) }
    var uninstallConfirmDialog by remember { mutableStateOf(false) }

    when {
        viewModel.uiState.isFetchingData -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.size(72.dp))
            }
        }
        viewModel.uiState.appExists -> AppDetails(
            id = viewModel.uiState.appId,
            name = viewModel.uiState.appName,
            versionName = viewModel.uiState.versionName,
            versionCode = viewModel.uiState.versionCode,
            installStatus = installStatus ?: InstallStatus.LOADING,
            onInstallClicked = {
                if (
                    context.isPrivileged() &&
                    installStatus == InstallStatus.INSTALLABLE &&
                    requireUserAction
                ) {
                    installConfirmDialog = true
                } else {
                    viewModel.installApp(viewModel.uiState.appId)
                }
            },
            onUninstallClicked = {
                // When uninstalling in privileged mode, the OS doesn't create a
                // confirmation dialog. To prevent users from mistakenly deleting
                // important app data, create our own dialog in this case.
                if (context.isPrivileged()) {
                    uninstallConfirmDialog = true
                } else {
                    viewModel.uninstallApp(viewModel.uiState.appId)
                }
            },
            onOpenClicked = { viewModel.openApp(viewModel.uiState.appId) },
            downloadProgress = downloadProgress,
        )
        else -> AppNotFoundError()
    }

    if (installConfirmDialog) {
        ActionConfirmDialog(
            title = stringResource(R.string.install_confirm),
            description = stringResource(R.string.install_confirm_desc),
            onDismiss = { installConfirmDialog = false },
            onConfirm = { viewModel.installApp(viewModel.uiState.appId) }
        )
    }
    if (uninstallConfirmDialog) {
        ActionConfirmDialog(
            title = stringResource(R.string.uninstall_confirm),
            description = stringResource(R.string.uninstall_confirm_desc),
            onDismiss = { uninstallConfirmDialog = false },
            onConfirm = { viewModel.uninstallApp(viewModel.uiState.appId) }
        )
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
    installStatus: InstallStatus,
    onInstallClicked: () -> Unit,
    onUninstallClicked: () -> Unit,
    onOpenClicked: () -> Unit,
    downloadProgress: DownloadProgress?,
) {
    val context = LocalContext.current
    var waitingForSize by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
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
                InstallStatus.UPDATABLE ->
                    // We can't uninstall ourselves if we're a priv-app
                    if (!(context.isPrivileged() && id == BuildConfig.APPLICATION_ID)) {
                        OutlinedButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 6.dp),
                            onClick = { onUninstallClicked() },
                        ) {
                            Text(stringResource(R.string.uninstall))
                        }
                    }
                else -> Unit
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 6.dp),
                enabled = downloadProgress == null,
                onClick = {
                    when (installStatus) {
                        InstallStatus.INSTALLABLE,
                        InstallStatus.UPDATABLE -> {
                            waitingForSize = true
                            onInstallClicked()
                        }
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

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (waitingForSize && downloadProgress == null) {
                CircularProgressIndicator(modifier = Modifier.size(96.dp))
                // Spacer to align this indicator with the other when it appears
                Text("", Modifier.padding(top = 16.dp))
            } else if (downloadProgress != null) {
                waitingForSize = false

                CircularProgressIndicator(
                    modifier = Modifier.size(96.dp),
                    progress = downloadProgress.part.toFloat() / downloadProgress.total,
                )

                val partMb = "%.1f".format(downloadProgress.part.toFloat() / 1_000_000)
                val totalMb = "%.1f".format(downloadProgress.total.toFloat() / 1_000_000)

                Text("$partMb MB / $totalMb MB", Modifier.padding(top = 16.dp))
            }

            Text(id, Modifier.padding(top = 48.dp))
        }
    }
}

@Composable
fun AppNotFoundError() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
