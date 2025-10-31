package com.example.neptune.ttsapp

import MessageType
import ProcessInputField
import SuggestionInputFieldState
import android.app.Application
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.neptune.ttsapp.Util.Debounce
import com.example.neptune.ttsapp.displayTaskToLearners.TaskIdAndExpectedTime
import com.example.neptune.ttsapp.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine

@Composable
fun TaskSubmission(viewModel: TaskSubmissionViewModel){
    val inputProcessField by viewModel.inputProcessField.collectAsState()
    val inputExpectedMintues by viewModel.inputActualMintues.collectAsState()
    val inputTaskMakingVersion by viewModel.inputTaskMakingVersion.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState();


    Column(Modifier.fillMaxWidth().padding(8.dp)){
        Text(text = "Task Submission", Modifier.padding(16.dp), textAlign = TextAlign.Center,fontWeight = FontWeight.Bold)
        Text(text = viewModel.getTaskId().toString(), Modifier.padding(16.dp))
        Text(text = "Process Done", Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
        ProcessExpected(inputProcessField,viewModel)
        CommentaryOrExperienceOnTask(3,inputProcessField,
            {viewModel.onInputChange(3,it)},
            {viewModel.onSuggestionSelected(3,it)}
        )
        ConsumedTime(inputExpectedMintues,viewModel::onConsumedTimeChange)
//        Text(text = "Difference: "+viewModel.differenceBetweenExpectedTimeAndActualTime(inputExpectedMintues.toString()).toString(),
//            modifier = Modifier.padding(16.dp))

        Text(text = "Task Making Delivered", fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween){
            Text("As assigned")
            DropDown(
                inputTaskMakingVersion, viewModel::onItemSelected, viewModel::showSuggestions, defaultState = "Select"
            )
            Button(onClick = {Debounce.debounceEffect {  }}) { Text("Done")}
//            Text("As assigned")
        }
        Row(modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween,verticalAlignment = Alignment.CenterVertically){
            Button(onClick = {
                Debounce.debounceEffect {  viewModel.submitTask() }
            }) {Text("Submit")}
            Text("Not Received")

        }


        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("MakingCodeOfVersion1", fontWeight = FontWeight.Bold)
            Button(onClick = {})  {Text("New Version") }
        }
        UserMessageResponse(userMessage.message,userMessage.type)
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("FinalMakingCode", fontWeight = FontWeight.Bold)
            Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = Red))  {Text("Seal") }
        }

    }
    
}


@Composable
fun ProcessExpected(suggestion: List<SuggestionInputFieldState>, viewModel: TaskSubmissionViewModel){
    repeat(3) {  index ->
        ProcessInputField(
            label = (index+1).toString(),
            onInputChange = {viewModel.onInputChange(index,it)},
            value = suggestion[index],
            onSuggestionSelected ={ viewModel.onSuggestionSelected(index,it)},
            label1 = "input expected"
        )
    }
}


@Composable
fun DropDown(input: SuggestionInputFieldState,
                      onItemSelected: (String) -> Unit,
                      showSuggestions: () -> Unit,modifier: Modifier = Modifier, defaultState: String) {
    val text = input.text.ifEmpty {defaultState}


    var anchorBounds by remember { mutableStateOf<Rect?>(null) }
    var popupOffset by remember { mutableStateOf(IntOffset.Zero) }
    Box(modifier = modifier.onGloballyPositioned { coordinates ->
        val position = coordinates.positionInWindow()
        val size = coordinates.size
        popupOffset = IntOffset(position.x.toInt(), ((position.y + size.height)).toInt())
    }){
    Text(text = text,
        modifier = modifier.clickable(enabled = input.isActive,onClick = {showSuggestions() }
        ).padding(vertical = 16.dp, horizontal = 16.dp)
        .onGloballyPositioned { coordinates ->
        val position = coordinates.positionInWindow()
        anchorBounds = Rect(
            position.x,
            position.y,
            position.x + coordinates.size.width,
            position.y + coordinates.size.height
        )
    }.fillMaxWidth())
    if (input.expanded && anchorBounds != null) {
        Popup(
            alignment = Alignment.TopStart,
            offset = IntOffset(
                x = 0,
                y = 70
            ),

            properties = PopupProperties(focusable = false)
        ) {
            LazyColumn ( modifier = Modifier
                .heightIn(max = 400.dp)
                .padding(10.dp)){
                items(count = input.suggestions.size, key = { index -> input.suggestions[index] }) {
                    ListItem(
                        headlineContent = { Text(input.suggestions[it]) },
                        modifier = Modifier.padding(5.dp).clickable {
                            onItemSelected(input.suggestions[it])
                        }
                    )

                }
            }
        }
    }
    }
}




