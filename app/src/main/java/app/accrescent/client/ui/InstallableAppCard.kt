package app.accrescent.client.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.accrescent.client.R
import app.accrescent.client.data.InstallStatus
import app.accrescent.client.data.db.App

@Composable
fun InstallableAppCard(
    app: App,
    installStatus: InstallStatus,
    onClick: () -> Unit,
    onInstallClicked: (String) -> Unit,
    onUninstallClicked: (String) -> Unit,
    onOpenClicked: (String) -> Unit,
) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        backgroundColor = MaterialTheme.colorScheme.primary,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                app.name,
                modifier = Modifier.padding(start = 16.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.headlineSmall,
            )
            Row {
                when (installStatus) {
                    InstallStatus.INSTALLED,
                    InstallStatus.UPDATABLE ->
                        Button(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp),
                            onClick = { onUninstallClicked(app.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        ) {
                            Text(stringResource(R.string.uninstall))
                        }
                    else -> Unit
                }
                Button(
                    modifier = Modifier.padding(end = 16.dp, top = 12.dp, bottom = 12.dp),
                    onClick = {
                        when (installStatus) {
                            InstallStatus.INSTALLABLE,
                            InstallStatus.UPDATABLE -> onInstallClicked(app.id)
                            InstallStatus.INSTALLED -> onOpenClicked(app.id)
                            InstallStatus.LOADING,
                            InstallStatus.UNKNOWN -> Unit
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
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
