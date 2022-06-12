package app.accrescent.client.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import app.accrescent.client.R
import app.accrescent.client.data.InstallStatus
import app.accrescent.client.data.db.App

@Composable
fun InstallableAppCard(
    app: App,
    navController: NavController,
    installStatus: InstallStatus,
    onInstallClicked: (String) -> Unit,
    onOpenClicked: (String) -> Unit,
) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { navController.navigate("${Screen.AppDetails.route}/${app.id}") },
        backgroundColor = MaterialTheme.colors.primary,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                app.name,
                modifier = Modifier.padding(start = 16.dp),
                style = MaterialTheme.typography.h4,
            )
            Button(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                onClick = {
                    when (installStatus) {
                        InstallStatus.INSTALLABLE,
                        InstallStatus.UPDATABLE -> onInstallClicked(app.id)
                        InstallStatus.INSTALLED -> onOpenClicked(app.id)
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primaryVariant),
            ) {
                when (installStatus) {
                    InstallStatus.INSTALLABLE ->
                        Text(stringResource(R.string.install), color = Color.LightGray)
                    InstallStatus.UPDATABLE ->
                        Text(stringResource(R.string.update), color = Color.LightGray)
                    InstallStatus.INSTALLED ->
                        Text(stringResource(R.string.open), color = Color.LightGray)
                }
            }
        }
    }
}
