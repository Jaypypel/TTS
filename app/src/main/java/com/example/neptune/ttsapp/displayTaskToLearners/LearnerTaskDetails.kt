package com.example.neptune.ttsapp.displayTaskToLearners

import MessageType
import UserMessageResponse
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.neptune.ttsapp.DisplayQueriesAgainstTaskActivity
import com.example.neptune.ttsapp.EnumStatus.Status
import com.example.neptune.ttsapp.TaskSubmissionActivity
import com.example.neptune.ttsapp.UserMessageResponse
import com.example.neptune.ttsapp.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine

@Parcelize
data class TaskIdAndExpectedTime(
    val id: Long,
    val expectedTime: String
) : Parcelable

@HiltViewModel
class LearnerTaskDetailsViewModel @Inject
constructor( application: Application,
    private val taskRepository: TaskRepository,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val _learnerTaskDetails = MutableStateFlow(TaskDetails("","","","","",""))
    val learnerTaskDetails = _learnerTaskDetails
    val learnerTaskSummary = savedStateHandle.get<TaskSummary>("taskSummary")
    private val _learnerTaskSummary = MutableStateFlow(learnerTaskSummary ?: TaskSummary("","","","","",false,0));
    val taskLearnerSummary = _learnerTaskSummary


    private val _userMessage = MutableStateFlow(UserMessageResponse("", type = MessageType.INFO))
    val userMessage = _userMessage

    init {
        loadTaskDetails()
    }

    fun moveToTaskQueriesScreen(context: Context, intent: Intent){
            intent.putExtra(
                "task_id",
                Math.toIntExact(taskLearnerSummary.value.id.toLong())
            )
            intent.putExtra("task_status",taskLearnerSummary.value.taskOwner)
            context.startActivity(intent)
    }


    fun updateTaskStatus(id: Long, status:String){
        viewModelScope.launch {
            try {
                  val result = isTaskUpdated(id,status);
                if(result=="updated"){
                    _userMessage.value = UserMessageResponse("Task Accepted",MessageType.SUCCESS)
                }else{
                    _userMessage.value = UserMessageResponse("Task not accepted due to error",MessageType.ERROR)
                }
            } catch (e: Exception) {
                _userMessage.value = UserMessageResponse("Error occurred while loading task details: ${e.message}",MessageType.ERROR)
            }
        }
    }

    private suspend fun isTaskUpdated(id: Long, status: String): String =
    suspendCoroutine { continuation ->
        taskRepository.updateTaskStatus(id,status).whenComplete {
                result, exception ->
            if (exception != null) continuation.resumeWith(Result.failure(exception))
            else continuation.resumeWith(Result.success(result))
        }
    }

    fun clearMessage(){
        _userMessage.value = UserMessageResponse("",MessageType.INFO)
    }
    fun moveToTaskSubmissionScreen(context: Context, intent: Intent){
        val taskIdAndExpectedTime = TaskIdAndExpectedTime(taskLearnerSummary.value.id.toLong(),learnerTaskDetails.value.expectedMinutes)
        intent.putExtra("id_and_time",taskIdAndExpectedTime)
        context.startActivity(intent)
    }

    private fun loadTaskDetails() {
        viewModelScope.launch {
            ->
            try {
                _learnerTaskDetails.value = getTaskDetails(taskLearnerSummary.value.id.toLong())
            } catch (e: Exception) {
               _userMessage.value = UserMessageResponse("Error occurred while loading task details: ${e.message}",MessageType.ERROR)
            }
        }
    }

    private suspend fun getTaskDetails(id: Long): TaskDetails =
        suspendCoroutine { continuation ->
            taskRepository.getTaskDetails(id).whenComplete {
                    result, exception ->
                if (exception != null) continuation.resumeWith(Result.failure(exception))
                else continuation.resumeWith(Result.success(result))
                }
        }
}



@Composable
fun LearnerTaskDetailsScreen(viewModel: LearnerTaskDetailsViewModel){
    val context: Context = LocalContext.current;
    val taskSummary by viewModel.taskLearnerSummary.collectAsState()
    val taskDetails by viewModel.learnerTaskDetails.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    LearnerTaskDetails(taskSummary,taskDetails,viewModel,context)
    UserMessageResponse(
        message = userMessage.message,
        type = userMessage.type,
        onDismiss = {
            viewModel.clearMessage()
        }
    )
}


@Composable
fun LearnerTaskDetails(taskSummary: TaskSummary,taskDetails: TaskDetails, viewModel: LearnerTaskDetailsViewModel,context: Context){
    val intent = Intent(context, DisplayQueriesAgainstTaskActivity::class.java)
    val taskIntent = Intent(context, TaskSubmissionActivity::class.java)
    Column(modifier = Modifier.fillMaxWidth()) {
        TaskSummary(taskSummary = taskSummary)
        TaskDetails(taskDetails = taskDetails)
        Row(Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
//            if(taskSummary.taskOwner == "Pending"){
            Button(onClick = {viewModel.updateTaskStatus(taskSummary.id.toLong(), Status.Accepted.name)}, modifier = Modifier
                .padding(5.dp)
                .weight(0.5f)) { Text("Accept")}
//            }
            Button(onClick = {viewModel.moveToTaskQueriesScreen(context,intent)}, modifier = Modifier
                .padding(5.dp)
                .weight(0.5f)) { Text("Task Query- TQ")}
        }
        Button(onClick = {viewModel.moveToTaskSubmissionScreen(context,taskIntent)}, modifier = Modifier.fillMaxWidth().padding(16.dp)) { Text("Task submission")}

    }
}



@Composable
fun TaskSummary(taskSummary: TaskSummary){
    Row(Modifier
        .fillMaxWidth()
        .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Text(taskSummary.id,fontWeight = FontWeight.Bold )
        Text(taskSummary.assignedOn.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), fontWeight = FontWeight.Bold)
    }
    Row(Modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        Text(taskSummary.name)
    }
}



@Composable
fun  TaskDetails(taskDetails: TaskDetails){

    if(taskDetails.processActionOne.isNotEmpty() && taskDetails.processActionTwo.isNotEmpty() && taskDetails.processActionThree.isNotEmpty()
        )
    {

        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Text("Process Expected", modifier = Modifier.padding(bottom = 8.dp),fontWeight = FontWeight.Bold)
            Text("1. "+taskDetails.processActionOne)
            Text("2. " +taskDetails.processActionTwo)
            Text("3. "+taskDetails.processActionThree)
        }
    }else {
        Row(Modifier
            .fillMaxWidth()
            .padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Process Expected",fontWeight = FontWeight.Bold)
            Text("Not Provided")
        }


    }

    Row(Modifier
        .fillMaxWidth()
        .padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Expected Task Making",fontWeight = FontWeight.Bold)
        Text(text = taskDetails.making)
    }


    Row(Modifier
        .fillMaxWidth()
        .padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Expected Task Making Type",fontWeight = FontWeight.Bold)
        Text(taskDetails.makingType)
    }



    Row(Modifier
        .fillMaxWidth()
        .padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Expected Time",fontWeight = FontWeight.Bold)
        Text(taskDetails.expectedMinutes+" Minutes")
    }
}
