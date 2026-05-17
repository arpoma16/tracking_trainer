package com.example.trianner4.ui.bienestar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.trianner4.data.local.entity.DiscomfortEntity
import com.example.trianner4.ui.bienestar.components.DiscomfortFormBottomSheet
import com.example.trianner4.ui.theme.Trianner4Theme
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BienestarScreen(
    viewModel: BienestarViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val discomfortFormState by viewModel.discomfortFormState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    fun showAddDiscomfortSheet() {
        viewModel.showDiscomfortForm(true)
        scope.launch { sheetState.show() }
    }

    fun hideAddDiscomfortSheet() {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                viewModel.showDiscomfortForm(false)
            }
        }
    }

    BienestarScreenContent(
        uiState = uiState,
        discomfortFormState = discomfortFormState,
        sheetState = sheetState,
        onShowAddSheet = { showAddDiscomfortSheet() },
        onHideSheet = { hideAddDiscomfortSheet() },
        onSaveDiscomfort = { viewModel.saveDiscomfort() },
        onResolveDiscomfort = { viewModel.resolveDiscomfort(it) },
        onReactivateDiscomfort = { viewModel.reactivateDiscomfort(it) },
        onEditDiscomfort = { dwt ->
            viewModel.editDiscomfort(dwt)
            scope.launch { sheetState.show() }
        },
        onBodyZoneSelected = viewModel::onBodyZoneSelected,
        onSeverityChanged = viewModel::onSeverityChanged,
        onDescriptionChanged = viewModel::onDescriptionChanged
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BienestarScreenContent(
    uiState: BienestarUiState,
    discomfortFormState: DiscomfortFormState,
    sheetState: SheetState,
    onShowAddSheet: () -> Unit,
    onHideSheet: () -> Unit,
    onSaveDiscomfort: () -> Unit,
    onResolveDiscomfort: (Long) -> Unit,
    onReactivateDiscomfort: (Long) -> Unit,
    onEditDiscomfort: (DiscomfortWithTags) -> Unit,
    onBodyZoneSelected: (Long, String) -> Unit,
    onSeverityChanged: (Int) -> Unit,
    onDescriptionChanged: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Bienestar") })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onShowAddSheet,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nueva molestia") },
            )
        },
    ) { innerPadding ->
        when (val state = uiState) {
            BienestarUiState.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            is BienestarUiState.Error -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            is BienestarUiState.Ready -> {
                if (state.activeDiscomforts.isEmpty() && state.resolvedDiscomforts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sin molestias registradas.\nUsa el botón + para añadir una.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (state.activeDiscomforts.isNotEmpty()) {
                            item {
                                SectionLabel("Activas (${state.activeDiscomforts.size})", color = MaterialTheme.colorScheme.error)
                            }
                            items(state.activeDiscomforts, key = { it.discomfort.id }) { dwt ->
                                DiscomfortCard(
                                    dwt = dwt,
                                    onResolve = { onResolveDiscomfort(dwt.discomfort.id) },
                                    onClick = { onEditDiscomfort(dwt) }
                                )
                            }
                        }

                        if (state.resolvedDiscomforts.isNotEmpty()) {
                            item {
                                SectionLabel("Resueltas (${state.resolvedDiscomforts.size})", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            items(state.resolvedDiscomforts, key = { it.discomfort.id }) { dwt ->
                                DiscomfortCard(
                                    dwt = dwt,
                                    onReactivate = { onReactivateDiscomfort(dwt.discomfort.id) },
                                    onClick = { onEditDiscomfort(dwt) }
                                )
                            }
                        }

                        if (state.adaptationHistory.isNotEmpty()) {
                            item {
                                SectionLabel("Historial de adaptaciones (${state.adaptationHistory.size})")
                            }
                            items(state.adaptationHistory, key = { it.id }) { log ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Text(
                                        text = "Sesión #${log.sessionId} · ${log.actionType.name.lowercase()}",
                                        modifier = Modifier.padding(12.dp),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (discomfortFormState.isSheetOpen) {
        DiscomfortFormBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onHideSheet,
            onSave = onSaveDiscomfort,
            formState = discomfortFormState,
            bodyZones = (uiState as? BienestarUiState.Ready)?.bodyZones ?: emptyList(),
            onBodyZoneSelected = onBodyZoneSelected,
            onSeverityChanged = onSeverityChanged,
            onDescriptionChanged = onDescriptionChanged
        )
    }
}

// ── Tarjeta de molestia ────────────────────────────────────────────────────────

@Composable
private fun DiscomfortCard(
    dwt: DiscomfortWithTags,
    onResolve: (() -> Unit)? = null,
    onReactivate: (() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    val d = dwt.discomfort
    val isActive = d.isActive
    val severityColor = when {
        d.severity >= 4 -> MaterialTheme.colorScheme.error
        d.severity >= 3 -> Color(0xFFE65100)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.surface
                             else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = d.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Severidad ${d.severity}/5",
                        style = MaterialTheme.typography.labelSmall,
                        color = severityColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                d.freeText?.takeIf { it.isNotBlank() }?.let { notes ->
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
                val dateStr = Instant.ofEpochMilli(d.startedAt)
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault()))
                Text(
                    text = "Desde $dateStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (dwt.tags.isNotEmpty()) {
                    Text(
                        text = dwt.tags.joinToString(", ") { it.name },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (onResolve != null) {
                IconButton(onClick = onResolve) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Marcar como resuelta",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (onReactivate != null) {
                IconButton(onClick = onReactivate) {
                    Icon(
                        Icons.Default.Circle,
                        contentDescription = "Reactivar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

// ── Previews ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun BienestarScreenReadyPreview() {
    val mockDiscomfort = DiscomfortEntity(
        id = 1,
        bodyZoneId = 1,
        label = "Dolor de Rodilla",
        freeText = "Molestia al bajar escaleras",
        severity = 3,
        startedAt = System.currentTimeMillis(),
        resolvedAt = null,
        isActive = true
    )
    val mockState = BienestarUiState.Ready(
        activeDiscomforts = listOf(DiscomfortWithTags(mockDiscomfort, emptyList())),
        resolvedDiscomforts = emptyList(),
        adaptationHistory = emptyList()
    )
    Trianner4Theme {
        BienestarScreenContent(
            uiState = mockState,
            discomfortFormState = DiscomfortFormState(),
            sheetState = rememberModalBottomSheetState(),
            onShowAddSheet = {},
            onHideSheet = {},
            onSaveDiscomfort = {},
            onResolveDiscomfort = {},
            onReactivateDiscomfort = {},
            onEditDiscomfort = {},
            onBodyZoneSelected = { _, _ -> },
            onSeverityChanged = {},
            onDescriptionChanged = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun BienestarScreenEmptyPreview() {
    Trianner4Theme {
        BienestarScreenContent(
            uiState = BienestarUiState.Ready(emptyList(), emptyList(), emptyList()),
            discomfortFormState = DiscomfortFormState(),
            sheetState = rememberModalBottomSheetState(),
            onShowAddSheet = {},
            onHideSheet = {},
            onSaveDiscomfort = {},
            onResolveDiscomfort = {},
            onReactivateDiscomfort = {},
            onEditDiscomfort = {},
            onBodyZoneSelected = { _, _ -> },
            onSeverityChanged = {},
            onDescriptionChanged = {}
        )
    }
}
