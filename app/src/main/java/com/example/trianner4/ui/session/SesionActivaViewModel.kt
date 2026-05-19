package com.example.trianner4.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trianner4.data.local.ExerciseType
import com.example.trianner4.data.local.Phase
import com.example.trianner4.data.local.SessionStatus
import com.example.trianner4.data.local.TrackingMode
import com.example.trianner4.data.local.dao.AdaptationLogDao
import com.example.trianner4.data.local.dao.AssistiveLogDao
import com.example.trianner4.data.local.dao.ExerciseDao
import com.example.trianner4.data.local.dao.PlannedSessionDao
import com.example.trianner4.data.local.dao.RoutineDao
import com.example.trianner4.data.local.dao.SessionDao
import com.example.trianner4.data.local.dao.SetLogDao
import com.example.trianner4.data.local.entity.AssistiveLogEntity
import com.example.trianner4.data.local.entity.SessionEntity
import com.example.trianner4.data.local.entity.SessionExerciseSnapshotEntity
import com.example.trianner4.data.local.entity.SetLogEntity
import com.example.trianner4.data.local.model.RoutinePhaseExerciseWithExercise
import com.example.trianner4.domain.streak.UpdateStreakUseCase
import java.time.LocalDate
import java.time.ZoneId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SesionActivaViewModel @Inject constructor(
    private val sessionDao: SessionDao,
    private val routineDao: RoutineDao,
    private val exerciseDao: ExerciseDao,
    private val setLogDao: SetLogDao,
    private val assistiveLogDao: AssistiveLogDao,
    private val adaptationLogDao: AdaptationLogDao,
    private val plannedSessionDao: PlannedSessionDao,
    private val updateStreakUseCase: UpdateStreakUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SesionActivaUiState>(SesionActivaUiState.Idle)
    val uiState: StateFlow<SesionActivaUiState> = _uiState.asStateFlow()

    private val phaseOrder = listOf(Phase.PRE, Phase.CORE, Phase.POST)

    private var sessionId: Long = 0L
    private var sessionStartMs: Long = 0L
    private var allSnapshots: List<SessionExerciseSnapshotEntity> = emptyList()
    private var phaseExerciseConfig: List<RoutinePhaseExerciseWithExercise> = emptyList()
    private var currentPhaseIndex: Int = 0
    private var currentExerciseIndex: Int = 0

    private var restTimerJob: Job? = null
    private var assistiveTimerJob: Job? = null

    // ── Inicio de sesión nueva desde una rutina ────────────────────────────────

    fun startSession(routineId: Long) {
        viewModelScope.launch {
            _uiState.value = SesionActivaUiState.Loading
            runCatching {
                val existing = sessionDao.getActiveSession()
                if (existing != null) {
                    if (existing.routineId == routineId) {
                        // Si no se ha registrado ninguna serie ni log asistencial, los snapshots
                        // pueden estar desactualizados respecto a cambios en la rutina.
                        val existingSnapshots = sessionDao.getSnapshotsForSession(existing.id)
                        val hasAnyLogs = existingSnapshots.any { snap ->
                            setLogDao.getForSnapshot(snap.id).isNotEmpty() ||
                                    assistiveLogDao.getForSnapshot(snap.id) != null
                        }
                        if (!hasAnyLogs) {
                            val freshExercises = routineDao.getPhaseExercisesWithExercise(routineId)
                            phaseExerciseConfig = freshExercises
                            sessionDao.replaceSnapshots(
                                sessionId = existing.id,
                                snapshots = freshExercises.map { rpe ->
                                    SessionExerciseSnapshotEntity(
                                        sessionId = 0,
                                        phase = rpe.routinePhaseExercise.phase,
                                        exerciseId = rpe.exercise.id,
                                        exerciseNameSnapshot = rpe.exercise.name,
                                        chainVariantLevelSnapshot = null,
                                        wasSubstituted = false,
                                        substitutionReason = null,
                                        orderIndex = rpe.routinePhaseExercise.orderIndex
                                    )
                                }
                            )
                        }
                    }
                    loadSession(existing.id)
                    return@runCatching
                }

                val exercises = routineDao.getPhaseExercisesWithExercise(routineId)
                phaseExerciseConfig = exercises

                val now = System.currentTimeMillis()
                val todayMidnight = LocalDate.now()
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
                val session = SessionEntity(
                    routineId = routineId,
                    date = todayMidnight,
                    startedAt = now,
                    endedAt = null,
                    status = SessionStatus.ACTIVE,
                    totalTonnage = null,
                    avgRir = null,
                    fatigueScore = null,
                    painScore = null,
                    readinessScore = null
                )
                val snapshots = exercises.map { rpe ->
                    SessionExerciseSnapshotEntity(
                        sessionId = 0,
                        phase = rpe.routinePhaseExercise.phase,
                        exerciseId = rpe.exercise.id,
                        exerciseNameSnapshot = rpe.exercise.name,
                        chainVariantLevelSnapshot = null,
                        wasSubstituted = false,
                        substitutionReason = null,
                        orderIndex = rpe.routinePhaseExercise.orderIndex
                    )
                }
                val newId = sessionDao.createSessionWithSnapshots(session, snapshots)
                loadSession(newId)
            }.onFailure { e ->
                _uiState.value = SesionActivaUiState.Error(e.message ?: "Error al iniciar sesión")
            }
        }
    }

    // ── Inicialización ─────────────────────────────────────────────────────────

    fun loadSession(id: Long) {
        viewModelScope.launch {
            _uiState.value = SesionActivaUiState.Loading
            runCatching {
                sessionId = id
                val session = sessionDao.getById(id)
                sessionStartMs = session?.startedAt ?: System.currentTimeMillis()
                // Cargar config de fases si no está ya cargada (ej. reanudación de sesión existente)
                if (phaseExerciseConfig.isEmpty() && session != null) {
                    phaseExerciseConfig = routineDao.getPhaseExercisesWithExercise(session.routineId)
                }
                allSnapshots = sessionDao.getSnapshotsForSession(id)
                currentPhaseIndex = 0
                currentExerciseIndex = 0
                showCurrentExercise()
            }.onFailure { e ->
                _uiState.value = SesionActivaUiState.Error(e.message ?: "Error al cargar sesión")
            }
        }
    }

    // ── Navegación entre ejercicios y fases ────────────────────────────────────

    fun nextExercise() {
        cancelTimers()
        val phaseSnaps = snapshotsForCurrentPhase()
        if (currentExerciseIndex < phaseSnaps.lastIndex) {
            currentExerciseIndex++
            showCurrentExercise()
        } else {
            advancePhase()
        }
    }

    fun previousExercise() {
        cancelTimers()
        if (currentExerciseIndex > 0) {
            currentExerciseIndex--
            showCurrentExercise()
        }
    }

    private fun advancePhase() {
        var next = currentPhaseIndex + 1
        // Saltear fases vacías
        while (next <= phaseOrder.lastIndex && allSnapshots.none { it.phase == phaseOrder[next] }) {
            next++
        }
        if (next > phaseOrder.lastIndex) {
            _uiState.value = SesionActivaUiState.SurveyPending
        } else {
            currentPhaseIndex = next
            currentExerciseIndex = 0
            showCurrentExercise()
        }
    }

    // ── Registro de series STRENGTH ────────────────────────────────────────────

    fun updatePendingSet(
        setIndex: Int,
        weightKg: Double?,
        bandTension: String?,
        reps: Int,
        rir: Int
    ) {
        val state = _uiState.value as? SesionActivaUiState.PhaseActive ?: return
        val updated = state.pendingSets.toMutableList()
        if (setIndex in updated.indices) {
            updated[setIndex] = updated[setIndex].copy(
                weightKg = weightKg,
                bandTension = bandTension,
                reps = reps,
                rir = rir
            )
            _uiState.update { state.copy(pendingSets = updated) }
        }
    }

    fun confirmSet(setIndex: Int, restSec: Int = 90) {
        val state = _uiState.value as? SesionActivaUiState.PhaseActive ?: return
        val set = state.pendingSets.getOrNull(setIndex) ?: return

        viewModelScope.launch {
            setLogDao.insert(
                SetLogEntity(
                    snapshotId = state.snapshot.id,
                    setIndex = setIndex,
                    weightKg = set.weightKg,
                    bandTension = set.bandTension,
                    reps = set.reps,
                    rir = set.rir,
                    isCompleted = true
                )
            )
            val updated = state.pendingSets.toMutableList()
            updated[setIndex] = updated[setIndex].copy(isCompleted = true)
            _uiState.update { state.copy(pendingSets = updated) }
            startRestTimer(restSec)
        }
    }

    // ── Registro de ejercicios ASSISTIVE ───────────────────────────────────────

    fun completeAssistive(
        durationActualSec: Int?,
        repsActual: Int?,
        reliefScore: Int?,
        injectedByDiscomfortId: Long? = null
    ) {
        val state = _uiState.value as? SesionActivaUiState.PhaseActive ?: return
        viewModelScope.launch {
            assistiveLogDao.insert(
                AssistiveLogEntity(
                    snapshotId = state.snapshot.id,
                    durationActualSec = durationActualSec,
                    repsActual = repsActual,
                    completed = true,
                    reliefScore = reliefScore,
                    injectedByDiscomfortId = injectedByDiscomfortId
                )
            )
            val timer = state.assistiveTimer?.copy(isCompleted = true)
            _uiState.update { state.copy(assistiveTimer = timer) }
        }
    }

    // ── Temporizador de descanso ───────────────────────────────────────────────

    fun startRestTimer(totalSec: Int) {
        restTimerJob?.cancel()
        restTimerJob = viewModelScope.launch {
            var remaining = totalSec
            while (remaining > 0) {
                val state = _uiState.value as? SesionActivaUiState.PhaseActive ?: return@launch
                _uiState.update {
                    state.copy(restTimer = RestTimerState(totalSec, remaining))
                }
                delay(1_000)
                remaining--
            }
            val state = _uiState.value as? SesionActivaUiState.PhaseActive ?: return@launch
            _uiState.update { state.copy(restTimer = null) }
        }
    }

    fun skipRest() {
        restTimerJob?.cancel()
        val state = _uiState.value as? SesionActivaUiState.PhaseActive ?: return
        _uiState.update { state.copy(restTimer = null) }
    }

    // ── Temporizador de ejercicio asistencial ──────────────────────────────────

    fun startAssistiveTimer() {
        val state = _uiState.value as? SesionActivaUiState.PhaseActive ?: return
        val timer = state.assistiveTimer ?: return
        assistiveTimerJob?.cancel()
        assistiveTimerJob = viewModelScope.launch {
            var elapsed = timer.elapsedSec
            val target = timer.targetDurationSec ?: return@launch
            _uiState.update { state.copy(assistiveTimer = timer.copy(isRunning = true)) }
            while (elapsed < target) {
                delay(1_000)
                elapsed++
                val s = _uiState.value as? SesionActivaUiState.PhaseActive ?: return@launch
                _uiState.update { s.copy(assistiveTimer = s.assistiveTimer?.copy(elapsedSec = elapsed)) }
            }
            val s = _uiState.value as? SesionActivaUiState.PhaseActive ?: return@launch
            _uiState.update {
                s.copy(assistiveTimer = s.assistiveTimer?.copy(isRunning = false, isCompleted = true))
            }
        }
    }

    fun pauseAssistiveTimer() {
        assistiveTimerJob?.cancel()
        val state = _uiState.value as? SesionActivaUiState.PhaseActive ?: return
        _uiState.update { state.copy(assistiveTimer = state.assistiveTimer?.copy(isRunning = false)) }
    }

    // ── Cierre de sesión ───────────────────────────────────────────────────────

    fun closeSession(fatigueScore: Int, painScore: Int, readinessScore: Int) {
        viewModelScope.launch {
            cancelTimers()
            runCatching {
                val setLogs = allSnapshots.flatMap { setLogDao.getForSnapshot(it.id) }
                val completed = setLogs.filter { it.isCompleted }
                val tonnage = completed.sumOf { (it.weightKg ?: 0.0) * it.reps }
                val avgRir = if (completed.isEmpty()) 0.0
                             else completed.sumOf { it.rir.toDouble() } / completed.size

                val endedAt = System.currentTimeMillis()
                sessionDao.closeSession(
                    sessionId = sessionId,
                    endedAt = endedAt,
                    status = SessionStatus.COMPLETED,
                    totalTonnage = tonnage,
                    avgRir = avgRir,
                    fatigueScore = fatigueScore,
                    painScore = painScore,
                    readinessScore = readinessScore
                )

                // Mark the planned session for today as completed, then recalculate streaks
                val session = sessionDao.getById(sessionId)
                if (session != null) {
                    val todayEpoch = LocalDate.now()
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                    plannedSessionDao
                        .getByRoutineAndDate(session.routineId, todayEpoch)
                        ?.let { plannedSessionDao.markCompleted(it.id, sessionId) }
                }
                updateStreakUseCase()

                _uiState.value = SesionActivaUiState.Summary(
                    SessionSummary(
                        sessionId = sessionId,
                        totalTonnageKg = tonnage,
                        durationMs = System.currentTimeMillis() - sessionStartMs,
                        setsCompleted = completed.size,
                        setsSkipped = setLogs.size - completed.size,
                        avgRir = avgRir,
                        adaptations = adaptationLogDao.getForSession(sessionId)
                    )
                )
            }.onFailure { e ->
                _uiState.value = SesionActivaUiState.Error(e.message ?: "Error al guardar sesión")
            }
        }
    }

    // ── Helpers internos ───────────────────────────────────────────────────────

    private fun showCurrentExercise() {
        viewModelScope.launch {
            runCatching {
                val phaseSnaps = snapshotsForCurrentPhase()
                if (phaseSnaps.isEmpty()) {
                    advancePhase()
                    return@launch
                }
                val snap = phaseSnaps[currentExerciseIndex]
                val exercise = exerciseDao.getById(snap.exerciseId)
                    ?: error("Ejercicio no encontrado: ${snap.exerciseId}")

                val pendingSets = buildPendingSets(snap, exercise.type)
                val assistiveTimer = buildAssistiveTimer(snap, exercise.type, exercise.trackingMode)

                val config = phaseExerciseConfig.find {
                    it.exercise.id == snap.exerciseId &&
                    it.routinePhaseExercise.phase == phaseOrder[currentPhaseIndex]
                }

                val isLastInPhase = currentExerciseIndex == phaseSnaps.lastIndex
                val isLastExerciseOverall = isLastInPhase && run {
                    val hasNextNonEmptyPhase = ((currentPhaseIndex + 1)..phaseOrder.lastIndex).any { idx ->
                        allSnapshots.any { it.phase == phaseOrder[idx] }
                    }
                    !hasNextNonEmptyPhase
                }

                _uiState.value = SesionActivaUiState.PhaseActive(
                    sessionId = sessionId,
                    phase = phaseOrder[currentPhaseIndex],
                    exerciseIndex = currentExerciseIndex,
                    totalExercisesInPhase = phaseSnaps.size,
                    snapshot = snap,
                    exerciseName = snap.exerciseNameSnapshot,
                    exerciseType = exercise.type,
                    trackingMode = exercise.trackingMode,
                    pendingSets = pendingSets,
                    assistiveTimer = assistiveTimer,
                    restTimer = null,
                    overallProgress = calculateOverallProgress(),
                    isAdaptedExercise = snap.wasSubstituted,
                    targetRir = config?.routinePhaseExercise?.targetRir,
                    restSec = config?.routinePhaseExercise?.restSec ?: 90,
                    isLastExerciseOverall = isLastExerciseOverall,
                )
            }.onFailure { e ->
                _uiState.value = SesionActivaUiState.Error(e.message ?: "Error al cargar ejercicio")
            }
        }
    }

    private fun buildPendingSets(
        snap: SessionExerciseSnapshotEntity,
        type: ExerciseType
    ): List<PendingSet> {
        if (type != ExerciseType.STRENGTH) return emptyList()
        val config = phaseExerciseConfig.find { it.exercise.id == snap.exerciseId }
        val targetSets = config?.routinePhaseExercise?.targetSets ?: 3
        val targetReps = config?.routinePhaseExercise?.targetReps
        return List(targetSets) { i ->
            PendingSet(setIndex = i, targetReps = targetReps, rir = config?.routinePhaseExercise?.targetRir ?: 2)
        }
    }

    private fun buildAssistiveTimer(
        snap: SessionExerciseSnapshotEntity,
        type: ExerciseType,
        trackingMode: TrackingMode
    ): AssistiveTimerState? {
        if (type != ExerciseType.ASSISTIVE) return null
        val config = phaseExerciseConfig.find { it.exercise.id == snap.exerciseId }
        return when (trackingMode) {
            TrackingMode.TIMER -> AssistiveTimerState(
                targetDurationSec = config?.routinePhaseExercise?.targetDurationSec ?: 120,
                targetReps = null
            )
            TrackingMode.FIXED_REPS -> AssistiveTimerState(
                targetDurationSec = null,
                targetReps = config?.routinePhaseExercise?.targetReps ?: 12
            )
            else -> null
        }
    }

    private fun snapshotsForCurrentPhase(): List<SessionExerciseSnapshotEntity> =
        allSnapshots
            .filter { it.phase == phaseOrder[currentPhaseIndex] }
            .sortedBy { it.orderIndex }

    private fun calculateOverallProgress(): Float {
        val total = allSnapshots.size.coerceAtLeast(1)
        val done = (0 until currentPhaseIndex).sumOf { phaseIdx ->
            allSnapshots.count { it.phase == phaseOrder[phaseIdx] }
        } + currentExerciseIndex
        return done.toFloat() / total
    }

    private fun cancelTimers() {
        restTimerJob?.cancel()
        assistiveTimerJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        cancelTimers()
    }
}
