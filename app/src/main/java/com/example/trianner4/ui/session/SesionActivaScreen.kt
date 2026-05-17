package com.example.trianner4.ui.session

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.trianner4.R
import com.example.trianner4.data.local.ExerciseType
import com.example.trianner4.data.local.Phase
import com.example.trianner4.data.local.TrackingMode
import com.example.trianner4.data.local.entity.SessionExerciseSnapshotEntity
import com.example.trianner4.ui.theme.Trianner4Theme
import kotlin.math.roundToInt

// ── Pantalla de sesión activa ────────────────────────────────────────────────────

@Composable
fun SesionActivaScreen(
    routineId: Long = 0L,
    onSessionFinished: () -> Unit = {},
    viewModel: SesionActivaViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(routineId) {
        if (routineId > 0L) viewModel.startSession(routineId)
    }

    SesionActivaScreenContent(
        uiState = uiState,
        onSessionFinished = onSessionFinished,
        onStartAssistiveTimer = viewModel::startAssistiveTimer,
        onPauseAssistiveTimer = viewModel::pauseAssistiveTimer,
        onCompleteAssistive = { dur, reps, relief, discomfortId -> 
            viewModel.completeAssistive(dur, reps, relief, discomfortId) 
        },
        onNextExercise = viewModel::nextExercise,
        onPrevExercise = viewModel::previousExercise,
        onUpdatePendingSet = viewModel::updatePendingSet,
        onConfirmSet = { idx, rest -> viewModel.confirmSet(idx, rest ?: 90) },
        onSkipRest = viewModel::skipRest
    )
}

@Composable
fun SesionActivaScreenContent(
    uiState: SesionActivaUiState,
    onSessionFinished: () -> Unit,
    onStartAssistiveTimer: () -> Unit,
    onPauseAssistiveTimer: () -> Unit,
    onCompleteAssistive: (Int?, Int?, Int?, Long?) -> Unit,
    onNextExercise: () -> Unit,
    onPrevExercise: () -> Unit,
    onUpdatePendingSet: (Int, Double?, String?, Int, Int) -> Unit,
    onConfirmSet: (Int, Int?) -> Unit,
    onSkipRest: () -> Unit
) {
    when (val state = uiState) {
        is SesionActivaUiState.Idle,
        is SesionActivaUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is SesionActivaUiState.Error -> {
            ErrorContent(state.message, onBack = onSessionFinished)
        }

        is SesionActivaUiState.PhaseActive -> {
            PhaseActiveContent(
                state = state,
                onSessionFinished = onSessionFinished,
                onStartAssistiveTimer = onStartAssistiveTimer,
                onPauseAssistiveTimer = onPauseAssistiveTimer,
                onCompleteAssistive = onCompleteAssistive,
                onNextExercise = onNextExercise,
                onPrevExercise = onPrevExercise,
                onUpdatePendingSet = onUpdatePendingSet,
                onConfirmSet = onConfirmSet,
                onSkipRest = onSkipRest
            )
        }

        is SesionActivaUiState.Summary -> {
            SummaryContent(
                summary = state.data,
                onClose = onSessionFinished,
            )
        }
    }
}

// ── Error ────────────────────────────────────────────────────────────────────────

@Composable
private fun ErrorContent(message: String, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = message.ifBlank { stringResource(R.string.session_error_generic) },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )
            Button(onClick = onBack) {
                Text(stringResource(R.string.action_back))
            }
        }
    }
}

