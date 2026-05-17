package com.example.trianner4.ui.rutinas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trianner4.data.local.ExerciseType
import com.example.trianner4.data.local.TagCategory
import com.example.trianner4.data.local.TrackingMode
import com.example.trianner4.data.local.dao.ExerciseDao
import com.example.trianner4.data.local.dao.TagDao
import com.example.trianner4.data.local.entity.ExerciseEntity
import com.example.trianner4.data.local.entity.ExerciseTagEntity
import com.example.trianner4.data.local.entity.TagEntity
import com.example.trianner4.data.local.seeders.TagSeeder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BibliotecaViewModel @Inject constructor(
    private val exerciseDao: ExerciseDao,
    private val tagDao: TagDao
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _showCreateDialog = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            TagSeeder.seed(tagDao)
        }
    }

    val uiState: StateFlow<BibliotecaUiState> = combine(
        exerciseDao.observeAll(),
        tagDao.observeAll(),
        _searchQuery,
        _showCreateDialog
    ) { exercises, tags, query, showDialog ->
        val filtered = if (query.isBlank()) {
            exercises
        } else {
            exercises.filter { it.name.contains(query, ignoreCase = true) }
        }
        BibliotecaUiState.Ready(
            exercises = filtered,
            searchQuery = query,
            tags = tags,
            showCreateDialog = showDialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BibliotecaUiState.Loading
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun setShowCreateDialog(show: Boolean) {
        _showCreateDialog.value = show
    }

    fun createExercise(name: String, type: ExerciseType, selectedTags: List<TagEntity>) {
        viewModelScope.launch {
            val trackingMode = when (type) {
                ExerciseType.STRENGTH -> TrackingMode.WEIGHT_REPS
                ExerciseType.ASSISTIVE -> TrackingMode.FIXED_REPS
            }
            val exercise = ExerciseEntity(
                name = name,
                type = type,
                trackingMode = trackingMode
            )
            val id = exerciseDao.insert(exercise)
            
            val exerciseTags = selectedTags.map { tag ->
                ExerciseTagEntity(exerciseId = id, tagId = tag.id)
            }
            exerciseDao.insertTags(exerciseTags)
            
            _showCreateDialog.value = false
        }
    }
}
