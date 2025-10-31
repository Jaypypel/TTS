package com.example.neptune.ttsapp.displayTaskToLearners


import MessageType
import SuggestionInputFieldState
import UserMessageResponse
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.neptune.ttsapp.DropDown
import com.example.neptune.ttsapp.SessionManager
import com.example.neptune.ttsapp.UserMessageResponse
import com.example.neptune.ttsapp.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine

//@Parcelize
//data class TaskSummary(
//
//    val id: String,
//    val assignedOn: String,
//    val name: String,
//    val status: String,
//    val queryResolved: Boolean,
//    val serialNo: Int,
//) : Parcelable
//
//
//
//
//@Parcelize
//data class TaskDetails(
//    val id: String,
//    val assignedOn: String,
//    val name: String,
//    val status: String,
//    val queryResolved: Boolean,
//    val serialNo: Int,
//) : Parcelable




@HiltViewModel
class LearnerAcceptedTasksViewModel @Inject constructor(

    application: Application,
    private val taskRepository: TaskRepository,

    ) : AndroidViewModel(application){
    val size  = MutableStateFlow(0)
    private val _taskSummary = MutableStateFlow(List(size.value){ TaskSummary("",
        "","",
        "",
        "",
        false,
        0)})
    val taskSummary = _taskSummary

    private val _userMessage = MutableStateFlow(UserMessageResponse("", type = MessageType.INFO))
    val userMessage = _userMessage


    private val _status = MutableStateFlow(List(size.value){SuggestionInputFieldState()})
    val status = _status

    val statuses =   listOf("IP","SC","CS","HD","VS")

    val statusFullForms = hashMapOf<String,String>()
    init  {
        statusFullForms["YTS"] = "Yet to Start"
        statusFullForms["IP"] = "In_process"
        statusFullForms["SC"] = "Submitted Checking"
        statusFullForms["CS"] = "Completed Sealed"
        statusFullForms["HD"] = "Hold"
        statusFullForms["VS"] = "Versioned"
    }
    val roles: Set<String>? by lazy {
        val sessionManager = SessionManager(application.applicationContext)
        sessionManager.roles;
    }

    fun onItemSelected(input : String, index: Int){
        val currentList = status.value.toMutableList()
        val currentTaskSummary = taskSummary.value.toMutableList()
        currentList[index] = currentList[index].copy(text = input, expanded = false)
//        val updateList = currentList;
        if(hasLearnerSelectedWrongTaskStatus(input)){
            setDefaultStatusStateAndShowMessageToUser(currentList, index, "Oops, You can't select this")
//            userMessage.value = UserMessageResponse("Oops, You can't select this", MessageType.INFO)
//            currentList[index] = currentList[index].copy("Select")
//            status.value = currentList
            return
        }
        if(moreThanOneSelected(currentList)) {
            setDefaultStatusStateAndShowMessageToUser(currentList, index, "Oops, Only one selection allowed")
//            currentList[index] = currentList[index].copy(text = "Select", expanded = false)
//            userMessage.value = UserMessageResponse("Oops, Only one selection allowed", MessageType.INFO)
//            status.value = currentList
            return
        }
        currentTaskSummary[index] = currentTaskSummary[index].copy(status = input)
        taskSummary.value = currentTaskSummary
        status.value = currentList
    }


    private fun setDefaultStatusStateAndShowMessageToUser(currentList: MutableList<SuggestionInputFieldState>, index: Int,message: String) {
        currentList[index] = currentList[index].copy(text = "Select", expanded = false)
        userMessage.value = UserMessageResponse(message, MessageType.INFO)
        status.value = currentList
    }


    fun showSuggestions(index: Int){
        val currentList = status.value.toMutableList()
        currentList[index] = currentList[index].copy( expanded = true, suggestions = statuses)
        status.value = currentList

    }

    fun moreThanOneSelected(currentList: MutableList<SuggestionInputFieldState>): Boolean {
        return currentList.count{it.text == "IP" || it.text == "SC"} >=2
    }

    fun hasLearnerSelectedWrongTaskStatus(input: String): Boolean{
        return roles?.contains("ROLE_LEARNER") == true && input == "CS" || input == "HD" || input == "VS"
    }

    fun allowEdit(){
      val upatedState =  status.value.map { suggestionInputFieldState ->
            Log.e("LearnerAcceptedTasksViewModel", "allowEdit: ${suggestionInputFieldState.text}")
            suggestionInputFieldState.copy(text = "Select ",isActive = true)
            }
        status.value = upatedState
    }

    val taskAssignee: String? by lazy {
        val sessionManager = SessionManager(application.applicationContext)
        sessionManager.username
    }



    init {
        loadTaskSummary()
    }

    fun clearMessage(){
        _userMessage.value = UserMessageResponse("",MessageType.INFO)
    }

    private fun loadTaskSummary() {
        viewModelScope.launch {
            try {
                val taskSummaryList = getTaskSummary()
                size.value=taskSummaryList.size
                taskSummary.value = taskSummaryList
                status.value = List(taskSummaryList.size){SuggestionInputFieldState()}
            } catch (e: Exception) {
                // Handle error
                userMessage.value = UserMessageResponse("Error occurred while loading task summary: ${e.message}",MessageType.ERROR)
            }
        }
    }

    private suspend fun getTaskSummary(): List<TaskSummary> =
        suspendCoroutine { continuation ->
            taskRepository
                .getTasksAssignToLearner(taskAssignee,"Pending")
                .whenComplete { result, exception ->
                if (exception != null) continuation
                    .resumeWith(Result.failure(exception))
                 else continuation
                     .resumeWith(Result.success(result))

            }
        }



    fun updateTaskStatus() {
        val taskSummaryList = taskSummary.value.toMutableList();
        if(taskSummaryList.count { it.status == "IP" || it.status == "SC" } == 0 ){
            userMessage.value = UserMessageResponse("No task status changed", MessageType.INFO)
            return
        }
        val taskSummary: TaskSummary = taskSummaryList.first { taskSummary -> taskSummary.status == "IP" || taskSummary.status == "SC" }
        val taskStatus = when (taskSummary.status) {
                "IP" -> statusFullForms.get("IP")
                "SC" -> statusFullForms.get("SC")
                else -> ""
        }
        Log.e("LearnerAcceptedTasksViewModel", "updateTaskStatus: $taskSummary")
        viewModelScope.launch {
            try {
                val result = isTaskUpdated(taskSummary.id.toLong(),taskStatus);
                if(result=="Success"){
                    _userMessage.value = UserMessageResponse("Task status changed",MessageType.SUCCESS)
                    status.value = taskSummaryList.map { taskSummary -> SuggestionInputFieldState(text = taskSummary.status) };
                }else{
                    _userMessage.value = UserMessageResponse("Task not changed due to error",MessageType.ERROR)
                }
            } catch (e: Exception) {
                _userMessage.value = UserMessageResponse("Error occurred while loading task details: ${e.message}",MessageType.ERROR)
            }
        }
    }


    private suspend fun isTaskUpdated(id: Long, status: String?): String =
        suspendCoroutine { continuation ->
            taskRepository.updateTaskStatus(id,status).whenComplete {
                    result, exception ->
                if (exception != null) continuation.resumeWith(Result.failure(exception))
                else continuation.resumeWith(Result.success(result))
            }
        }
}

