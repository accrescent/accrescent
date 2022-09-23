package app.accrescent.client.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.accrescent.client.R
import app.accrescent.client.data.SOURCE_CODE_URL
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(padding: PaddingValues, viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val dynamicColor by viewModel.dynamicColor.collectAsState(false)

    Column(
        modifier = Modifier
            .padding(padding)
            .padding(horizontal = 32.dp)
    ) {
        SettingGroupLabel(stringResource(R.string.customization))
        Setting(
            label = stringResource(R.string.dynamic_color),
            description = stringResource(R.string.dynamic_color_desc),
        ) {
            Switch(
                checked = dynamicColor,
                onCheckedChange = { coroutineScope.launch { viewModel.setDynamicColor(it) } },
            )
        }
        SettingGroupLabel(stringResource(R.string.about))
        Setting(
            label = stringResource(R.string.source_code),
            description = stringResource(R.string.source_code_desc),
        ) {
            IconButton(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(SOURCE_CODE_URL))
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                }
            }) {
                Icon(Icons.Rounded.OpenInNew, stringResource(R.string.open_link))
            }
        }
    }
}

@Composable
fun SettingGroupLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(top = 16.dp),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge,
    )
}

@Composable
fun Setting(label: String, description: String, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .width(228.dp)
                .padding(vertical = 16.dp)
        ) {
            Text(label, fontWeight = FontWeight.Bold)
            Text(description, fontSize = 14.sp)
        }
        content()
    }
}
