package com.example.trianner4.ui.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trianner4.data.local.ExerciseType
import com.example.trianner4.data.local.TrackingMode
import com.example.trianner4.data.local.dao.ExerciseDao
import com.example.trianner4.data.local.dao.BodyZoneDao
import com.example.trianner4.data.local.dao.BiomechanicalChainDao
import com.example.trianner4.data.local.entity.ExerciseEntity
import com.example.trianner4.data.local.entity.BodyZoneEntity
import com.example.trianner4.data.local.entity.BiomechanicalChainEntity
import com.example.trianner4.data.local.seeders.BodyZoneSeeder
import com.example.trianner4.data.local.seeders.BiomechanicalChainSeeder
import com.example.trianner4.data.local.seeders.ExerciseSeeder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExerciseFormState(
    val name: String = "",
    val type: ExerciseType? = null,
    val trackingMode: TrackingMode? = null,
    val description: String = "",
    val mediaRef: String? = null,
    val primaryBodyZoneId: Long? = null,
    val chainId: Long? = null,
    val defaultSets: String = "",
    val defaultReps: String = "",
    val defaultRir: String = "",
    val defaultDurationSec: String = "",
    val isFormValid: Boolean = false
)

sealed interface ExerciseUiState {
    data object Loading : ExerciseUiState
    data class Ready(
        val bodyZones: List<BodyZoneEntity> = emptyList(),
        val biomechanicalChains: List<BiomechanicalChainEntity> = emptyList()
    ) : ExerciseUiState
    data class Success(val message: String) : ExerciseUiState
    data class Error(val message: String) : ExerciseUiState
}

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val exerciseDao: ExerciseDao,
    private val bodyZoneDao: BodyZoneDao,
    private val biomechanicalChainDao: BiomechanicalChainDao
) : ViewModel() {

    private val _formState = MutableStateFlow(ExerciseFormState())
    val formState: StateFlow<ExerciseFormState> = _formState.asStateFlow()

    private val _uiState = MutableStateFlow<ExerciseUiState>(ExerciseUiState.Loading)
    val uiState: StateFlow<ExerciseUiState> = _uiState.asStateFlow()

    private var editingExerciseId: Long? = null

    init {
        loadInitialData()
    }

    fun loadExercise(exerciseId: Long?) {
        if (exerciseId == null || exerciseId == editingExerciseId) return
        editingExerciseId = exerciseId

        viewModelScope.launch {
            try {
                val exercise = exerciseDao.getById(exerciseId) ?: return@launch
                _formState.update {
                    it.copy(
                        name = exercise.name,
                        type = exercise.type,
                        trackingMode = exercise.trackingMode,
                        description = exercise.description,
                        mediaRef = exercise.mediaRef,
                        primaryBodyZoneId = exercise.primaryBodyZoneId,
                        chainId = exercise.chainId,
                        defaultSets = exercise.defaultSets?.toString() ?: "",
                        defaultReps = exercise.defaultReps?.toString() ?: "",
                        defaultRir = exercise.defaultRir?.toString() ?: "",
                        defaultDurationSec = exercise.defaultDurationSec?.toString() ?: "",
                        isFormValid = validateForm(
                            name = exercise.name,
                            type = exercise.type,
                            trackingMode = exercise.trackingMode,
                            primaryBodyZoneId = exercise.primaryBodyZoneId
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.value = ExerciseUiState.Error("Failed to load exercise: ${e.message}")
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                BodyZoneSeeder.seed(bodyZoneDao)
                BiomechanicalChainSeeder.seed(biomechanicalChainDao)
                ExerciseSeeder.seed(exerciseDao, bodyZoneDao, biomechanicalChainDao)
                val bodyZones = bodyZoneDao.getAll().first()
                val chains = biomechanicalChainDao.observeAll().first()
                _uiState.value = ExerciseUiState.Ready(
                    bodyZones = bodyZones,
                    biomechanicalChains = chains
                )
            } catch (e: Exception) {
                _uiState.value = ExerciseUiState.Error("Failed to load data: ${e.message}")
            }
        }
    }

    fun onNameChanged(name: String) {
        _formState.update {
            it.copy(
                name = name,
                isFormValid = validateForm(name, it.type, it.trackingMode, it.primaryBodyZoneId)
            )
        }
    }

    fun onTypeChanged(type: ExerciseType) {
        _formState.update {
            it.copy(
                type = type,
                isFormValid = validateForm(it.name, type, it.trackingMode, it.primaryBodyZoneId)
            )
        }
    }

    fun onTrackingModeChanged(trackingMode: TrackingMode) {
        _formState.update {
            it.copy(
                trackingMode = trackingMode,
                isFormValid = validateForm(it.name, it.type, trackingMode, it.primaryBodyZoneId)
            )
        }
    }

    fun onDescriptionChanged(description: String) {
        _formState.update { it.copy(description = description) }
    }

    fun onMediaRefChanged(mediaRef: String?) {
        _formState.update { it.copy(mediaRef = mediaRef) }
    }

    fun onPrimaryBodyZoneSelected(bodyZoneId: Long?) {
        _formState.update {
            it.copy(
                primaryBodyZoneId = bodyZoneId,
                isFormValid = validateForm(it.name, it.type, it.trackingMode, bodyZoneId)
            )
        }
    }

    fun onChainIdSelected(chainId: Long?) {
        _formState.update { it.copy(chainId = chainId) }
    }

    fun onDefaultSetsChanged(value: String) {
        _formState.update { it.copy(defaultSets = value) }
    }

    fun onDefaultRepsChanged(value: String) {
        _formState.update { it.copy(defaultReps = value) }
    }

    fun onDefaultRirChanged(value: String) {
        _formState.update { it.copy(defaultRir = value) }
    }

    fun onDefaultDurationChanged(value: String) {
        _formState.update { it.copy(defaultDurationSec = value) }
    }

    private fun validateForm(
        name: String,
        type: ExerciseType?,
        trackingMode: TrackingMode?,
        primaryBodyZoneId: Long?
    ): Boolean {
        return name.isNotBlank() && type != null && trackingMode != null && primaryBodyZoneId != null
    }

    fun saveExercise(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val currentState = _formState.value
            if (currentState.isFormValid) {
                try {
                    val exercise = ExerciseEntity(
                        id = editingExerciseId ?: 0,
                        name = currentState.name,
                        type = currentState.type!!,
                        trackingMode = currentState.trackingMode!!,
                        description = currentState.description,
                        mediaRef = currentState.mediaRef,
                        primaryBodyZoneId = currentState.primaryBodyZoneId,
                        chainId = currentState.chainId,
                        defaultSets = currentState.defaultSets.toIntOrNull(),
                        defaultReps = currentState.defaultReps.toIntOrNull(),
                        defaultRir = currentState.defaultRir.toIntOrNull(),
                        defaultDurationSec = currentState.defaultDurationSec.toIntOrNull()
                    )
                    
                    exerciseDao.insert(exercise)

                    _formState.update { ExerciseFormState() }
                    _uiState.value = ExerciseUiState.Success("Exercise saved successfully")
                    onSuccess()
                } catch (e: Exception) {
                    _uiState.value = ExerciseUiState.Error("Failed to save exercise: ${e.message}")
                }
            } else {
                _uiState.value = ExerciseUiState.Error("Please fill in all required fields.")
            }
        }
    }
}
