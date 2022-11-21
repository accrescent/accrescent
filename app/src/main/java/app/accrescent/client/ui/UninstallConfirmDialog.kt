package app.accrescent.client.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import app.accrescent.client.R

@Composable
fun UninstallConfirmDialog(
    appId: String,
    onDismiss: () -> Unit,
    onConfirm: (appId: String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.uninstall_confirm)) },
        text = {
            Text(stringResource(R.string.uninstall_confirm_desc), fontFamily = FontFamily.Default)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(appId)
                    onDismiss()
                },
                content = { Text(stringResource(R.string.yes)) },
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                content = { Text(stringResource(R.string.no)) },
            )
        },
    )
}