// ── PhaseActive ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhaseActiveContent(
    state: SesionActivaUiState.PhaseActive,
    onSessionFinished: () -> Unit,
    onStartAssistiveTimer: () -> Unit,
    onPauseAssistiveTimer: () -> Unit,
    onCompleteAssistive: (Int?, Int?, Int?, Long?) -> Unit,
    onNextExercise: () -> Unit,
    onPrevExercise: () -> Unit,
    onUpdatePendingSet: (Int, Double?, String?, Int, Int) -> Unit,
    onConfirmSet: (Int, Int?) -> Unit,
    onSkipRest: () -> Unit
) {
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler { showExitDialog = true }

    if (showExitDialog) {
        AbandonarSesionDialog(
            onConfirm = { showExitDialog = false; onSessionFinished() },
            onDismiss = { showExitDialog = false },
        )
    }

    val phaseLabelRes = when (state.phase) {
        Phase.PRE  -> R.string.session_phase_pre
        Phase.CORE -> R.string.session_phase_core
        Phase.POST -> R.string.session_phase_post
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    title = {
                        Column {
                            Text(
                                text = stringResource(phaseLabelRes),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = stringResource(
                                    R.string.session_exercise_counter,
                                    state.exerciseIndex + 1,
                                    state.totalExercisesInPhase
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showExitDialog = true }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.session_abandon_confirm)
                            )
                        }
                    },
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                LinearProgressIndicator(
                    progress = { state.overallProgress },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(8.dp))
                PhaseStepperRow(currentPhase = state.phase)
                Spacer(Modifier.height(8.dp))

                AnimatedContent(
                    targetState = state.snapshot.id,
                    label = "exercise_content"
                ) { _ ->
                    when (state.exerciseType) {
                        ExerciseType.ASSISTIVE -> {
                            AssistiveExerciseContent(
                                state = state,
                                onStart = onStartAssistiveTimer,
                                onPause = onPauseAssistiveTimer,
                                onComplete = { dur, reps ->
                                    onCompleteAssistive(dur, reps, null, null)
                                },
                                onNext = onNextExercise,
                                onPrev = onPrevExercise,
                            )
                        }
                        ExerciseType.STRENGTH -> {
                            StrengthExerciseContent(
                                state = state,
                                onUpdateSet = onUpdatePendingSet,
                                onConfirmSet = { idx ->
                                    onConfirmSet(idx, state.restSec)
                                },
                                onNext = onNextExercise,
                                onPrev = onPrevExercise,
                            )
                        }
                    }
                }
            }
        }

        // Rest timer overlay — aparece sobre todo cuando hay descanso activo
        state.restTimer?.let { rest ->
            RestTimerOverlay(
                restTimer = rest,
                onSkip = onSkipRest,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

// ── Stepper de fases ─────────────────────────────────────────────────────────────

@Composable
private fun PhaseStepperRow(currentPhase: Phase) {
    val phases = listOf(
        Phase.PRE  to stringResource(R.string.phase_pre),
        Phase.CORE to stringResource(R.string.phase_core),
        Phase.POST to stringResource(R.string.phase_post),
    )
    val currentIndex = phases.indexOfFirst { it.first == currentPhase }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        phases.forEachIndexed { index, (_, label) ->
            val isActive = index == currentIndex
            val isDone   = index < currentIndex

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = when {
                        isActive -> MaterialTheme.colorScheme.primary
                        isDone   -> MaterialTheme.colorScheme.secondary
                        else     -> MaterialTheme.colorScheme.surfaceVariant
                    },
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isDone) "✓" else (index + 1).toString(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isActive -> MaterialTheme.colorScheme.onPrimary
                                isDone   -> MaterialTheme.colorScheme.onSecondary
                                else     -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

// ── Fase PRE / POST — ejercicio asistencial ──────────────────────────────────────

@Composable
private fun AssistiveExerciseContent(
    state: SesionActivaUiState.PhaseActive,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onComplete: (durationSec: Int?, repsActual: Int?) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
) {
    val timer = state.assistiveTimer

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ExerciseHeader(
                name = state.exerciseName,
                isAdapted = state.isAdaptedExercise,
            )
        }

        item {
            when {
                timer?.targetDurationSec != null -> {
                    CountdownTimerCard(
                        timer = timer,
                        onStart = onStart,
                        onPause = onPause,
                        onComplete = { onComplete(timer.elapsedSec, null) },
                    )
                }
                timer?.targetReps != null -> {
                    FixedRepsCard(
                        targetReps = timer.targetReps,
                        isCompleted = timer.isCompleted,
                        onComplete = { onComplete(null, timer.targetReps) },
                    )
                }
                else -> {
                    FixedRepsCard(
                        targetReps = 10,
                        isCompleted = false,
                        onComplete = { onComplete(null, 10) },
                    )
                }
            }
        }

        item {
            NavigationButtons(
                canGoBack = state.exerciseIndex > 0,
                isLastExercise = state.exerciseIndex >= state.totalExercisesInPhase - 1,
                isCompleted = timer?.isCompleted == true,
                onPrev = onPrev,
                onNext = onNext,
            )
        }
    }
}

// ── Temporizador circular de cuenta regresiva ────────────────────────────────────

@Composable
private fun CountdownTimerCard(
    timer: AssistiveTimerState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onComplete: () -> Unit,
) {
    val target = timer.targetDurationSec ?: 0
    val remaining = (target - timer.elapsedSec).coerceAtLeast(0)
    val fraction = if (target > 0) remaining.toFloat() / target else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            CircularCountdownTimer(
                fraction = fraction,
                remaining = remaining,
                isCompleted = timer.isCompleted,
            )

            if (!timer.isCompleted) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!timer.isRunning) {
                        Button(onClick = onStart, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                if (timer.elapsedSec == 0)
                                    stringResource(R.string.session_timer_start)
                                else
                                    stringResource(R.string.session_timer_resume)
                            )
                        }
                    } else {
                        OutlinedButton(onClick = onPause, modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.session_timer_pause))
                        }
                    }
                    OutlinedButton(onClick = onComplete, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.session_timer_skip))
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.session_reps_done),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CircularCountdownTimer(
    fraction: Float,
    remaining: Int,
    isCompleted: Boolean,
) {
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 800),
        label = "timer_arc"
    )

    val arcColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    val completedColor = MaterialTheme.colorScheme.secondary

    val minutes = remaining / 60
    val seconds = remaining % 60
    val label = "%02d:%02d".format(minutes, seconds)

    Box(
        modifier = Modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            drawArc(color = trackColor, startAngle = -90f, sweepAngle = 360f, useCenter = false, style = stroke)
            drawArc(
                color = if (isCompleted) completedColor else arcColor,
                startAngle = -90f,
                sweepAngle = if (isCompleted) 360f else animatedFraction * 360f,
                useCenter = false,
                style = stroke,
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isCompleted) "✓" else label,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = if (isCompleted) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.onSurface,
            )
            if (!isCompleted) {
                Text(
                    text = "mm:ss",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun FixedRepsCard(
    targetReps: Int,
    isCompleted: Boolean,
    onComplete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = stringResource(R.string.session_reps_target, targetReps),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
            if (!isCompleted) {
                Button(onClick = onComplete, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.session_reps_done))
                }
            } else {
                Text(
                    text = stringResource(R.string.session_reps_done),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ── Fase NÚCLEO — ejercicio de fuerza ────────────────────────────────────────────

@Composable
private fun StrengthExerciseContent(
    state: SesionActivaUiState.PhaseActive,
    onUpdateSet: (Int, Double?, String?, Int, Int) -> Unit,
    onConfirmSet: (Int) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
) {
    val allConfirmed = state.pendingSets.all { it.isCompleted }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            ExerciseHeader(
                name = state.exerciseName,
                isAdapted = state.isAdaptedExercise,
                subtitle = state.targetRir?.let { "RIR objetivo: $it" }
            )
        }

        item { SetTableHeader() }

        itemsIndexed(state.pendingSets) { idx, set ->
            SetRow(
                setNumber = idx + 1,
                pendingSet = set,
                trackingMode = state.trackingMode,
                onUpdate = { wt, band, reps, rir ->
                    onUpdateSet(idx, wt, band, reps, rir)
                },
                onConfirm = { onConfirmSet(idx) },
            )
        }

        item {
            Spacer(Modifier.height(4.dp))
            NavigationButtons(
                canGoBack = state.exerciseIndex > 0,
                isLastExercise = state.exerciseIndex >= state.totalExercisesInPhase - 1,
                isCompleted = allConfirmed,
                onPrev = onPrev,
                onNext = onNext,
            )
        }
    }
}

@Composable
private fun SetTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeaderCell(stringResource(R.string.session_set_col_set), Modifier.width(30.dp))
        Spacer(Modifier.width(8.dp))
        HeaderCell(stringResource(R.string.session_set_col_weight), Modifier.weight(2f))
        Spacer(Modifier.width(8.dp))
        HeaderCell(stringResource(R.string.session_set_col_reps), Modifier.weight(1.3f))
        Spacer(Modifier.width(8.dp))
        HeaderCell(stringResource(R.string.session_set_col_rir), Modifier.weight(3f))
        Spacer(Modifier.width(8.dp))
        Spacer(Modifier.width(40.dp))
    }
}

