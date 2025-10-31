package com.example.neptune.ttsapp

import android.app.Application
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.neptune.ttsapp.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine

data class LearnerTaskCompletionDetails(
    val commentaryOrExperience: String,
    val actualTotalTime: String,
    val expectedMaking: String,
    val makingVersion: String,
    val difference: String,
    val processActualSteps: List<String>
)

@HiltViewModel
class TaskCompletionViewModel @Inject constructor(
    application: Application,
    savedStateHandle: SavedStateHandle,
    private val taskRepository: TaskRepository
) : AndroidViewModel(application) {
    val username: String? by lazy {
        val sessionManager = SessionManager(application.applicationContext)
        sessionManager.username
    }
    val taskId: Long = savedStateHandle.get<Long>("id")!!
    private val _inputProcessField = MutableStateFlow(
        LearnerTaskCompletionDetails("",
            "",
            "",
            "",
            "",
            emptyList()
        )
    )
    val inputProcessField: StateFlow<LearnerTaskCompletionDetails> = _inputProcessField

    init {
        loadLearnerCompletionDetails(taskId,username.toString())
    }

    fun loadLearnerCompletionDetails(id: Long, username: String){
        viewModelScope.launch {
            try {
                _inputProcessField.value = getLearnerCompletionDetails(id,username)
            }catch (ex: Exception){
                Log.e("TaskCompletionViewModel","Error occurred : "+ex.message)
            }
        }
    }

    private suspend fun getLearnerCompletionDetails(id: Long, username: String): LearnerTaskCompletionDetails =
        suspendCoroutine { continuation ->
            taskRepository.getTaskCompletionsDetails(id,username).whenComplete { result, exception ->
                if (exception != null) continuation.resumeWith(Result.failure(exception))
                else continuation.resumeWith(Result.success(result))
            }
        }
}

@Composable
fun TaskCompletion(viewModel: TaskCompletionViewModel){

    val taskCompletionDetails by viewModel.inputProcessField.collectAsState()

    Column(Modifier.fillMaxWidth().padding(8.dp)){
        Text(text = "Task Submission", Modifier.padding(16.dp), textAlign = TextAlign.Center,fontWeight = FontWeight.Bold)
        Text(text = viewModel.taskId    .toString(), Modifier.padding(16.dp))
        Text(text = "1", Modifier.padding(16.dp))
        Text(text = "Process Done", Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
        ProcessDone(taskCompletionDetails.processActualSteps)
        CommentaryOrExperience(taskCompletionDetails.commentaryOrExperience)
        ConsumedTime(taskCompletionDetails.actualTotalTime)
        Difference(taskCompletionDetails.difference)
        MakingDetails(taskCompletionDetails.makingVersion,
            taskCompletionDetails.expectedMaking,
            "Done")

    }

}

@Composable
fun ProcessDone(processCompleted: List<String>){
    if(processCompleted.isEmpty()) return
    repeat(3){ index ->
        Text("${index+1} : ${processCompleted[index]}", modifier = Modifier.padding(bottom = 5.dp, start = 16.dp))
    }
}

@Composable
fun CommentaryOrExperience(commentaryOrExperience: String){
        Text("Commentary/Experience", fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp));
        Text(if(commentaryOrExperience.isNullOrBlank()) "N/A" else commentaryOrExperience, modifier = Modifier.padding(start = 16.dp))
}

@Composable
fun ConsumedTime(consumedTime: String){
    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.padding(16.dp).fillMaxWidth()) {
        Text("Consumed time", fontWeight = FontWeight.Bold);
        Text(if(consumedTime.isNullOrBlank()) "N/A" else consumedTime)
    }
}


@Composable
fun Grids(){
}

@Composable
fun Difference(difference: String){
    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.padding(16.dp).fillMaxWidth()) {
        Text("Difference", fontWeight = FontWeight.Bold);
        Text(if(difference.isNullOrBlank()) "N/A" else difference)
    }
}


@Composable
fun MakingDetails(makingVersion: String, expectedMaking: String,status: String){
    Text(text = "Task Making Delivered", fontWeight = FontWeight.Bold)
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween){
        Text(if(expectedMaking.isNullOrBlank()) "N/A" else expectedMaking)
        Text(text = if(makingVersion.isNullOrBlank()) "N/A" else makingVersion);
        Text(status)
    }
}

//
