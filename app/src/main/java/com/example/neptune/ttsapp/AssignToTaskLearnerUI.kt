import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate
import java.util.Calendar


data class SuggestionInputFieldState(
    val text: String = "",
    val suggestions: List<String> = emptyList(),
    val expanded: Boolean = false
)

data class TimeInputFieldState(
    val hours: Int = 0,
    val minutes: Int = 0
)

data class DateInputFieldState(
    val date: LocalDate = LocalDate.now(),
    var datePicker: Boolean = false
)




class TaskToLearnerViewModel : ViewModel() {

    private val _inputField  = MutableStateFlow(List(6){SuggestionInputFieldState()})
    val inputField  =  _inputField
    private val _timeField = MutableStateFlow(List(2){TimeInputFieldState()})
    val timeField = _timeField
    private val _date = MutableStateFlow(DateInputFieldState())
    val date = _date

    var learnerSuggestions = listOf(
    "Alice Johnson"
    )
    var taskSuggestions = listOf(
    "Complete Module 1"
    )
   var  makingSuggestions = listOf(
    "Poster Presentation"
    )
    var processSuggestions = listOf(
    "Research"
    )

    fun updateHours(hours: Int,index: Int){
        val newTimeField = _timeField.value.toMutableList()
        newTimeField[index] = newTimeField[index].copy(hours = hours)
        _timeField.value = newTimeField
    }
    fun updateMinute(minutes: Int, index: Int ){
        val newTimeField = _timeField.value.toMutableList()
        newTimeField[index] = newTimeField[index].copy(minutes = minutes)
        _timeField.value = newTimeField
    }
    fun updateDate(date: DateInputFieldState){
        _date.value = date
    }
    
    
    fun updateKey(index: Int){
        val inputFieldState = _inputField.value.toMutableList();
        inputFieldState[index] = inputFieldState[index].copy(expanded = !inputFieldState[index].expanded)
        _inputField.value = inputFieldState
    }
    
    fun updateHeadContent(index: Int){
            val inputFieldState = _inputField.value.toMutableList();
            inputFieldState[index] = inputFieldState[index].copy(expanded = !inputFieldState[index].expanded)
            _inputField.value = inputFieldState
    }
    fun  onInputChange(index: Int, input: String){
        val currentList = _inputField.value.toMutableList();
        val suggestions = when(index){
            0 -> learnerSuggestions
            1 -> taskSuggestions
            5 -> makingSuggestions
            else -> processSuggestions
        }
        val filteredSuggestions = if(input.length>=2) suggestions.filter { it.contains(it, ignoreCase = true) } else emptyList()
        currentList[index] = currentList[index].copy(text = input, suggestions = filteredSuggestions, expanded = filteredSuggestions.isNotEmpty())
        _inputField.value = currentList
    }

}



