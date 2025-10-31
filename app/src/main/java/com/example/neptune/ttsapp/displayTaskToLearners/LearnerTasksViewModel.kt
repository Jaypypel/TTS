//package com.example.neptune.ttsapp.displayTaskToLearners
//
//import android.app.Application
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.viewModelScope
//import androidx.paging.PagingData
//import androidx.paging.cachedIn
//import com.example.neptune.ttsapp.SessionManager
//import com.example.neptune.ttsapp.repository.TaskRepository
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.Flow
//import javax.inject.Inject
//
//@HiltViewModel
//class LearnerTasksViewModel @Inject constructor(
//    application: Application,
//    private val taskRepository: TaskRepository,
//) : AndroidViewModel(application) {
//
//    private val taskAssignee: String by lazy {
//        val sessionManager = SessionManager(application.applicationContext)
//        sessionManager.username ?: ""
//    }
//
//    fun getTasks(): Flow<PagingData<TaskSummary>> {
//        return taskRepository.getLearnerTasksStream(taskAssignee, "Pending").cachedIn(viewModelScope)
//    }
//}
