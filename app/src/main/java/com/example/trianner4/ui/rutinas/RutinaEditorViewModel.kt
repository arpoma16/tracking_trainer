package com.example.trianner4.ui.rutinas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trianner4.data.local.Phase
import com.example.trianner4.data.local.ScheduleType
import com.example.trianner4.data.local.dao.ExerciseDao
import com.example.trianner4.data.local.dao.RoutineDao
import com.example.trianner4.data.local.entity.ExerciseEntity
import com.example.trianner4.data.local.entity.RoutineEntity
import com.example.trianner4.data.local.entity.RoutinePhaseExerciseEntity
import com.example.trianner4.data.local.entity.RoutineScheduleEntity
import com.example.trianner4.data.local.model.RoutinePhaseExerciseWithExercise
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RutinaEditorViewModel @Inject constructor(
    private val routineDao: RoutineDao,
    private val exerciseDao: ExerciseDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(RutinaEditorUiState())
    val uiState: StateFlow<RutinaEditorUiState> = _uiState.asStateFlow()

    private var editingRoutineId: Long? = null

    fun loadRoutine(id: Long?) {
        editingRoutineId = id
        if (id != null) {
            viewModelScope.launch {
                val routine = routineDao.getById(id) ?: return@launch
                val schedules = routineDao.getSchedulesForRoutine(id)
                val frequency = schedules.firstOrNull { it.scheduleType == ScheduleType.EVERY_N_DAYS }?.everyNDays ?: 1
                
                val exercises = routineDao.getPhaseExercisesWithExercise(id)
                
                _uiState.update { it.copy(
                    name = routine.name,
                    frequencyDays = frequency,
                    preExercises = exercises.filter { e -> e.routinePhaseExercise.phase == Phase.PRE },
                    coreExercises = exercises.filter { e -> e.routinePhaseExercise.phase == Phase.CORE },
                    postExercises = exercises.filter { e -> e.routinePhaseExercise.phase == Phase.POST }
                ) }
            }
        }
    }

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }

    fun onFrequencyChange(newFrequency: Int) {
        _uiState.update { it.copy(frequencyDays = newFrequency) }
    }

    fun openExercisePicker(phase: Phase) {
        _uiState.update { it.copy(showExercisePicker = phase) }
    }

    fun closeExercisePicker() {
        _uiState.update { it.copy(showExercisePicker = null, searchQuery = "") }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun removeExercise(exerciseWithConfig: RoutinePhaseExerciseWithExercise) {
        val phase = exerciseWithConfig.routinePhaseExercise.phase
        _uiState.update { state ->
            when (phase) {
                Phase.PRE -> state.copy(preExercises = state.preExercises.filter { it != exerciseWithConfig })
                Phase.CORE -> state.copy(coreExercises = state.coreExercises.filter { it != exerciseWithConfig })
                Phase.POST -> state.copy(postExercises = state.postExercises.filter { it != exerciseWithConfig })
            }
        }
    }

    fun updateExerciseConfig(
        exerciseWithConfig: RoutinePhaseExerciseWithExercise,
        sets: Int?,
        reps: Int?,
        rir: Int?,
        duration: Int?
    ) {
        val phase = exerciseWithConfig.routinePhaseExercise.phase
        val updatedEntry = exerciseWithConfig.copy(
            routinePhaseExercise = exerciseWithConfig.routinePhaseExercise.copy(
                targetSets = sets,
                targetReps = reps,
                targetRir = rir,
                targetDurationSec = duration
            )
        )

        _uiState.update { state ->
            when (phase) {
                Phase.PRE -> state.copy(preExercises = state.preExercises.map { if (it == exerciseWithConfig) updatedEntry else it })
                Phase.CORE -> state.copy(coreExercises = state.coreExercises.map { if (it == exerciseWithConfig) updatedEntry else it })
                Phase.POST -> state.copy(postExercises = state.postExercises.map { if (it == exerciseWithConfig) updatedEntry else it })
            }
        }
    }

    fun addExerciseToPhase(exercise: ExerciseEntity, phase: Phase) {
        val currentList = when (phase) {
            Phase.PRE -> _uiState.value.preExercises
            Phase.CORE -> _uiState.value.coreExercises
            Phase.POST -> _uiState.value.postExercises
        }
        
        val newEntry = RoutinePhaseExerciseWithExercise(
            routinePhaseExercise = RoutinePhaseExerciseEntity(
                routineId = editingRoutineId ?: 0,
                phase = phase,
                exerciseId = exercise.id,
                orderIndex = currentList.size,
                targetSets = exercise.defaultSets ?: if (phase == Phase.CORE) 3 else 1,
                targetReps = exercise.defaultReps ?: 10,
                targetRir = exercise.defaultRir ?: 2,
                targetDurationSec = exercise.defaultDurationSec,
                restSec = 90,
                chainVariantId = null
            ),
            exercise = exercise
        )
        
        _uiState.update { state ->
            when (phase) {
                Phase.PRE -> state.copy(preExercises = state.preExercises + newEntry, showExercisePicker = null)
                Phase.CORE -> state.copy(coreExercises = state.coreExercises + newEntry, showExercisePicker = null)
                Phase.POST -> state.copy(postExercises = state.postExercises + newEntry, showExercisePicker = null)
            }
        }
    }

    fun saveRoutine(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            val routine = RoutineEntity(
                id = editingRoutineId ?: 0,
                name = _uiState.value.name,
                isActive = true,
                createdAt = System.currentTimeMillis()
            )
            
            val routineId = if (editingRoutineId == null) {
                routineDao.insert(routine)
            } else {
                routineDao.update(routine)
                editingRoutineId!!
            }
            
            // Save schedule
            val schedule = RoutineScheduleEntity(
                routineId = routineId,
                scheduleType = ScheduleType.EVERY_N_DAYS,
                everyNDays = _uiState.value.frequencyDays,
                anchorDate = System.currentTimeMillis(),
                weekdaysMask = null
            )
            val existingSchedules = routineDao.getSchedulesForRoutine(routineId)
            if (existingSchedules.isEmpty()) {
                routineDao.insertSchedule(schedule)
            } else {
                routineDao.updateSchedule(schedule.copy(id = existingSchedules.first().id))
            }
            
            // Save exercises
            routineDao.clearPhaseExercises(routineId)
            val allExercises = (_uiState.value.preExercises + _uiState.value.coreExercises + _uiState.value.postExercises)
                .mapIndexed { index, e ->
                    e.routinePhaseExercise.copy(
                        routineId = routineId,
                        orderIndex = index
                    )
                }
            routineDao.insertPhaseExercises(allExercises)
            
            _uiState.update { it.copy(isSaving = false) }
            onSuccess()
        }
    }
}
