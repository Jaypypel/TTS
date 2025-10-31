package com.example.neptune.ttsapp.displayTaskToLearners


import MessageType
import SuggestionInputFieldState
import UserMessageResponse
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.neptune.ttsapp.SessionManager
import com.example.neptune.ttsapp.UserMessageResponse
import com.example.neptune.ttsapp.Util.Debounce
import com.example.neptune.ttsapp.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine

@Parcelize
data class TaskSummary(

    val id: String,
    val assignedOn: String,
    val name: String,
    val taskOwner: String,
    val status: String,
    val queryResolved: Boolean,
    val serialNo: Int,
        ) : Parcelable


data class TaskDetails(
    val processActionOne: String,
    val processActionTwo: String,
    val processActionThree: String,
    val making: String,
    val makingType: String,
    val expectedMinutes: String,
    )

@HiltViewModel
class LearnerTasksViewModel @Inject constructor(

    application: Application,
    private val taskRepository: TaskRepository,

    ) : AndroidViewModel(application){
    val size  = MutableStateFlow(0)
    private val _taskSummary = MutableStateFlow(List(size.value){ TaskSummary("","","","","",false,0)})
    val taskSummary = _taskSummary

    private val _userMessage = MutableStateFlow(UserMessageResponse("", type = MessageType.INFO))
    val userMessage = _userMessage


    private val _status = MutableStateFlow(SuggestionInputFieldState("YTS",emptyList(), expanded = false, isActive = false))
    val status = _status
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
                _taskSummary.value = taskSummaryList
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
                .whenComplete { result, exception -> if (exception != null) continuation
                    .resumeWith(Result.failure(exception))
                 else continuation.resumeWith(Result.success(result))
                }
        }
}

@Composable
fun LearnerTasks(viewModel: LearnerTasksViewModel){
    val taskSummary by viewModel.taskSummary.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val status by viewModel.status.collectAsState()
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text(text = viewModel.taskAssignee.toString())
            CurrentDateDisplayDDMMYY()
        }

        Row(horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(10.dp)) { Text("Sr no.", modifier = Modifier.weight(1f))
                Text("TR Code.",Modifier.weight(1f))
                Text("Task Date",modifier = Modifier.weight(1.5f))
                Text("Task Name",modifier = Modifier.weight(3f))
                Text("Mentor Name", modifier = Modifier.weight(1.5f))

        }
        LazyColumn(Modifier.fillMaxWidth()) {

            items(taskSummary.size) { index ->
                LearnerTaskUIState(index,taskSummary[index],status)
            }
        }
        UserMessageResponse(
            message = userMessage.message,
            type = userMessage.type,
            onDismiss = {
                viewModel.clearMessage()
            }
        )

    }
}

@SuppressLint("SuspiciousIndentation")
@Composable
fun LearnerTaskUIState(index: Int, taskSummary: TaskSummary, status: SuggestionInputFieldState){
  val  context: Context = LocalContext.current
    Row(modifier = Modifier
        .fillMaxWidth()
        .border(border = BorderStroke(1.dp, Color.Black), shape = RectangleShape)
        .padding(vertical = 15.dp)
        .clickable(enabled = true) {
            Debounce.debounceEffect{
                val intent = Intent(context, TaskToLearnerDetailsActivity::class.java)
                intent.putExtra("taskSummary", taskSummary)
                context.startActivity(intent)
            }
        },
        horizontalArrangement = Arrangement.Absolute.SpaceBetween, verticalAlignment = CenterVertically) {
        if(taskSummary.queryResolved){
            Text(text = index.toString(),modifier = Modifier.weight(1f))
        }else {
            Text(text = index.toString(), modifier = Modifier.background(color = Color.Red).weight(1f))
        }

        Text(text = taskSummary.id,Modifier.weight(1f))
        Text(text = taskSummary.assignedOn,Modifier.weight(1.5f))
        Text(text = taskSummary.name,Modifier.weight(3f))
        Text(text = taskSummary.taskOwner,Modifier.weight(1.5f))
//        val list = listOf("IP","SC","CS","HD","VS")
//        DropDown(
//            input = status,
//            onItemSelected = {inpt -> status.copy(text = inpt,expanded = false, isActive = true)} ,
//            showSuggestions = {status.copy(expanded =true, suggestions = list)},
//            modifier = Modifier.weight(1f), defaultState = "NA"
//        )
    }
}

@Composable
fun CurrentDateDisplayDDMMYY() {
    // State to hold the formatted date string
    var formattedDate by remember { mutableStateOf("00.00.00") } // Initial placeholder

    // Use LaunchedEffect to get the date once when the composable enters composition.
    // Date usually doesn't need to update every second like time.
    LaunchedEffect(Unit) { // Keyed with Unit to run once
        val currentDate = LocalDate.now()

        // Define the formatter for "dd.MM.yy" (e.g., 13.09.23)
        // val formatter = DateTimeFormatter.ofPattern("dd.MM.yy")

        // Or for "dd.MM.yyyy" (e.g., 13.09.2023)
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

        formattedDate = currentDate.format(formatter)
    }

    Text(text = formattedDate)
}