@Composable
private fun HeaderCell(text: String, modifier: Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun SetRow(
    setNumber: Int,
    pendingSet: PendingSet,
    trackingMode: TrackingMode,
    onUpdate: (weightKg: Double?, bandTension: String?, reps: Int, rir: Int) -> Unit,
    onConfirm: () -> Unit,
) {
    var weightText by remember(setNumber, pendingSet.isCompleted) {
        mutableStateOf(pendingSet.weightKg?.let { if (it == 0.0) "" else it.toString() } ?: "")
    }
    var reps by remember(setNumber, pendingSet.isCompleted) {
        mutableIntStateOf(pendingSet.reps.coerceAtLeast(pendingSet.targetReps ?: 0))
    }
    var rir by remember(setNumber, pendingSet.isCompleted) {
        mutableIntStateOf(pendingSet.rir)
    }

    val rowBg = if (pendingSet.isCompleted)
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
    else
        MaterialTheme.colorScheme.surface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg, RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Número de serie
        Text(
            text = setNumber.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(30.dp),
            textAlign = TextAlign.Center,
            color = if (pendingSet.isCompleted) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.width(8.dp))

        // Peso — stepper numérico
        WeightStepper(
            value = weightText,
            enabled = !pendingSet.isCompleted,
            modifier = Modifier.weight(2f),
            onValueChange = { newText ->
                weightText = newText
                onUpdate(newText.toDoubleOrNull(), null, reps, rir)
            },
            onIncrement = {
                val next = ((weightText.toDoubleOrNull() ?: 0.0) + 2.5).roundTo1()
                weightText = next.toString()
                onUpdate(next, null, reps, rir)
            },
            onDecrement = {
                val next = ((weightText.toDoubleOrNull() ?: 0.0) - 2.5).coerceAtLeast(0.0).roundTo1()
                weightText = if (next == 0.0) "" else next.toString()
                onUpdate(next, null, reps, rir)
            }
        )

        Spacer(Modifier.width(8.dp))

        // Reps — stepper
        RepsStepper(
            value = reps,
            enabled = !pendingSet.isCompleted,
            modifier = Modifier.weight(1.3f),
            onIncrement = {
                reps++
                onUpdate(weightText.toDoubleOrNull(), null, reps, rir)
            },
            onDecrement = {
                if (reps > 0) {
                    reps--
                    onUpdate(weightText.toDoubleOrNull(), null, reps, rir)
                }
            }
        )

        Spacer(Modifier.width(8.dp))

        // RIR selector 0..10
        RirSelector(
            selected = rir,
            enabled = !pendingSet.isCompleted,
            modifier = Modifier.weight(3f),
            onSelect = { newRir ->
                rir = newRir
                onUpdate(weightText.toDoubleOrNull(), null, reps, newRir)
            }
        )

        Spacer(Modifier.width(8.dp))

        // Confirmar / check
        if (!pendingSet.isCompleted) {
            FilledTonalButton(
                onClick = onConfirm,
                modifier = Modifier.size(40.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text("✓", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("✓", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

// ── Componentes de input ─────────────────────────────────────────────────────────

@Composable
private fun WeightStepper(
    value: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onIncrement, enabled = enabled, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.KeyboardArrowUp, null, Modifier.size(18.dp))
        }
        OutlinedTextField(
            value = value,
            onValueChange = { if (it.length <= 7) onValueChange(it) },
            enabled = enabled,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            placeholder = {
                Text(
                    "kg",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        )
        IconButton(onClick = onDecrement, enabled = enabled, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(18.dp))
        }
    }
}

@Composable
private fun RepsStepper(
    value: Int,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        IconButton(onClick = onIncrement, enabled = enabled, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.KeyboardArrowUp, null, Modifier.size(20.dp))
        }
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (enabled) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant
        )
        IconButton(onClick = onDecrement, enabled = enabled, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(20.dp))
        }
    }
}

@Composable
private fun RirSelector(
    selected: Int,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        (0..10).forEach { v ->
            val isSelected = v == selected
            val chipColor = rirColor(v)
            Surface(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape),
                color = if (isSelected) chipColor else chipColor.copy(alpha = 0.15f),
                shape = CircleShape,
                onClick = { if (enabled) onSelect(v) },
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = v.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else chipColor,
                        fontSize = 8.sp,
                    )
                }
            }
        }
    }
}

