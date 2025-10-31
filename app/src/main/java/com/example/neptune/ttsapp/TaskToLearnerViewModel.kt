package com.example.neptune.ttsapp

import DateInputFieldState
import MessageType
import SuggestionInputFieldState
import TimeInputFieldState
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.neptune.ttsapp.Util.js
import com.example.neptune.ttsapp.repository.TaskRepository
import com.example.neptune.ttsapp.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine


data class TaskAssignmentRequest(
    val mentorName: String,
    val learnerName: String,
    val taskName: String,
    val processSteps: List<String>,
    val making: String,
    val makingType: String,
    val expectedTime: String,  // Format: "HH:mm"
    val actualTime: String,
    val date: String        // Format: "yyyy-MM-dd"
)

data class UserMessageResponse(
    val message: String,
    val type: MessageType
)

@HiltViewModel
class TaskToLearnerViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val taskRepository: TaskRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _inputField  = MutableStateFlow(List(7){SuggestionInputFieldState()})
    val inputField  =  _inputField
    private val _timeField = MutableStateFlow(List(2){TimeInputFieldState()})
    private val _taskResponseCode = MutableStateFlow("");
    val taskResponseCode = _taskResponseCode;
    val timeField = _timeField
    private val _date = MutableStateFlow(DateInputFieldState())
    val date = _date


    private val _learnerSuggestions = MutableStateFlow<List<String>>(emptyList())
    val learnerSuggestions: StateFlow<List<String>> = _learnerSuggestions

    private val _userMessage = MutableStateFlow(UserMessageResponse("",MessageType.INFO))
    val userMessage = _userMessage

    private val _submit= MutableStateFlow(true);
    val submit = _submit;