@Composable
fun CommentaryOrExperienceOnTask(index: Int,
                                 input: List<SuggestionInputFieldState>,
                                 updateCommentaryOrExperience: (String) -> Unit,
                                 selectSuggestion: (String) -> Unit){
    Text(text = "Experience or Commentary",fontWeight = FontWeight.Bold)
    ProcessInputField(
        label = "",
        onInputChange = {updateCommentaryOrExperience(it)},
        value = input[index],
        onSuggestionSelected = {selectSuggestion(it)},
        label1 = "experience"
    )
}
@Composable
fun ConsumedTime(input: ConsumedTimeState, onValueChange: (String) -> Unit){
   Row(modifier = Modifier.fillMaxWidth(),
       horizontalArrangement = Arrangement.SpaceBetween,
       verticalAlignment = Alignment.CenterVertically) {
       Text(text = "Consumed Time : ",fontWeight = FontWeight.Bold)
       OutlinedTextField(
       value = input.minutes.toString(), onValueChange = {onValueChange(it)},
       modifier = Modifier.fillMaxWidth(),
       keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
           , isError = input.minutes==0 && input.hasActiveState,
           supportingText = {if (input.minutes==0 && input.hasActiveState) Text("This field can't be zero")}
   ) }
}



@HiltViewModel
class TaskSubmissionViewModel
@Inject constructor (
    application: Application,
    savedStateHandle: SavedStateHandle,
    private val taskRepository: TaskRepository
    ): AndroidViewModel(application){


        val idAndTime = savedStateHandle.get<TaskIdAndExpectedTime>("id_and_time");
    private val _inputProcessField = MutableStateFlow(List(4) { SuggestionInputFieldState() })
    val inputProcessField = _inputProcessField

    private val _inputActualMintues = MutableStateFlow(ConsumedTimeState(0,false))
    val inputActualMintues = _inputActualMintues

    private val _inputTaskMakingVersion = MutableStateFlow(SuggestionInputFieldState())
    val inputTaskMakingVersion = _inputTaskMakingVersion

    private val _userMessage = MutableStateFlow(UserMessageResponse("",MessageType.INFO))
    val userMessage = _userMessage;

   val suggestions = listOf("Research", "DomainCombiner","Input Design", "Research Requirement",
       "collections","Requirement Gathering","Refinement Session","Review Meeting",
       "Report Writing")

    val versions = listOf("v1", "v2","v3","v4","v5");




    fun onItemSelected(input: String){
        _inputTaskMakingVersion.value = SuggestionInputFieldState(text = input, expanded = false)
    }

    fun showSuggestions(){
        _inputTaskMakingVersion.value = SuggestionInputFieldState(expanded = true, suggestions = versions)
    }
    fun differenceBetweenExpectedTimeAndActualTime(expectedTime: String): Int {
         return ((expectedTime.toIntOrNull()
            ?: (0 - _inputActualMintues.value.minutes)))
    }

    fun getTaskId(): Int {
        return idAndTime?.id?.toInt() ?: 0
    }

    fun onInputChange(index: Int, input: String){
        val currentList = _inputProcessField.value.toMutableList()
        val suggestions = suggestions
        val filteredSuggestions = if(input.length>=2) suggestions.filter { it.contains(input, ignoreCase = true) } else emptyList()
        currentList[index] = currentList[index].copy(text = input, suggestions = filteredSuggestions, expanded = filteredSuggestions.isNotEmpty(),isActive = true)
        _inputProcessField.value = currentList
    }

    fun onSuggestionSelected(index: Int, suggestion: String){
        val currentList = _inputProcessField.value.toMutableList()
        currentList[index] = currentList[index].copy(text = suggestion, suggestions = emptyList(), expanded = false,isActive = true)
        _inputProcessField.value = currentList
    }

    fun onConsumedTimeChange(input: String){
        _inputActualMintues.value = ConsumedTimeState(input.toIntOrNull() ?: 0, input.isNotEmpty())
    }

    fun submitTask() {
        val one = _inputProcessField.value[0].text
        val two = _inputProcessField.value[1].text
        val three = _inputProcessField.value[2].text
        val processSteps = listOf(one,two,three)
        val experienceOrCommentary = _inputProcessField.value[3].text
        val makingVersion = _inputTaskMakingVersion.value.text
        val actualTotalTime = _inputActualMintues.value.minutes.toString()
        val request = TaskSubmissionRequest(
            processPerformedSteps = processSteps,
            experienceOrCommentary = experienceOrCommentary,
            makingVersion = makingVersion,
            actualTotalTime = actualTotalTime
        )
        Log.e("TaskSubmissionViewModel", "Submitting task: $request")

        viewModelScope.launch {
            try {
                val status = isTaskSubmitted(getTaskId().toLong(),request)
                if(status == "success"){
                    _userMessage.value = UserMessageResponse("Task submitted successfully", MessageType.SUCCESS)
                }else {
                    _userMessage.value = UserMessageResponse("Task Submission Failed", MessageType.ERROR)
                }
            }catch (e: Exception){
                _userMessage.value = UserMessageResponse("Error occurred" + e.message, MessageType.ERROR)
            }

        }
    }

    private suspend fun isTaskSubmitted(id: Long,request: TaskSubmissionRequest): String =
        suspendCoroutine { continuation ->
            taskRepository.submitTask(id,request).whenComplete { result, exception ->
                if (exception != null) continuation.resumeWith(Result.failure(exception))
                else continuation.resumeWith(Result.success(result))
            }
        }








}


data class ConsumedTimeState(
    val minutes: Int,
    val hasActiveState: Boolean = false
)
data class TaskSubmissionRequest(
    var processPerformedSteps : List<String>,
    var experienceOrCommentary : String,
    var makingVersion: String,
    var actualTotalTime: String
)