package com.example.trianner4.ui.bienestar.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.trianner4.data.local.entity.BodyZoneEntity
import com.example.trianner4.ui.bienestar.DiscomfortFormState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscomfortFormBottomSheet(
    sheetState: androidx.compose.material3.SheetState,
    onDismissRequest: () -> Unit,
    onSave: () -> Unit,
    formState: DiscomfortFormState,
    bodyZones: List<BodyZoneEntity>,
    onBodyZoneSelected: (Long, String) -> Unit,
    onSeverityChanged: (Int) -> Unit,
    onDescriptionChanged: (String) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (formState.editingDiscomfortId == null) "Registrar Nueva Molestia" else "Editar Molestia",
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            BodyZoneSelector(
                bodyZones = bodyZones,
                selectedBodyZoneId = formState.selectedBodyZoneId,
                onBodyZoneSelected = onBodyZoneSelected
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Severidad: ${formState.severity}",
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium
            )
            Slider(
                value = formState.severity.toFloat(),
                onValueChange = { onSeverityChanged(it.toInt()) },
                valueRange = 0f..5f,
                steps = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = formState.description,
                onValueChange = onDescriptionChanged,
                label = { Text("Descripción (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSave,
                enabled = formState.selectedBodyZoneId != null && formState.severity > 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Molestia")
            }
        }
    }
}

@Composable
fun BodyZoneSelector(
    bodyZones: List<BodyZoneEntity>,
    selectedBodyZoneId: Long?,
    onBodyZoneSelected: (Long, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rootZones = bodyZones.filter { it.parentId == null }

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedBodyZoneId?.let { id ->
                        bodyZones.find { it.id == id }?.name ?: "Seleccionar Zona del Cuerpo"
                    } ?: "Seleccionar Zona del Cuerpo"
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Contraer" else "Expandir"
                )
            }
        }

        if (expanded) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(vertical = 4.dp)
            ) {
                items(rootZones) { rootZone ->
                    BodyZoneItem(
                        bodyZone = rootZone,
                        bodyZones = bodyZones,
                        selectedBodyZoneId = selectedBodyZoneId,
                        onBodyZoneSelected = { id, name ->
                            onBodyZoneSelected(id, name)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BodyZoneItem(
    bodyZone: BodyZoneEntity,
    bodyZones: List<BodyZoneEntity>,
    selectedBodyZoneId: Long?,
    onBodyZoneSelected: (Long, String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val childZones = bodyZones.filter { it.parentId == bodyZone.id }

    Column {
        ListItem(
            headlineContent = { Text(bodyZone.name) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onBodyZoneSelected(bodyZone.id, bodyZone.name)
                },
            trailingContent = if (childZones.isNotEmpty()) {
                {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Contraer" else "Expandir",
                        modifier = Modifier.clickable { isExpanded = !isExpanded }
                    )
                }
            } else null
        )
        if (isExpanded && childZones.isNotEmpty()) {
            Column(modifier = Modifier.padding(start = 16.dp)) {
                childZones.forEach { childZone ->
                    BodyZoneItem(
                        bodyZone = childZone,
                        bodyZones = bodyZones,
                        selectedBodyZoneId = selectedBodyZoneId,
                        onBodyZoneSelected = onBodyZoneSelected
                    )
                }
            }
        }
    }
}