//    private val _taskAssignee = MutableStateFlow<String>("")
//    val taskAssignee = _taskAssignee


    val makingType = listOf("Paper Model","Image","Video","Audio","Model","Sketch","Writing","Comp file");


    fun showSuggestions(index: Int){
        val inputStates = _inputField.value.toMutableList()
        inputStates[index] = inputStates[index].copy("",makingType,true, isActive = true)
        inputField.value = inputStates
    }

    fun activateDropDown(index: Int){
        val inputStates = _inputField.value.toMutableList()
        inputStates[index] = inputStates[index].copy(isActive = true)
        inputField.value = inputStates

    }

    fun showSuccess(msg: String){
        userMessage.value = UserMessageResponse(msg,MessageType.SUCCESS)
    }
    fun showError(msg: String, e: Exception){
        userMessage.value = UserMessageResponse(msg+e.message,MessageType.ERROR)
    }

    fun clearMessage(){
        userMessage.value = UserMessageResponse("",MessageType.INFO)
    }

    val taskAssignee: String? by lazy {
        val sessionManager = SessionManager(application.applicationContext)
        sessionManager.username
    }

    init {
        loadLearnerSuggestions()
    }

    private fun loadLearnerSuggestions() {
        viewModelScope.launch {
            try {
                val usernames = getUsernamesAsync()
                _learnerSuggestions.value = usernames
            } catch (e: Exception) {
                showError("Error occurred while fetching usernames",e)
//                userMessage.value = UserMessageResponse("Error occurred while fetching usernames" + e.message, MessageType.ERROR)
            }
        }
    }

    private suspend fun getUsernamesAsync(): List<String> =
        suspendCoroutine { continuation ->
            userRepository.getUsernames().whenComplete { result, exception ->
                if (exception != null) continuation.resumeWith(Result.failure(exception))
                else continuation.resumeWith(Result.success(result))
            }
        }


    private fun isValidated(): Boolean {
        if(inputField.value[6].text=="Select"){
            userMessage.value = UserMessageResponse("Select making type", MessageType.ERROR)
            return false
        }

        return inputField.value.isNotEmpty() &&
                inputField.value.all {
                    it.text.isNotEmpty() && it.text.length > 2 && js.text_input_regex.matches(it.text)
                } && timeField.value.any() { it.minutes!=0 }
    }

    fun submitTaskAssignment() {
        _submit.value = false;
        val inputs = _inputField.value
        val times = _timeField.value
        val date = _date.value

        val convertedDate: Date = Date.from(date.date.atStartOfDay(ZoneId.systemDefault()).toInstant())
        val currentDate: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(convertedDate )

        val learner = inputs[0].text
        val task = inputs[1].text.trim()
        val processSteps = listOf(inputs[2].text.trim(), inputs[3].text.trim(), inputs[4].text.trim())
        val making = inputs[5].text.trim()
        val makingType = inputs[6].text;

        val startTime = "${times[0].minutes}"
        val endTime = "${times[1].hours}:${times[1].minutes}"
        val request = TaskAssignmentRequest(
            mentorName = taskAssignee.toString(),
            learnerName = learner,
            taskName = task,
            processSteps = processSteps,
            making = making,
            makingType = makingType,
            expectedTime = startTime,
            actualTime = endTime,
            date = currentDate
        )
        Log.d("Submit", "Submitting: $request")

        if(isValidated()){


            Log.d("Submit", "Submitting: $request")

            // Optionally call repository to send to API
            viewModelScope.launch {
                try {
                    val status = isTaskSubmitted(request);
                    if(status[0] == "Successful") {
                        //userMessage.value = UserMessageResponse("Task assigned successfully", MessageType.SUCCESS)
                        showSuccess("Task assigned successfully")
                        submit.value=true
                        taskResponseCode.value = status[1];
                    }else{
                        userMessage.value = UserMessageResponse("Task assigned failed", MessageType.ERROR)
                        submit.value=true
                    }
                }catch (e: Exception){
                    showError("Error occurred",e)
                    submit.value=true;
                    //userMessage.value = UserMessageResponse("Error occurred" + e.message, MessageType.ERROR)
                }
            }

        }else{
            userMessage.value= UserMessageResponse("Fill all the fields with Only letters, numbers, spaces, and . , | ? ' \" ( ) ", MessageType.ERROR)
            submit.value=true;
        }
    }

    private suspend fun isTaskSubmitted(request: TaskAssignmentRequest): ArrayList<String> =
        suspendCoroutine { continuation ->
            taskRepository.assignTaskToUser(request).whenComplete { result, exception ->
                if (exception != null) continuation.resumeWith(Result.failure(exception))
                else continuation.resumeWith(Result.success(result))
            }
        }

    var taskSuggestions = listOf(
        "Complete Module 1", "Complete Module 13",
        "Complete Module 2", "Complete Module 15",
        "Complete Module ", "Complete Module 18",
        "Complete Module 20", "Complete Module 19",
    )
    val makingSuggestions = listOf(
        "Poster Presentation",
        "Prototype Demo",
        "Proof of Concept",
        "Portfolio Submission",
        "Pitch Video"
    )

    val processSuggestions = listOf(
        "Research",
        "Requirement Gathering",
        "Refinement Session",
        "Review Meeting",
        "Report Writing"
    )


    fun updateHours(hours: Int,index: Int){
        val newTimeField = _timeField.value.toMutableList()
        newTimeField[index] = newTimeField[index].copy(hours = hours, hasActiveState = true )
        _timeField.value = newTimeField
    }
    fun updateMinute(minutes: Int, index: Int ){
        val newTimeField = _timeField.value.toMutableList()
        newTimeField[index] = newTimeField[index].copy(minutes = minutes, hasActiveState = true)
        _timeField.value = newTimeField
    }

    fun  onInputChange(index: Int, input: String){
        val currentList = _inputField.value.toMutableList();
        val suggestions = when(index){
            0 -> learnerSuggestions.value
            1 -> taskSuggestions
            5 -> makingSuggestions
            else -> processSuggestions
        }
        val filteredSuggestions = if(input.length>=2) suggestions.filter { it.contains(input, ignoreCase = true) } else emptyList()
        currentList[index] = currentList[index].copy(text = input, suggestions = filteredSuggestions, expanded = filteredSuggestions.isNotEmpty(),isActive = true)
        _inputField.value = currentList
    }


    fun showDatePicker() {
        _date.value = _date.value.copy(datePicker = true)
    }

    fun hideDatePicker() {
        _date.value = _date.value.copy(datePicker = false)
    }

    fun updateDateValue(newDate: LocalDate) {

        _date.value = _date.value.copy(date = newDate)
    }

    fun onSuggestionSelected(index: Int, selected: String) {
        val currentList = _inputField.value.toMutableList()
        currentList[index] = currentList[index].copy(text = selected, expanded = false, isActive = true)
        _inputField.value = currentList
    }

    fun isSubmitButtonEnabled(): Boolean {
        return submit.value;
    }

}