private fun rirColor(rir: Int): Color = when {
    rir <= 1 -> Color(0xFFD32F2F)
    rir <= 3 -> Color(0xFF388E3C)
    else     -> Color(0xFFF57F17)
}

// ── Temporizador de descanso (overlay sticky-bottom) ────────────────────────────

@Composable
private fun RestTimerOverlay(
    restTimer: RestTimerState,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fraction = if (restTimer.totalSec > 0)
        restTimer.remainingSec.toFloat() / restTimer.totalSec else 0f
    val animatedFrac by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(900),
        label = "rest_arc"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Arc circular
            val arcColor = MaterialTheme.colorScheme.primary
            val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            Box(Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val stroke = Stroke(7.dp.toPx(), cap = StrokeCap.Round)
                    drawArc(trackColor, -90f, 360f, false, style = stroke)
                    drawArc(arcColor, -90f, animatedFrac * 360f, false, style = stroke)
                }
                Text(
                    text = "${restTimer.remainingSec}s",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.session_rest_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = stringResource(R.string.session_rest_next_set),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }

            Button(
                onClick = onSkip,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(stringResource(R.string.session_rest_skip))
            }
        }
    }
}

// ── Componentes compartidos ──────────────────────────────────────────────────────

@Composable
private fun ExerciseHeader(
    name: String,
    isAdapted: Boolean,
    subtitle: String? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            if (isAdapted) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF795548).copy(alpha = 0.12f)
                ) {
                    Text(
                        text = stringResource(R.string.session_injected_badge),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF795548)
                    )
                }
            }
        }
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun NavigationButtons(
    canGoBack: Boolean,
    isLastExercise: Boolean,
    isCompleted: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (canGoBack) {
            OutlinedButton(onClick = onPrev, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.session_prev_exercise))
            }
        } else {
            Spacer(Modifier.weight(1f))
        }

        Button(
            onClick = onNext,
            enabled = isCompleted,
            modifier = Modifier.weight(2f),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLastExercise)
                    MaterialTheme.colorScheme.secondary
                else
                    MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                if (isLastExercise)
                    stringResource(R.string.session_finish_phase)
                else
                    stringResource(R.string.session_next_exercise)
            )
        }
    }
}