@Composable
fun TaskToLearnerComposable(taskToLearnerViewModel: TaskToLearnerViewModel){
    val inputStates by taskToLearnerViewModel.inputField.collectAsState()
    val timeStates by taskToLearnerViewModel.timeField.collectAsState()
    val inputDate by taskToLearnerViewModel.date.collectAsState()
    val context = LocalContext.current
   Column(modifier = Modifier
       .fillMaxWidth()
       .padding(1.dp)) {
       Card {
           Row(modifier = Modifier
               .fillMaxWidth()
               .padding(1.dp)) {
               SuggestionInputField(
                   0.65f,
                   onInputChange = { taskToLearnerViewModel.onInputChange(0,it)},
                   value = inputStates[0],
                   updateKey = {taskToLearnerViewModel.updateKey(0)},
                   updateHeadContent = {taskToLearnerViewModel.updateHeadContent(0)},
               )
               DateInputField(
                   context = context,
                   state = inputDate,
                   onDateChange = {taskToLearnerViewModel.updateDate(inputDate)}
               )
           }
           SuggestionInputField(
               1f,
               onInputChange = {taskToLearnerViewModel.onInputChange(1,it)},
               value = inputStates[1],                        
               updateKey = {taskToLearnerViewModel.updateKey(0)},
               updateHeadContent = {taskToLearnerViewModel.updateKey(0)}
           )
       }
       Text(text = "")
       Card(modifier = Modifier
           .fillMaxWidth()
           .padding(1.dp)) {
           ProcessInputField(
               size = 0.9                                                                                                                                                                                                                    f,
               label = "1",
               onInputChange = { taskToLearnerViewModel.onInputChange(2, it) },
               value = inputStates[2],
               onUpdateKey = { taskToLearnerViewModel.updateKey(2) },
               onUpdateHeadContent = { taskToLearnerViewModel.updateHeadContent(2) },
           )
           ProcessInputField(
               size = 0.8f,
               label = "2",
               onInputChange = { taskToLearnerViewModel.onInputChange(3, it) },
               value = inputStates[3],
               onUpdateKey = { taskToLearnerViewModel.updateKey(3) },
               onUpdateHeadContent = { taskToLearnerViewModel.updateHeadContent(3) },
           )
           ProcessInputField(
               size = 0.8f,
               label = "3",
               onInputChange = { taskToLearnerViewModel.onInputChange(4, it) },
               value = inputStates[4],
               onUpdateKey = { taskToLearnerViewModel.updateKey(4) },
               onUpdateHeadContent = { taskToLearnerViewModel.updateHeadContent(4) },
           )
       }
       SuggestionInputField(
           1f,
           onInputChange = {taskToLearnerViewModel.onInputChange(5,it)},
           value = inputStates[5],
           updateKey = {taskToLearnerViewModel.updateKey(5)},
           updateHeadContent = {taskToLearnerViewModel.updateKey(5)}
       )
       Card(modifier = Modifier
           .fillMaxWidth()
           .padding(10.dp)) {
           Row {
               TimeInputField(
                   state = timeStates[0],
                   onHoursChange = {taskToLearnerViewModel.updateHours(it,0)},
                   onMintuesChange = {taskToLearnerViewModel.updateMinute(it,0)},
               )
               TimeInputField(
                   state = timeStates[1],
                   onHoursChange = {taskToLearnerViewModel.updateHours(it,1)},
                   onMintuesChange = {taskToLearnerViewModel.updateMinute(it,1)},
               )
           }
       }
       Button(
           onClick = {}
       ) {

       }
   }
}








@Composable
fun SuggestionInputField(size: Float,
                         onInputChange: (String) -> Unit,
                         value: SuggestionInputFieldState,
                         updateKey: (String) -> Unit,
                         updateHeadContent: (String) -> Unit
){
    OutlinedTextField(
        value = value.text,
        onValueChange = {onInputChange},
        modifier = Modifier.fillMaxWidth(size),
    )
    LazyColumn{
        if (value.expanded){
            items(
                count = value.suggestions.size ,
                key = { index -> value.suggestions[index] }
            ) {
                ListItem(
                    headlineContent = {Text(value.suggestions[it])},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp)
                )
            }
        }
   }
}



@Composable
fun TimeInputField(
    state: TimeInputFieldState,
    onHoursChange: (Int) -> Unit,
    onMintuesChange: (Int) -> Unit
){
    Text(text = "")
    Card(modifier = Modifier.width(50.dp)) {
        Column() {
            OutlinedTextField(
                value = state.hours.toString(),
                onValueChange = {onHoursChange},
                modifier = Modifier.width(50.dp),
            )

            OutlinedTextField(
                value = state.minutes.toString(),
                onValueChange = {onMintuesChange},
                modifier = Modifier.width(50.dp),
            )
        }
    }
}


@Composable
fun ProcessInputField(size: Float,
                      label: String,
                      onInputChange: (String) -> Unit,
                      value: SuggestionInputFieldState,
                      onUpdateKey: (String) -> Unit,
                      onUpdateHeadContent: (String) -> Unit){
    Row() {
        Text(text = label, modifier = Modifier.fillMaxWidth(0.2f))
        SuggestionInputField(
            size,
            onInputChange = onInputChange,
            value = value,
            updateKey = onUpdateKey,
            updateHeadContent = onUpdateHeadContent,
        )
    }
}


@Composable
fun DateInputField(context: Context,
                   state: DateInputFieldState,
                   onDateChange: (LocalDate) -> Unit){

    if(
        state.datePicker
    ){
        state.datePicker = false
        val calendar = Calendar.getInstance()
        val dialog  = DatePickerDialog(
            context,
            {_, year, month, dayOfMonth ->
                onDateChange(LocalDate.of(year,month+1,dayOfMonth))
            },
            state.date.year,
            state.date.monthValue-1,
            state.date.dayOfMonth
        )
        dialog.show()

    }
    OutlinedTextField(
        value = state.date.toString(),
        onValueChange = {},
        readOnly = true,
        label = {Text("Select Date")},
        modifier = Modifier
            .clickable { state.datePicker = true }
            .focusProperties {
                canFocus = false
            }
    )
}