@Composable
fun LearnerAcceptedTasks(viewModel: LearnerAcceptedTasksViewModel){
    val taskSummary by viewModel.taskSummary.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val status by viewModel.status.collectAsState()
    Column(modifier = Modifier.fillMaxWidth() ) {
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text(text = viewModel.taskAssignee.toString())
            CurrentDateDisplayDDMMYY()
        }

        Row(horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(10.dp)) {
            Text("Sr \nNo.", modifier = Modifier.weight(1f))
            Text("TR Code.",Modifier.weight(1f))
            Text("Task \nDate",modifier = Modifier.weight(2.5f))
            Text("Task \nName",modifier = Modifier.weight(3.5f))
            Text("Task \nStatus", modifier = Modifier.weight(1.5f))

        }
        LazyColumn(Modifier.fillMaxWidth().fillMaxHeight(0.9f)) {
            items(taskSummary.size) { index ->
                LearnerAcceptedTaskUIState(
                    index,
                    taskSummary[index],
                    status[index],
                    viewModel
                )
            }
        }
        UserMessageResponse(
            message = userMessage.message,
            type = userMessage.type,
            onDismiss = {
                viewModel.clearMessage()
            }
        )
        Row( Modifier.fillMaxWidth(),
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround) { Button(onClick = {viewModel.updateTaskStatus()},
            enabled = status.count { it.text == "IP" || it.text == "SC" } > 0){
            Text("Done")}
            Button(onClick = { viewModel.allowEdit() }) {Text("Edit") }
        }


    }
}



@SuppressLint("SuspiciousIndentation")
@Composable
fun LearnerAcceptedTaskUIState(index: Int,
                               taskSummary: TaskSummary,
                               status: SuggestionInputFieldState,
                               viewModel: LearnerAcceptedTasksViewModel){
    val  context: Context = LocalContext.current
    Row(modifier = Modifier
        .fillMaxWidth()
        .border(border = BorderStroke(1.dp, Color.Black), shape = RectangleShape)
        .padding(vertical = 15.dp)
        .clickable(enabled = false) {

        }
        ,horizontalArrangement = Arrangement.Absolute.SpaceBetween, verticalAlignment = CenterVertically) {
        if(taskSummary.queryResolved){
            Text(text = index.toString(),modifier = Modifier.weight(1f))
        }else {
            Text(text = index.toString(), modifier = Modifier.background(color = Color.Red).weight(1f))
        }

        Text(text = taskSummary.id,Modifier.weight(1f))
        Text(text = taskSummary.assignedOn,Modifier.weight(1.5f))
        Text(text = taskSummary.name,Modifier.weight(3f))
//        status.copy(text = taskSummary.taskOwner,expanded = false, isActive = false)
//        Text(text = taskSummary.taskOwner,Modifier.weight(1.5f))
        DropDown(
            input = status,
            onItemSelected = {viewModel.onItemSelected(it,index)} ,
            showSuggestions = {viewModel.showSuggestions(index)},
            modifier = Modifier.weight(1.5f),
            defaultState = taskSummary.status
        )
    }
}

//@Composable
//fun CurrentDateDisplayDDMMYY() {
//    // State to hold the formatted date string
//    var formattedDate by remember { mutableStateOf("00.00.00") } // Initial placeholder
//
//    // Use LaunchedEffect to get the date once when the composable enters composition.
//    // Date usually doesn't need to update every second like time.
//    LaunchedEffect(Unit) { // Keyed with Unit to run once
//        val currentDate = LocalDate.now()
//
//        // Define the formatter for "dd.MM.yy" (e.g., 13.09.23)
//        // val formatter = DateTimeFormatter.ofPattern("dd.MM.yy")
//
//        // Or for "dd.MM.yyyy" (e.g., 13.09.2023)
//        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
//
//        formattedDate = currentDate.format(formatter)
//    }
//
//    Text(text = formattedDate)
//}