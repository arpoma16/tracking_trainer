package com.example.trianner4.ui.exercises

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.trianner4.data.local.ExerciseType
import com.example.trianner4.data.local.TrackingMode
import com.example.trianner4.data.local.entity.BiomechanicalChainEntity
import com.example.trianner4.data.local.entity.BodyZoneEntity
import com.example.trianner4.ui.exercises.components.BodyZoneSelector
import com.example.trianner4.ui.theme.Trianner4Theme

@Composable
fun ExerciseScreen(
    exerciseId: Long? = null,
    onBack: () -> Unit = {},
    viewModel: ExerciseViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(exerciseId) {
        viewModel.loadExercise(exerciseId)
    }

    ExerciseScreenContent(
        formState = formState,
        uiState = uiState,
        isEditing = exerciseId != null,
        onBack = onBack,
        onNameChanged = viewModel::onNameChanged,
        onTypeChanged = viewModel::onTypeChanged,
        onTrackingModeChanged = viewModel::onTrackingModeChanged,
        onDescriptionChanged = viewModel::onDescriptionChanged,
        onMediaRefChanged = viewModel::onMediaRefChanged,
        onPrimaryBodyZoneSelected = viewModel::onPrimaryBodyZoneSelected,
        onChainIdSelected = viewModel::onChainIdSelected,
        onDefaultSetsChanged = viewModel::onDefaultSetsChanged,
        onDefaultRepsChanged = viewModel::onDefaultRepsChanged,
        onDefaultRirChanged = viewModel::onDefaultRirChanged,
        onDefaultDurationChanged = viewModel::onDefaultDurationChanged,
        onSaveExercise = {
            viewModel.saveExercise(onSuccess = onBack)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseScreenContent(
    formState: ExerciseFormState,
    uiState: ExerciseUiState,
    isEditing: Boolean,
    onBack: () -> Unit,
    onNameChanged: (String) -> Unit,
    onTypeChanged: (ExerciseType) -> Unit,
    onTrackingModeChanged: (TrackingMode) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onMediaRefChanged: (String?) -> Unit,
    onPrimaryBodyZoneSelected: (Long?) -> Unit,
    onChainIdSelected: (Long?) -> Unit,
    onDefaultSetsChanged: (String) -> Unit,
    onDefaultRepsChanged: (String) -> Unit,
    onDefaultRirChanged: (String) -> Unit,
    onDefaultDurationChanged: (String) -> Unit,
    onSaveExercise: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (isEditing) "Edit Exercise" else "Add New Exercise") 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = onSaveExercise,
                        enabled = formState.isFormValid
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        // ... rest of the column content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (val state = uiState) {
                is ExerciseUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is ExerciseUiState.Error -> {
                    Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
                is ExerciseUiState.Ready -> {
                    ExerciseFormContent(
                        formState = formState,
                        uiState = state,
                        onNameChanged = onNameChanged,
                        onTypeChanged = onTypeChanged,
                        onTrackingModeChanged = onTrackingModeChanged,
                        onDescriptionChanged = onDescriptionChanged,
                        onMediaRefChanged = onMediaRefChanged,
                        onPrimaryBodyZoneSelected = onPrimaryBodyZoneSelected,
                        onChainIdSelected = onChainIdSelected,
                        onDefaultSetsChanged = onDefaultSetsChanged,
                        onDefaultRepsChanged = onDefaultRepsChanged,
                        onDefaultRirChanged = onDefaultRirChanged,
                        onDefaultDurationChanged = onDefaultDurationChanged
                    )
                }
                is ExerciseUiState.Success -> {
                    Text("Success: ${state.message}")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseFormContent(
    formState: ExerciseFormState,
    uiState: ExerciseUiState.Ready,
    onNameChanged: (String) -> Unit,
    onTypeChanged: (ExerciseType) -> Unit,
    onTrackingModeChanged: (TrackingMode) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onMediaRefChanged: (String?) -> Unit,
    onPrimaryBodyZoneSelected: (Long?) -> Unit,
    onChainIdSelected: (Long?) -> Unit,
    onDefaultSetsChanged: (String) -> Unit,
    onDefaultRepsChanged: (String) -> Unit,
    onDefaultRirChanged: (String) -> Unit,
    onDefaultDurationChanged: (String) -> Unit
) {
    OutlinedTextField(
        value = formState.name,
        onValueChange = onNameChanged,
        label = { Text("Exercise Name*") },
        modifier = Modifier.fillMaxWidth()
    )

    // Type Selector
    var typeExpanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = typeExpanded,
        onExpandedChange = { typeExpanded = !typeExpanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = formState.type?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Type*") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = typeExpanded,
            onDismissRequest = { typeExpanded = false })
        {
            ExerciseType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.name) },
                    onClick = {
                        onTypeChanged(type)
                        typeExpanded = false
                    }
                )
            }
        }
    }

    // Default Metrics Section
    Text("Default Metrics", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())

    if (formState.type == ExerciseType.STRENGTH) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = formState.defaultSets,
                onValueChange = onDefaultSetsChanged,
                label = { Text("Sets") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = formState.defaultReps,
                onValueChange = onDefaultRepsChanged,
                label = { Text("Reps") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = formState.defaultRir,
                onValueChange = onDefaultRirChanged,
                label = { Text("RIR") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = formState.defaultDurationSec,
                onValueChange = onDefaultDurationChanged,
                label = { Text("Duration (sec)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = formState.defaultReps,
                onValueChange = onDefaultRepsChanged,
                label = { Text("Reps") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    }

    // Tracking Mode Selector
    var trackingModeExpanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = trackingModeExpanded,
        onExpandedChange = { trackingModeExpanded = !trackingModeExpanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = formState.trackingMode?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Tracking Mode*") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = trackingModeExpanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = trackingModeExpanded,
            onDismissRequest = { trackingModeExpanded = false })
        {
            TrackingMode.entries.forEach { mode ->
                DropdownMenuItem(
                    text = { Text(mode.name) },
                    onClick = {
                        onTrackingModeChanged(mode)
                        trackingModeExpanded = false
                    }
                )
            }
        }
    }

    OutlinedTextField(
        value = formState.description,
        onValueChange = onDescriptionChanged,
        label = { Text("Description") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3
    )

    OutlinedTextField(
        value = formState.mediaRef ?: "",
        onValueChange = onMediaRefChanged,
        label = { Text("Media Reference (URL or file path)") },
        modifier = Modifier.fillMaxWidth()
    )

    // Primary Body Zone Selector
    BodyZoneSelector(
        bodyZones = uiState.bodyZones,
        selectedBodyZoneId = formState.primaryBodyZoneId,
        onBodyZoneSelected = { id, _ -> onPrimaryBodyZoneSelected(id) }
    )

    // Chain ID Selector
    var chainIdExpanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = chainIdExpanded,
        onExpandedChange = { chainIdExpanded = !chainIdExpanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = formState.chainId?.let { id ->
                uiState.biomechanicalChains.find { it.id == id }?.name ?: "Select Chain"
            } ?: "Select Chain",
            onValueChange = {},
            readOnly = true,
            label = { Text("Biomechanical Chain") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = chainIdExpanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = chainIdExpanded,
            onDismissRequest = { chainIdExpanded = false })
        {
            uiState.biomechanicalChains.forEach { chain ->
                DropdownMenuItem(
                    text = { Text(chain.name) },
                    onClick = {
                        onChainIdSelected(chain.id)
                        chainIdExpanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExerciseScreenPreview() {
    Trianner4Theme {
        ExerciseScreenContent(
            formState = ExerciseFormState(
                name = "Push Up",
                type = ExerciseType.STRENGTH,
                trackingMode = TrackingMode.WEIGHT_REPS,
                isFormValid = true
            ),
            uiState = ExerciseUiState.Ready(
                bodyZones = listOf(
                    BodyZoneEntity(id = 1, name = "Chest", level = 1, parentId = null)
                ),
                biomechanicalChains = listOf(
                    BiomechanicalChainEntity(id = 1, name = "Horizontal Push")
                )
            ),
            isEditing = false,
            onNameChanged = {},
            onTypeChanged = {},
            onTrackingModeChanged = {},
            onDescriptionChanged = {},
            onMediaRefChanged = {},
            onPrimaryBodyZoneSelected = {},
            onChainIdSelected = {},
            onDefaultSetsChanged = {},
            onDefaultRepsChanged = {},
            onDefaultRirChanged = {},
            onDefaultDurationChanged = {},
            onSaveExercise = {},
            onBack = {}
        )
    }
}
