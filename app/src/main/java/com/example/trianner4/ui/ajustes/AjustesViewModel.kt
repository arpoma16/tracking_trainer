package com.example.trianner4.ui.ajustes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trianner4.data.backup.ExportBackupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AjustesViewModel @Inject constructor(
    private val exportBackupUseCase: ExportBackupUseCase
) : ViewModel() {

    private val _backupJson = MutableSharedFlow<String>()
    val backupJson = _backupJson.asSharedFlow()

    fun onExportBackup() {
        viewModelScope.launch {
            val json = exportBackupUseCase.execute()
            _backupJson.emit(json)
        }
    }
}
