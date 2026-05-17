package com.example.trianner4.ui.bienestar

import com.example.trianner4.data.local.entity.AdaptationLogEntity
import com.example.trianner4.data.local.entity.BodyZoneEntity
import com.example.trianner4.data.local.entity.DiscomfortEntity
import com.example.trianner4.data.local.entity.TagEntity

data class DiscomfortWithTags(
    val discomfort: DiscomfortEntity,
    val tags: List<TagEntity>
)

data class DiscomfortFormState(
    val isSheetOpen: Boolean = false,
    val editingDiscomfortId: Long? = null,
    val selectedBodyZoneId: Long? = null,
    val selectedBodyZoneName: String = "",
    val severity: Int = 0,
    val description: String = ""
)

sealed interface BienestarUiState {
    data object Loading : BienestarUiState

    data class Ready(
        val activeDiscomforts: List<DiscomfortWithTags>,
        val resolvedDiscomforts: List<DiscomfortWithTags>,
        val adaptationHistory: List<AdaptationLogEntity>,
        val bodyZones: List<BodyZoneEntity> = emptyList(),
        val discomfortFormState: DiscomfortFormState = DiscomfortFormState()
    ) : BienestarUiState

    data class Error(val message: String) : BienestarUiState
}
