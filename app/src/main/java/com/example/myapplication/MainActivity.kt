package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.myapplication.data.repository.InMemoryTaskRepository
import com.example.myapplication.domain.usecase.AddTaskUseCase
import com.example.myapplication.domain.usecase.GetTasksUseCase
import com.example.myapplication.ui.presentation.tasks.AcademicTaskApp
import com.example.myapplication.ui.presentation.tasks.AcademicTaskViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // 1. Orquestación manual de dependencias de infraestructura y negocio
        val repository = InMemoryTaskRepository()
        val getTasksUseCase = GetTasksUseCase(repository)
        val addTaskUseCase = AddTaskUseCase(repository)
        // 2. Creación del ViewModel inyectando sus colaboradores requeridos
        val viewModel = AcademicTaskViewModel(getTasksUseCase, addTaskUseCase, repository)
        // 3. Renderización de la vista declarativa limpia
        setContent {
            AcademicTaskApp(viewModel = viewModel)
        }
    }
}
