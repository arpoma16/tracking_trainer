package com.example.trianner4.ui.exercises.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.trianner4.data.local.entity.BodyZoneEntity

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
                        bodyZones.find { it.id == id }?.name ?: "Select Body Zone"
                    } ?: "Select Body Zone"
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
        }

        if (expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                rootZones.forEach {
                    BodyZoneItem(
                        bodyZone = it,
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
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
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
