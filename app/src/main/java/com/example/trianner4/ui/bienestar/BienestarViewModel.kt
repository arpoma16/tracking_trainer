package com.example.trianner4.ui.bienestar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trianner4.data.local.dao.AdaptationLogDao
import com.example.trianner4.data.local.dao.BodyZoneDao
import com.example.trianner4.data.local.dao.DiscomfortDao
import com.example.trianner4.data.local.dao.TagDao
import com.example.trianner4.data.local.entity.DiscomfortEntity
import com.example.trianner4.data.local.entity.DiscomfortTagEntity
import com.example.trianner4.data.local.seeders.BodyZoneSeeder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BienestarViewModel @Inject constructor(
    private val discomfortDao: DiscomfortDao,
    private val adaptationLogDao: AdaptationLogDao,
    private val tagDao: TagDao,
    private val bodyZoneDao: BodyZoneDao
) : ViewModel() {

    private val _discomfortFormState = MutableStateFlow(DiscomfortFormState())
    val discomfortFormState: StateFlow<DiscomfortFormState> = _discomfortFormState.asStateFlow()

    init {
        viewModelScope.launch { 
            BodyZoneSeeder.seed(bodyZoneDao)
        }
    }

    val uiState: StateFlow<BienestarUiState> = combine(
        discomfortDao.observeAll(),
        adaptationLogDao.observeAll(),
        bodyZoneDao.getAll(),
        _discomfortFormState
    ) { discomforts, adaptations, bodyZones, formState ->
        val active = discomforts.filter { it.isActive }.map { d ->
            DiscomfortWithTags(d, tagDao.getTagsForDiscomfort(d.id))
        }
        val resolved = discomforts.filter { !it.isActive }.map { d ->
            DiscomfortWithTags(d, tagDao.getTagsForDiscomfort(d.id))
        }
        BienestarUiState.Ready(
            activeDiscomforts = active,
            resolvedDiscomforts = resolved,
            adaptationHistory = adaptations,
            bodyZones = bodyZones,
            discomfortFormState = formState
        ) as BienestarUiState
    }
        .catch { e -> emit(BienestarUiState.Error(e.message ?: "Error desconocido")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BienestarUiState.Loading
        )

    fun showDiscomfortForm(show: Boolean) {
        if (!show) {
            _discomfortFormState.update { DiscomfortFormState() }
        } else {
            _discomfortFormState.update { it.copy(isSheetOpen = true) }
        }
    }

    fun editDiscomfort(dwt: DiscomfortWithTags) {
        _discomfortFormState.update {
            it.copy(
                isSheetOpen = true,
                editingDiscomfortId = dwt.discomfort.id,
                selectedBodyZoneId = dwt.discomfort.bodyZoneId,
                selectedBodyZoneName = dwt.discomfort.label,
                severity = dwt.discomfort.severity,
                description = dwt.discomfort.freeText ?: ""
            )
        }
    }

    fun onBodyZoneSelected(bodyZoneId: Long, bodyZoneName: String) {
        _discomfortFormState.update { it.copy(selectedBodyZoneId = bodyZoneId, selectedBodyZoneName = bodyZoneName) }
    }

    fun onSeverityChanged(severity: Int) {
        _discomfortFormState.update { it.copy(severity = severity) }
    }

    fun onDescriptionChanged(description: String) {
        _discomfortFormState.update { it.copy(description = description) }
    }

    fun saveDiscomfort() {
        viewModelScope.launch {
            val currentFormState = _discomfortFormState.value
            currentFormState.selectedBodyZoneId?.let { bodyZoneId ->
                val discomfort = DiscomfortEntity(
                    id = currentFormState.editingDiscomfortId ?: 0L,
                    bodyZoneId = bodyZoneId,
                    label = currentFormState.selectedBodyZoneName,
                    freeText = currentFormState.description,
                    severity = currentFormState.severity,
                    startedAt = System.currentTimeMillis(),
                    resolvedAt = null,
                    isActive = true
                )
                discomfortDao.insert(discomfort) // REPLACE strategy handles update
                // Reset form after saving
                _discomfortFormState.update { DiscomfortFormState(isSheetOpen = false) }
            }
        }
    }

    fun addDiscomfort(entity: DiscomfortEntity, tagIds: List<Long>) {
        viewModelScope.launch {
            val id = discomfortDao.insert(entity)
            if (tagIds.isNotEmpty()) {
                discomfortDao.insertDiscomfortTags(
                    tagIds.map { DiscomfortTagEntity(discomfortId = id, tagId = it) }
                )
            }
        }
    }

    fun updateDiscomfort(entity: DiscomfortEntity, tagIds: List<Long>) {
        viewModelScope.launch {
            discomfortDao.update(entity)
            discomfortDao.clearTagsForDiscomfort(entity.id)
            if (tagIds.isNotEmpty()) {
                discomfortDao.insertDiscomfortTags(
                    tagIds.map { DiscomfortTagEntity(discomfortId = entity.id, tagId = it) }
                )
            }
        }
    }

    fun resolveDiscomfort(id: Long) {
        viewModelScope.launch {
            discomfortDao.resolve(id, System.currentTimeMillis())
        }
    }

    fun reactivateDiscomfort(id: Long) {
        viewModelScope.launch { discomfortDao.reactivate(id) }
    }

    fun deleteDiscomfort(entity: DiscomfortEntity) {
        viewModelScope.launch { discomfortDao.delete(entity) }
    }
}
