package com.example.trianner4.ui.ajustes

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.trianner4.R
import com.example.trianner4.ui.theme.Trianner4Theme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// ── Pantalla principal: Ajustes ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    onOpenPerfil: () -> Unit = {},
    onOpenTriggers: () -> Unit = {},
    onOpenBackup: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.nav_settings)) })
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            item {
                SettingsSectionLabel(stringResource(R.string.settings_section_account))
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Person,
                    label = stringResource(R.string.settings_profile),
                    onClick = onOpenPerfil,
                )
            }
            item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }
            item {
                SettingsSectionLabel(stringResource(R.string.settings_section_training))
            }
            item {
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.TrendingFlat,
                    label = stringResource(R.string.settings_progression),
                    onClick = onOpenTriggers,
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Default.AcUnit,
                    label = stringResource(R.string.settings_freeze),
                    onClick = {},
                )
            }
            item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }
            item {
                SettingsSectionLabel(stringResource(R.string.settings_section_data))
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Backup,
                    label = stringResource(R.string.settings_backup),
                    onClick = onOpenBackup,
                )
            }
            item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }
            item {
                SettingsSectionLabel(stringResource(R.string.settings_section_about))
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    label = stringResource(R.string.settings_about),
                    onClick = {},
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionLabel(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 4.dp),
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ── Sub-pantallas ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(onBack: () -> Unit = {}) {
    SubScreen(title = stringResource(R.string.settings_profile), onBack = onBack) {
        Text(
            text = stringResource(R.string.placeholder_coming_soon),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriggersProgresoScreen(onBack: () -> Unit = {}) {
    SubScreen(title = stringResource(R.string.settings_progression), onBack = onBack) {
        Text(
            text = stringResource(R.string.placeholder_coming_soon),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    onBack: () -> Unit = {},
    viewModel: AjustesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var jsonToWrite by remember { mutableStateOf<String?>(null) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            jsonToWrite?.let { json ->
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(json.toByteArray())
                }
                jsonToWrite = null // Limpiar después de escribir
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.backupJson.collect { json ->
            jsonToWrite = json
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))
            createDocumentLauncher.launch("trianner_backup_$timestamp.json")
        }
    }

    SubScreen(title = stringResource(R.string.settings_backup), onBack = onBack) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Backup,
                contentDescription = null,
                modifier = Modifier.padding(bottom = 16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.backup_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            Button(
                onClick = { viewModel.onExportBackup() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.backup_action_export))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubScreen(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun AjustesScreenPreview() {
    Trianner4Theme { AjustesScreen() }
}
