package com.example.trianner4.ui.rutinas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trianner4.data.local.dao.RoutineDao
import com.example.trianner4.data.local.entity.RoutineEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RutinasViewModel @Inject constructor(
    private val routineDao: RoutineDao
) : ViewModel() {

    val uiState: StateFlow<RutinasUiState> = routineDao.observeAll()
        .map { routines ->
            val withSchedules = routines.map { routine ->
                RoutineWithSchedule(
                    routine = routine,
                    schedules = routineDao.getSchedulesForRoutine(routine.id)
                )
            }
            RutinasUiState.Ready(withSchedules) as RutinasUiState
        }
        .catch { e -> emit(RutinasUiState.Error(e.message ?: "Error desconocido")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RutinasUiState.Loading
        )

    fun createRoutine(name: String, description: String = "") {
        viewModelScope.launch {
            routineDao.insert(
                RoutineEntity(
                    name = name.trim(),
                    description = description.trim(),
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteRoutine(routine: RoutineEntity) {
        viewModelScope.launch { routineDao.delete(routine) }
    }

    fun toggleActive(routine: RoutineEntity) {
        viewModelScope.launch {
            routineDao.update(routine.copy(isActive = !routine.isActive))
        }
    }

    fun renameRoutine(routine: RoutineEntity, newName: String) {
        viewModelScope.launch {
            routineDao.update(routine.copy(name = newName.trim()))
        }
    }
}
