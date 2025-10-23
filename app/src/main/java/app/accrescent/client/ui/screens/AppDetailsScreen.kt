package app.accrescent.client.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.accrescent.client.R
import app.accrescent.client.ui.components.ActionableCard
import app.accrescent.client.ui.components.AppIcon
import app.accrescent.client.ui.components.CloseableErrorBox
import app.accrescent.client.ui.state.AppActionButton
import app.accrescent.client.ui.state.AppDetailsLoadState
import app.accrescent.client.ui.state.AppDetailsUiState
import app.accrescent.client.ui.state.AppDetailsViewModel
import app.accrescent.client.ui.state.Progress

@Composable
fun AppDetailsScreen(
    onGoBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AppDetailsViewModel = hiltViewModel(),
) {
    val screenUiState by viewModel.screenUiState.collectAsStateWithLifecycle()

    when (val uiState = screenUiState) {
        is AppDetailsUiState.Loaded -> {
            Column(
                modifier = modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // App information
                Box(modifier = Modifier.size(128.dp)) {
                    AppIcon(
                        iconUrl = uiState.appDetails.iconUrl,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                    )

                    when (uiState.progressToShow) {
                        is Progress.Determinate -> CircularProgressIndicator(
                            progress = { uiState.progressToShow.progress },
                            modifier = Modifier.fillMaxSize(),
                        )

                        Progress.Indeterminate ->
                            CircularProgressIndicator(modifier = Modifier.fillMaxSize())

                        null -> Unit
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.appDetails.name,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = uiState.appDetails.shortDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 40.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.version, uiState.appDetails.version),
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(modifier = Modifier.height(8.dp))

                // App status, e.g., errors and download progress
                uiState.getDisplayText()?.let { Text(text = it) }
                uiState.getErrorText()?.let { errorText ->
                    CloseableErrorBox(
                        errorText = errorText,
                        onClose = viewModel::clearInstallationResult,
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )
                }

                // Action buttons, e.g., install and update
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    val buttonModifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp)

                    for (button in uiState.buttonsToShow) {
                        when (button) {
                            is AppActionButton.Install -> Button(
                                onClick = viewModel::installApp,
                                modifier = buttonModifier,
                                enabled = button.enabled,
                            ) {
                                Text(stringResource(R.string.install))
                            }

                            is AppActionButton.Update -> Button(
                                onClick = viewModel::updateApp,
                                modifier = buttonModifier,
                                enabled = button.enabled,
                            ) {
                                Text(stringResource(R.string.update))
                            }

                            is AppActionButton.Open -> Button(
                                onClick = viewModel::openApp,
                                modifier = buttonModifier,
                                enabled = button.enabled,
                            ) {
                                Text(stringResource(R.string.open))
                            }

                            is AppActionButton.Unarchive -> Button(
                                onClick = viewModel::unarchiveApp,
                                modifier = buttonModifier,
                                enabled = button.enabled,
                            ) {
                                Text(stringResource(R.string.unarchive))
                            }

                            is AppActionButton.Enable -> Button(
                                onClick = viewModel::openAppDetailsSettings,
                                modifier = buttonModifier,
                                enabled = button.enabled,
                            ) {
                                Text(stringResource(R.string.enable))
                            }

                            is AppActionButton.Uninstall -> OutlinedButton(
                                onClick = viewModel::uninstallApp,
                                modifier = buttonModifier,
                                enabled = button.enabled,
                            ) {
                                Text(stringResource(R.string.uninstall))
                            }
                        }
                    }
                }
            }
        }

        AppDetailsUiState.Loading -> {
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .aspectRatio(1f),
                )
            }
        }

        is AppDetailsUiState.LoadingError -> {
            val (message, actionLabel, action) = when (uiState.error) {
                is AppDetailsLoadState.Error.AppNotFound -> Triple(
                    stringResource(R.string.cant_find_app),
                    stringResource(R.string.go_back),
                    onGoBack,
                )

                AppDetailsLoadState.Error.Internal -> Triple(
                    stringResource(R.string.internal_error),
                    stringResource(R.string.go_back),
                    onGoBack,
                )

                AppDetailsLoadState.Error.Network -> Triple(
                    stringResource(R.string.app_details_network_error),
                    stringResource(R.string.retry),
                    viewModel::loadData,
                )

                AppDetailsLoadState.Error.Timeout -> Triple(
                    stringResource(R.string.app_details_timeout_error),
                    stringResource(R.string.retry),
                    viewModel::loadData,
                )

                AppDetailsLoadState.Error.Unknown -> Triple(
                    stringResource(R.string.app_details_unknown_error),
                    stringResource(R.string.go_back),
                    onGoBack,
                )
            }

            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                ActionableCard(
                    bodyText = message,
                    actionText = actionLabel,
                    onActionClicked = action,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
            }
        }
    }
}