// ── Resumen final ────────────────────────────────────────────────────────────────

@Composable
private fun SummaryContent(
    summary: SessionSummary,
    onClose: () -> Unit,
) {
    var fatigueScore by remember { mutableFloatStateOf(5f) }
    var painScore by remember { mutableFloatStateOf(0f) }
    var readinessScore by remember { mutableFloatStateOf(3f) }

    val durationMinutes = summary.durationMs / 60_000
    val durationSeconds = (summary.durationMs % 60_000) / 1_000

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.session_summary_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SummaryMetric(
                        label = stringResource(R.string.session_summary_tonnage),
                        value = stringResource(R.string.session_summary_tonnage_value, summary.totalTonnageKg)
                    )
                    SummaryMetric(
                        label = stringResource(R.string.session_summary_duration),
                        value = "${durationMinutes}m ${durationSeconds}s"
                    )
                    SummaryMetric(
                        label = stringResource(R.string.session_summary_sets),
                        value = "${summary.setsCompleted}"
                    )
                    SummaryMetric(
                        label = stringResource(R.string.session_summary_avg_rir),
                        value = stringResource(R.string.session_summary_avg_rir_value, summary.avgRir)
                    )
                }
            }
        }

        item {
            HorizontalDivider()
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.session_survey_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            SurveySlider(
                label = stringResource(R.string.session_survey_fatigue),
                value = fatigueScore,
                valueRange = 1f..10f,
                steps = 8,
                onValueChange = { fatigueScore = it }
            )
        }

        item {
            SurveySlider(
                label = stringResource(R.string.session_survey_pain),
                value = painScore,
                valueRange = 0f..10f,
                steps = 9,
                onValueChange = { painScore = it }
            )
        }

        item {
            SurveySlider(
                label = stringResource(R.string.session_survey_readiness),
                value = readinessScore,
                valueRange = 1f..5f,
                steps = 3,
                onValueChange = { readinessScore = it }
            )
        }

        item {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.session_save_close),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun SummaryMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SurveySlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = value.roundToInt().toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ── Diálogo de abandono ──────────────────────────────────────────────────────────

