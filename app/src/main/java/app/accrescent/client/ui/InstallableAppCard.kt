package app.accrescent.client.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.accrescent.client.BuildConfig
import app.accrescent.client.R
import app.accrescent.client.data.InstallStatus
import app.accrescent.client.data.db.App
import app.accrescent.client.util.isPrivileged

@Composable
fun InstallableAppCard(
    app: App,
    installStatus: InstallStatus,
    onClick: () -> Unit,
    onInstallClicked: (String, requireUserAction: Boolean) -> Unit,
    onUninstallClicked: (String) -> Unit,
    onOpenClicked: (String) -> Unit,
    requireUserAction: Boolean = false,
) {
    val context = LocalContext.current

    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                app.name,
                modifier = Modifier.padding(start = 16.dp),
                style = MaterialTheme.typography.headlineSmall,
            )
            Row {
                when (installStatus) {
                    InstallStatus.INSTALLED,
                    InstallStatus.UPDATABLE ->
                        // We can't uninstall ourselves if we're a priv-app
                        if (!(context.isPrivileged() && app.id == BuildConfig.APPLICATION_ID)) {
                            OutlinedButton(
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp),
                                onClick = { onUninstallClicked(app.id) },
                            ) {
                                Text(stringResource(R.string.uninstall))
                            }
                        }
                    else -> Unit
                }
                Button(
                    modifier = Modifier.padding(end = 16.dp, top = 12.dp, bottom = 12.dp),
                    onClick = {
                        when (installStatus) {
                            InstallStatus.INSTALLABLE -> onInstallClicked(app.id, requireUserAction)
                            InstallStatus.UPDATABLE -> onInstallClicked(app.id, false)
                            InstallStatus.INSTALLED -> onOpenClicked(app.id)
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
    }
}
