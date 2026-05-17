package com.example.trianner4.ui.rutinas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.trianner4.R
import com.example.trianner4.data.local.ScheduleType
import com.example.trianner4.ui.theme.Trianner4Theme
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.trianner4.data.local.ExerciseType
import com.example.trianner4.data.local.Phase
import com.example.trianner4.data.local.TagCategory
import com.example.trianner4.data.local.TrackingMode
import com.example.trianner4.data.local.entity.ExerciseEntity
import com.example.trianner4.data.local.entity.RoutineEntity
import com.example.trianner4.data.local.entity.TagEntity
import com.example.trianner4.data.local.model.RoutinePhaseExerciseWithExercise

// ── Pantalla principal: Lista de rutinas ────────────────────────────────────────

@Composable
fun RutinasScreen(
    onCreateRoutine: () -> Unit = {},
    onEditRoutine: (Long) -> Unit = {},
    onOpenBiblioteca: () -> Unit = {},
    viewModel: RutinasViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RutinasScreenContent(
        uiState = uiState,
        onCreateRoutine = onCreateRoutine,
        onEditRoutine = onEditRoutine,
        onOpenBiblioteca = onOpenBiblioteca,
        onToggleActive = { viewModel.toggleActive(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RutinasScreenContent(
    uiState: RutinasUiState,
    onCreateRoutine: () -> Unit,
    onEditRoutine: (Long) -> Unit,
    onOpenBiblioteca: () -> Unit,
    onToggleActive: (RoutineEntity) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.nav_routines)) })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateRoutine,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.routines_fab_create)) },
            )
        },
    ) { innerPadding ->
        when (val state = uiState) {
            RutinasUiState.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            is RutinasUiState.Error -> Box(
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

            is RutinasUiState.Ready -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        OutlinedButton(
                            onClick = onOpenBiblioteca,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.routines_biblioteca))
                        }
                    }

                    if (state.routines.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = stringResource(R.string.routines_empty),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    } else {
                        items(state.routines, key = { it.routine.id }) { rws ->
                            RoutineCard(
                                rws = rws,
                                onEdit = { onEditRoutine(rws.routine.id) },
                                onToggleActive = { onToggleActive(rws.routine) },
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Tarjeta de rutina ──────────────────────────────────────────────────────────

@Composable
private fun RoutineCard(
    rws: RoutineWithSchedule,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (rws.routine.isActive) MaterialTheme.colorScheme.surface
                             else MaterialTheme.colorScheme.surfaceVariant
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = rws.routine.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (!rws.routine.isActive) {
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "Inactiva",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (rws.routine.description.isNotBlank()) {
                    Text(
                        text = rws.routine.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
                if (rws.schedules.isNotEmpty()) {
                    Text(
                        text = scheduleLabel(rws),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar ${rws.routine.name}",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun scheduleLabel(rws: RoutineWithSchedule): String {
    val schedule = rws.schedules.firstOrNull() ?: return ""
    return when (schedule.scheduleType) {
        ScheduleType.WEEKDAYS -> {
            val mask = schedule.weekdaysMask ?: 0
            val dayLetters = listOf("L", "M", "X", "J", "V", "S", "D")
            dayLetters.filterIndexed { i, _ -> (mask and (1 shl i)) != 0 }.joinToString(" ")
        }
        ScheduleType.EVERY_N_DAYS -> "Cada ${schedule.everyNDays ?: "?"} días"
        ScheduleType.CUSTOM -> "Personalizado"
    }
}

// ── Editor de rutina ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RutinaEditorScreen(
    rutinaId: Long?,
    onBack: () -> Unit = {},
    viewModel: RutinaEditorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(rutinaId) {
        viewModel.loadRoutine(rutinaId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (rutinaId == null) stringResource(R.string.routines_new_title)
                        else stringResource(R.string.routines_edit_title)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
        floatingActionButton = {
            Button(
                onClick = { viewModel.saveRoutine(onSuccess = onBack) },
                enabled = uiState.name.isNotBlank() && !uiState.isSaving
            ) {
                if (uiState.isSaving) CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                Text(stringResource(R.string.action_save))
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text(stringResource(R.string.routines_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.frequencyDays.toString(),
                        onValueChange = { viewModel.onFrequencyChange(it.toIntOrNull() ?: 1) },
                        label = { Text(stringResource(R.string.routines_frequency_label)) },
                        modifier = Modifier.width(120.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Text(
                        text = stringResource(R.string.routines_frequency_every, uiState.frequencyDays),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            item {
                PhaseSection(
                    label = stringResource(R.string.phase_pre),
                    exercises = uiState.preExercises,
                    onAddExercise = { viewModel.openExercisePicker(Phase.PRE) },
                    onRemoveExercise = { viewModel.removeExercise(it) },
                    onUpdateConfig = { ex, s, r, rir, d -> viewModel.updateExerciseConfig(ex, s, r, rir, d) }
                )
            }

            item {
                PhaseSection(
                    label = stringResource(R.string.phase_core),
                    exercises = uiState.coreExercises,
                    onAddExercise = { viewModel.openExercisePicker(Phase.CORE) },
                    onRemoveExercise = { viewModel.removeExercise(it) },
                    onUpdateConfig = { ex, s, r, rir, d -> viewModel.updateExerciseConfig(ex, s, r, rir, d) }
                )
            }

            item {
                PhaseSection(
                    label = stringResource(R.string.phase_post),
                    exercises = uiState.postExercises,
                    onAddExercise = { viewModel.openExercisePicker(Phase.POST) },
                    onRemoveExercise = { viewModel.removeExercise(it) },
                    onUpdateConfig = { ex, s, r, rir, d -> viewModel.updateExerciseConfig(ex, s, r, rir, d) }
                )
            }
        }
    }

    if (uiState.showExercisePicker != null) {
        ExercisePickerDialog(
            onDismiss = viewModel::closeExercisePicker,
            onExerciseSelected = { viewModel.addExerciseToPhase(it, uiState.showExercisePicker!!) }
        )
    }
}

@Composable
private fun PhaseSection(
    label: String,
    exercises: List<RoutinePhaseExerciseWithExercise>,
    onAddExercise: () -> Unit,
    onRemoveExercise: (RoutinePhaseExerciseWithExercise) -> Unit,
    onUpdateConfig: (RoutinePhaseExerciseWithExercise, Int?, Int?, Int?, Int?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            TextButton(onClick = onAddExercise) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.routines_add_exercise))
            }
        }
        
        if (exercises.isEmpty()) {
            Text(
                text = "Sin ejercicios",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            exercises.forEach { ex ->
                var showEditDialog by remember { mutableStateOf(false) }
                
                if (showEditDialog) {
                    ExerciseConfigDialog(
                        exerciseWithConfig = ex,
                        onDismiss = { showEditDialog = false },
                        onConfirm = { s, r, rir, d ->
                            onUpdateConfig(ex, s, r, rir, d)
                            showEditDialog = false
                        }
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                        ListItem(
                            headlineContent = { Text(ex.exercise.name) },
                            supportingContent = {
                                Column {
                                    Text(ex.exercise.type.name)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        TextButton(
                                            onClick = { showEditDialog = true },
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            if (ex.routinePhaseExercise.phase == Phase.CORE) {
                                                Text("${ex.routinePhaseExercise.targetSets ?: ex.exercise.defaultSets ?: 0}x${ex.routinePhaseExercise.targetReps ?: ex.exercise.defaultReps ?: 0} @RIR ${ex.routinePhaseExercise.targetRir ?: ex.exercise.defaultRir ?: 0}")
                                            } else {
                                                if (ex.routinePhaseExercise.targetDurationSec != null || ex.exercise.defaultDurationSec != null) {
                                                    Text("${ex.routinePhaseExercise.targetDurationSec ?: ex.exercise.defaultDurationSec}s")
                                                } else {
                                                    Text("${ex.routinePhaseExercise.targetReps ?: ex.exercise.defaultReps ?: 0} reps")
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            trailingContent = {
                                IconButton(onClick = { onRemoveExercise(ex) }) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove")
                                }
                            }
                        )
                }
            }
        }
    }
}

@Composable
fun ExerciseConfigDialog(
    exerciseWithConfig: RoutinePhaseExerciseWithExercise,
    onDismiss: () -> Unit,
    onConfirm: (Int?, Int?, Int?, Int?) -> Unit
) {
    var sets by remember { mutableStateOf(exerciseWithConfig.routinePhaseExercise.targetSets?.toString() ?: exerciseWithConfig.exercise.defaultSets?.toString() ?: "") }
    var reps by remember { mutableStateOf(exerciseWithConfig.routinePhaseExercise.targetReps?.toString() ?: exerciseWithConfig.exercise.defaultReps?.toString() ?: "") }
    var rir by remember { mutableStateOf(exerciseWithConfig.routinePhaseExercise.targetRir?.toString() ?: exerciseWithConfig.exercise.defaultRir?.toString() ?: "") }
    var duration by remember { mutableStateOf(exerciseWithConfig.routinePhaseExercise.targetDurationSec?.toString() ?: exerciseWithConfig.exercise.defaultDurationSec?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure ${exerciseWithConfig.exercise.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (exerciseWithConfig.routinePhaseExercise.phase == Phase.CORE) {
                    OutlinedTextField(value = sets, onValueChange = { sets = it }, label = { Text("Sets") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = reps, onValueChange = { reps = it }, label = { Text("Reps") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = rir, onValueChange = { rir = it }, label = { Text("Target RIR") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                } else {
                    OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Duration (sec)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = reps, onValueChange = { reps = it }, label = { Text("Reps") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(sets.toIntOrNull(), reps.toIntOrNull(), rir.toIntOrNull(), duration.toIntOrNull()) }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ── Biblioteca de ejercicios ────────────────────────────────────────────────────

@Composable
fun BibliotecaEjerciciosScreen(
    onExerciseClick: (Long) -> Unit = {},
    onCreateExerciseClick: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: BibliotecaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BibliotecaEjerciciosScreenContent(
        uiState = uiState,
        onExerciseClick = onExerciseClick,
        onCreateExerciseClick = onCreateExerciseClick,
        onBack = onBack,
        onSearchQueryChange = { viewModel.onSearchQueryChange(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibliotecaEjerciciosScreenContent(
    uiState: BibliotecaUiState,
    onExerciseClick: (Long) -> Unit,
    onCreateExerciseClick: () -> Unit,
    onBack: () -> Unit,
    onSearchQueryChange: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.routines_biblioteca)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateExerciseClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.biblioteca_create_exercise)) }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            OutlinedTextField(
                value = if (uiState is BibliotecaUiState.Ready) uiState.searchQuery else "",
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text(stringResource(R.string.action_search)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            when (val state = uiState) {
                BibliotecaUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                is BibliotecaUiState.Ready -> {
                    if (state.exercises.isEmpty()) {
                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                            Text(stringResource(R.string.biblioteca_empty))
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.exercises, key = { it.id }) { exercise ->
                                ListItem(
                                    headlineContent = { Text(exercise.name) },
                                    supportingContent = { Text(exercise.type.name) },
                                    modifier = Modifier.clickable { onExerciseClick(exercise.id) }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CreateExerciseDialog(
    availableTags: List<TagEntity>,
    onDismiss: () -> Unit,
    onConfirm: (String, ExerciseType, List<TagEntity>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(ExerciseType.STRENGTH) }
    val selectedTags = remember { mutableStateListOf<TagEntity>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.biblioteca_create_exercise)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.exercise_name_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(stringResource(R.string.exercise_type_label), style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == ExerciseType.STRENGTH,
                        onClick = { type = ExerciseType.STRENGTH },
                        label = { Text(stringResource(R.string.exercise_type_strength)) }
                    )
                    FilterChip(
                        selected = type == ExerciseType.ASSISTIVE,
                        onClick = { type = ExerciseType.ASSISTIVE },
                        label = { Text(stringResource(R.string.exercise_type_assistive)) }
                    )
                }

                Text(stringResource(R.string.exercise_tags_label), style = MaterialTheme.typography.labelLarge)
                LazyColumn(modifier = Modifier.height(250.dp)) {
                    val groupedTags = availableTags.groupBy { it.category }
                    groupedTags.forEach { (category, tags) ->
                        item {
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        item {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                tags.forEach { tag ->
                                    FilterChip(
                                        selected = selectedTags.contains(tag),
                                        onClick = {
                                            if (selectedTags.contains(tag)) selectedTags.remove(tag)
                                            else selectedTags.add(tag)
                                        },
                                        label = { Text(tag.name) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, type, selectedTags.toList()) },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.action_create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.session_abandon_cancel)) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePickerDialog(
    onDismiss: () -> Unit,
    onExerciseSelected: (ExerciseEntity) -> Unit,
    viewModel: BibliotecaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_back)) }
        },
        title = { Text(stringResource(R.string.biblioteca_add_exercise)) },
        text = {
            Column(modifier = Modifier.height(400.dp)) {
                OutlinedTextField(
                    value = if (uiState is BibliotecaUiState.Ready) (uiState as BibliotecaUiState.Ready).searchQuery else "",
                    onValueChange = viewModel::onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.action_search)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true
                )
                
                Spacer(Modifier.height(8.dp))

                when (val state = uiState) {
                    BibliotecaUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                    is BibliotecaUiState.Ready -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.exercises) { exercise ->
                                ListItem(
                                    headlineContent = { Text(exercise.name) },
                                    supportingContent = { Text(exercise.type.name) },
                                    modifier = Modifier.clickable { onExerciseSelected(exercise) }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    )
}

// ── Detalle de ejercicio ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EjercicioDetalleScreen(
    ejercicioId: Long,
    onBack: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ejercicio_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Ejercicio #$ejercicioId",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ── Preview ─────────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun RutinasScreenPreview() {
    Trianner4Theme {
        RutinasScreenContent(
            uiState = RutinasUiState.Ready(
                routines = listOf(
                    RoutineWithSchedule(
                        routine = RoutineEntity(id = 1, name = "Routine 1", isActive = true, createdAt = 0L),
                        schedules = emptyList()
                    )
                )
            ),
            onCreateRoutine = {},
            onEditRoutine = {},
            onOpenBiblioteca = {},
            onToggleActive = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BibliotecaScreenPreview() {
    Trianner4Theme {
        BibliotecaEjerciciosScreenContent(
            uiState = BibliotecaUiState.Ready(
                exercises = listOf(
                    ExerciseEntity(id = 1, name = "Squat", type = ExerciseType.STRENGTH, trackingMode = TrackingMode.WEIGHT_REPS)
                ),
                tags = emptyList(),
                searchQuery = "",
                showCreateDialog = false
            ),
            onExerciseClick = {},
            onCreateExerciseClick = {},
            onBack = {},
            onSearchQueryChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RutinaEditorScreenPreview() {
    Trianner4Theme { RutinaEditorScreen(rutinaId = null) }
}