@Composable
private fun AbandonarSesionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.session_abandon_title)) },
        text = { Text(stringResource(R.string.session_abandon_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.session_abandon_confirm),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.session_abandon_cancel))
            }
        },
    )
}

// ── Helpers ──────────────────────────────────────────────────────────────────────

private fun Double.roundTo1(): Double = (this * 10).roundToInt() / 10.0

// ── Previews ─────────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun SesionActivaScreenPreview() {
    Trianner4Theme {
        SesionActivaScreenContent(
            uiState = SesionActivaUiState.PhaseActive(
                sessionId = 1,
                phase = Phase.CORE,
                exerciseIndex = 0,
                totalExercisesInPhase = 5,
                exerciseName = "Squat",
                exerciseType = ExerciseType.STRENGTH,
                trackingMode = TrackingMode.WEIGHT_REPS,
                snapshot = SessionExerciseSnapshotEntity(
                    sessionId = 1,
                    exerciseId = 1,
                    phase = Phase.CORE,
                    orderIndex = 0,
                    exerciseNameSnapshot = "Squat",
                    chainVariantLevelSnapshot = 1,
                    substitutionReason = null
                ),
                pendingSets = listOf(
                    PendingSet(setIndex = 0, targetReps = 10, rir = 2)
                ),
                assistiveTimer = null,
                restTimer = null,
                overallProgress = 0.2f
            ),
            onSessionFinished = {},
            onStartAssistiveTimer = {},
            onPauseAssistiveTimer = {},
            onCompleteAssistive = { _, _, _, _ -> },
            onNextExercise = {},
            onPrevExercise = {},
            onUpdatePendingSet = { _, _, _, _, _ -> },
            onConfirmSet = { _, _ -> },
            onSkipRest = {}
        )
    }
}

@Preview(showBackground = true, name = "Timer — en curso")
@Composable
private fun PreviewCountdownRunning() {
    Trianner4Theme {
        Surface(Modifier.padding(16.dp)) {
            CountdownTimerCard(
                timer = AssistiveTimerState(targetDurationSec = 120, targetReps = null, elapsedSec = 45, isRunning = true),
                onStart = {},
                onPause = {},
                onComplete = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "Timer — completado")
@Composable
private fun PreviewCountdownDone() {
    Trianner4Theme {
        Surface(Modifier.padding(16.dp)) {
            CountdownTimerCard(
                timer = AssistiveTimerState(targetDurationSec = 120, targetReps = null, elapsedSec = 120, isRunning = false, isCompleted = true),
                onStart = {},
                onPause = {},
                onComplete = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "Reps fijas")
@Composable
private fun PreviewFixedReps() {
    Trianner4Theme {
        Surface(Modifier.padding(16.dp)) {
            FixedRepsCard(targetReps = 12, isCompleted = false, onComplete = {})
        }
    }
}

@Preview(showBackground = true, name = "Selector RIR")
@Composable
private fun PreviewRirSelector() {
    Trianner4Theme {
        Surface(Modifier.padding(16.dp)) {
            RirSelector(selected = 2, enabled = true, onSelect = {})
        }
    }
}

@Preview(showBackground = true, name = "Descanso overlay")
@Composable
private fun PreviewRestOverlay() {
    Trianner4Theme {
        Surface {
            RestTimerOverlay(
                restTimer = RestTimerState(totalSec = 90, remainingSec = 57),
                onSkip = {}
            )
        }
    }
}
