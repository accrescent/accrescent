package app.accrescent.client.presentation.screens.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.work.NetworkType
import app.accrescent.client.R
import app.accrescent.client.data.DONATE_URL
import app.accrescent.client.data.SOURCE_CODE_URL
import app.accrescent.client.data.Theme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(modifier: Modifier = Modifier, viewModel: SettingsViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val dynamicColor by viewModel.dynamicColor.collectAsState(false)
    val theme by viewModel.theme.collectAsState(Theme.SYSTEM.name)
    val automaticUpdates by viewModel.automaticUpdates.collectAsState(true)
    val networkType by viewModel.updaterNetworkType.collectAsState(NetworkType.UNMETERED.name)

    Column(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SettingGroupLabel(stringResource(R.string.customization), Modifier.padding(top = 16.dp))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SwitchSetting(
                checked = dynamicColor,
                label = stringResource(R.string.dynamic_color),
                description = stringResource(R.string.dynamic_color_desc),
                onValueChange = {
                    coroutineScope.launch { viewModel.setDynamicColor(it) }
                },
            )
        }
        val themeNames = persistentListOf(
            stringResource(R.string.dark),
            stringResource(R.string.light),
            stringResource(R.string.system),
        )
        val themeValues = persistentListOf(Theme.DARK, Theme.LIGHT, Theme.SYSTEM)
        ListPreference(
            label = stringResource(R.string.theme),
            entries = themeNames,
            currentValueIndex = themeValues.indexOf(Theme.valueOf(theme)),
            onSelectionChanged = {
                coroutineScope.launch { viewModel.setTheme(themeValues[it]) }
            }
        )
        val networkTypeNames = persistentListOf(
            stringResource(R.string.any),
            stringResource(R.string.not_roaming),
            stringResource(R.string.unmetered),
        )
        val networkTypeValues = persistentListOf(
            NetworkType.CONNECTED,
            NetworkType.NOT_ROAMING,
            NetworkType.UNMETERED,
        )
        SettingGroupLabel(stringResource(R.string.updater), Modifier.padding(top = 16.dp))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SwitchSetting(
                checked = automaticUpdates,
                label = stringResource(R.string.automatic_updates),
                description = stringResource(R.string.automatic_updates_desc),
                onValueChange = {
                    coroutineScope.launch { viewModel.setAutomaticUpdates(it) }
                },
            )
        }
        ListPreference(
            label = stringResource(R.string.network_condition),
            entries = networkTypeNames,
            currentValueIndex = networkTypeValues.indexOf(NetworkType.valueOf(networkType)),
            onSelectionChanged = {
                coroutineScope.launch { viewModel.setUpdaterNetworkType(context, networkTypeValues[it]) }
            },
        )
        SettingGroupLabel(stringResource(R.string.about), Modifier.padding(top = 16.dp))
        ButtonSetting(
            label = stringResource(R.string.donate),
            description = stringResource(R.string.donate_desc),
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(DONATE_URL))
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                }
            }
        ) {
            Icon(Icons.AutoMirrored.Rounded.OpenInNew, stringResource(R.string.open_link))
        }
        ButtonSetting(
            label = stringResource(R.string.source_code),
            description = stringResource(R.string.source_code_desc),
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(SOURCE_CODE_URL))
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                }
            },
        ) {
            Icon(Icons.AutoMirrored.Rounded.OpenInNew, stringResource(R.string.open_link))
        }
    }
}

@Composable
fun SettingGroupLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier
            .semantics { heading() }
            .padding(horizontal = 8.dp),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge,
    )
}

@Composable
private fun Setting(
    label: String,
    description: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .padding(end = 8.dp)
                .weight(1f),
        ) {
            Text(label, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.bodyMedium, lineHeight = 16.sp)
        }
        content()
    }
}

/**
 * Modifier that wraps the existing Modifier with padding and rounded corners.
 *
 * @return A [Modifier] that adds padding and rounded corners to a composable.
 */
private fun Modifier.rounded(): Modifier =
    clip(RoundedCornerShape(8.dp))
        .then(this)
        .padding(8.dp)

@Composable
fun SwitchSetting(
    label: String,
    description: String,
    checked: Boolean,
    onValueChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Setting(
        modifier = modifier
            .toggleable(
                value = checked,
                onValueChange = onValueChange,
                role = Role.Switch,
            )
            .rounded(),
        label = label,
        description = description,
    ) {
        Switch(
            checked = checked,
            onCheckedChange = null,
        )
    }
}

@Composable
fun ButtonSetting(
    label: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Setting(
        modifier = modifier
            .clickable(
                onClick = onClick,
                role = Role.Button,
            )
            .rounded(),
        label = label,
        description = description,
        content = content,
    )
}

@Composable
fun RadioListItem(
    modifier: Modifier = Modifier,
    text: String = "",
    selected: Boolean = false,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .rounded(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
        RadioButton(selected = selected, onClick = null)
    }
}

@Composable
fun ListPreference(
    label: String,
    entries: ImmutableList<String>,
    currentValueIndex: Int,
    modifier: Modifier = Modifier,
    onSelectionChanged: (index: Int) -> Unit,
) {
    var showDialog by remember {
        mutableStateOf(false)
    }

    ButtonSetting(
        modifier = modifier,
        label = label,
        description = entries[currentValueIndex],
        onClick = { showDialog = true },
    ) {
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                },
                title = { Text(label) },
                text = {
                    Column (modifier = Modifier.selectableGroup()) {
                        entries.forEachIndexed { index, entry ->
                            RadioListItem(
                                modifier = Modifier.fillMaxWidth(),
                                text = entry,
                                selected = index == currentValueIndex,
                                onClick = {
                                    onSelectionChanged(index)
                                    showDialog = false
                                }
                            )
                        }
                    }
                },
            )
        }
    }
}
