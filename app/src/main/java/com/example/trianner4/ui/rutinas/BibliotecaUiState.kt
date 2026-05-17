package com.example.trianner4.ui.rutinas

import com.example.trianner4.data.local.entity.ExerciseEntity
import com.example.trianner4.data.local.entity.TagEntity

sealed interface BibliotecaUiState {
    object Loading : BibliotecaUiState
    data class Ready(
        val exercises: List<ExerciseEntity> = emptyList(),
        val searchQuery: String = "",
        val tags: List<TagEntity> = emptyList(),
        val showCreateDialog: Boolean = false
    ) : BibliotecaUiState
}
