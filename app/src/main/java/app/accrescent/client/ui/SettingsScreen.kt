package app.accrescent.client.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import app.accrescent.client.data.SOURCE_CODE_URL
import app.accrescent.client.util.isPrivileged
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(modifier: Modifier = Modifier, viewModel: SettingsViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val dynamicColor by viewModel.dynamicColor.collectAsState(false)
    val requireUserAction by viewModel.requireUserAction.collectAsState(!context.isPrivileged())
    val automaticUpdates by viewModel.automaticUpdates.collectAsState(true)
    val networkType by viewModel.updaterNetworkType.collectAsState(NetworkType.CONNECTED.name)

    Column(
        modifier
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        if (context.isPrivileged()) {
            SettingGroupLabel(stringResource(R.string.app_updates), Modifier.padding(top = 16.dp))
            Setting(
                label = stringResource(R.string.require_user_action),
                description = stringResource(R.string.require_user_action_desc),
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = requireUserAction,
                        role = Role.Switch,
                        onValueChange = { coroutineScope.launch { viewModel.setRequireUserAction(it) } }
                    )
            ) {
                Switch(
                    checked = requireUserAction,
                    onCheckedChange = null,
                )
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SettingGroupLabel(stringResource(R.string.customization), Modifier.padding(top = 16.dp))
            Setting(
                label = stringResource(R.string.dynamic_color),
                description = stringResource(R.string.dynamic_color_desc),
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = dynamicColor,
                        role = Role.Switch,
                        onValueChange = { coroutineScope.launch { viewModel.setDynamicColor(it) } }
                    )
            ) {
                Switch(
                    checked = dynamicColor,
                    onCheckedChange = null,
                )
            }
        }
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
            Setting(
                label = stringResource(R.string.automatic_updates),
                description = stringResource(R.string.automatic_updates_desc),
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = automaticUpdates,
                        role = Role.Switch,
                        onValueChange = { coroutineScope.launch { viewModel.setAutomaticUpdates(it) } }
                    )
            ) {
                Switch(
                    checked = automaticUpdates,
                    onCheckedChange = null,
                )
            }
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
        Setting(
            label = stringResource(R.string.source_code),
            description = stringResource(R.string.source_code_desc),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(SOURCE_CODE_URL))
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    }
                },
        ) {
            Icon(Icons.Rounded.OpenInNew, stringResource(R.string.open_link))
        }
    }
}

@Composable
fun SettingGroupLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.semantics { heading() },
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge,
    )
}

@Composable
fun Setting(
    label: String,
    description: String,
    modifier: Modifier = Modifier,
    fillMaxWidth: Boolean = false,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = (if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier.width(228.dp))
                .padding(vertical = 16.dp)
        ) {
            Text(label, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.bodyMedium, lineHeight = 16.sp)
        }
        content()
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
    val interactionSource = remember { MutableInteractionSource() }
    var showDialog by remember {
        mutableStateOf(false)
    }

    Setting(
        label = label,
        description = entries[currentValueIndex],
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) { showDialog = true },
        fillMaxWidth = true,
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
                    LazyColumn {
                        itemsIndexed(entries) { index, entry ->
                            Text(
                                text = if (index == currentValueIndex) "$entry   ✓" else entry,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable {
                                        onSelectionChanged(index)
                                        showDialog = false
                                    }
                                    .padding(horizontal = 15.dp, vertical = 12.dp),
                            )
                        }
                    }
                },
            )
        }
    }
}
