package com.example.myapplication.ui.presentation.tasks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.repository.AcademicTaskRepository
import com.example.myapplication.domain.usecase.AddTaskUseCase
import com.example.myapplication.domain.usecase.GetTasksUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AcademicTaskViewModel(
    private val getTasksUseCase: GetTasksUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val repository: AcademicTaskRepository
) : ViewModel() {
    // 1. Estado mutable interno oculto e inaccesible desde el exterior
    private val _uiState = MutableStateFlow<AcademicTaskUiState>(AcademicTaskUiState.Loading)
    // 2. Estado inmutable expuesto públicamente de solo lectura para cumplir con UDF
    val uiState: StateFlow<AcademicTaskUiState> = _uiState.asStateFlow()

    // Cache the last successful tasks to recover from errors
    private var lastSuccessTasks: List<AcademicTask> = emptyList()

    // Gestión segura de la navegación de presentación y campos locales mutables
    var currentScreen by mutableStateOf(ScreenType.LIST)
        private set

    var newTaskTitle by mutableStateOf("")
        private set

    init {
        observeTasks()
    }

    private fun observeTasks() {
        viewModelScope.launch {
            try {
                // Consumo asíncrono y reactivo del flujo del caso de uso
                getTasksUseCase().collect { taskList ->
                    lastSuccessTasks = taskList
                    _uiState.value = AcademicTaskUiState.Success(taskList)
                }
            } catch (e: Exception) {
                _uiState.value = AcademicTaskUiState.Error("Error crítico al cargar el listado: ${e.localizedMessage}")
            }
        }
    }

    fun onTaskCheckedChange(taskId: String) {
        viewModelScope.launch {
            try {
                repository.toggleTaskCompletion(taskId)
            } catch (e: Exception) {
                _uiState.value = AcademicTaskUiState.Error("No se pudo actualizar el estado de la tarea.")
            }
        }
    }

    fun onNavigateToCreate() {
        newTaskTitle = ""
        currentScreen = ScreenType.CREATE
        if (_uiState.value is AcademicTaskUiState.Error) {
            _uiState.value = AcademicTaskUiState.Success(lastSuccessTasks)
        }
    }

    fun onNavigateToList() {
        currentScreen = ScreenType.LIST
        if (_uiState.value is AcademicTaskUiState.Error) {
            _uiState.value = AcademicTaskUiState.Success(lastSuccessTasks)
        }
    }

    fun onTaskTitleChange(newTitle: String) {
        newTaskTitle = newTitle
    }

    fun onSaveTask() {
        if (newTaskTitle.isBlank()) return
        viewModelScope.launch {
            try {
                addTaskUseCase(newTaskTitle)
                currentScreen = ScreenType.LIST
                newTaskTitle = ""
            } catch (e: IllegalArgumentException) {
                _uiState.value = AcademicTaskUiState.Error(e.message ?: "Dato inválido")
                currentScreen = ScreenType.LIST
            } catch (e: Exception) {
                _uiState.value = AcademicTaskUiState.Error("Error al guardar: ${e.localizedMessage}")
                currentScreen = ScreenType.LIST
            }
        }
    }
}
