
import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.neptune.ttsapp.DropDown
import com.example.neptune.ttsapp.TaskToLearnerViewModel
import com.example.neptune.ttsapp.Util.Debounce
import com.example.neptune.ttsapp.Util.js
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.util.Calendar


data class SuggestionInputFieldState(
    val text: String = "",
    val suggestions: List<String> = emptyList(),
    val expanded: Boolean = false,
    val isActive: Boolean = false
)

data class TimeInputFieldState(
    val hours: Int = 0,
    val minutes: Int = 0,
    val hasActiveState: Boolean = false
)

data class DateInputFieldState(
    val date: LocalDate = LocalDate.now(),
    var datePicker: Boolean = false
)





@Composable
fun TaskToLearnerComposable(taskToLearnerViewModel: TaskToLearnerViewModel){
    val inputStates by taskToLearnerViewModel.inputField.collectAsState()
    val timeStates by taskToLearnerViewModel.timeField.collectAsState()
    val taskResponseCodeState by taskToLearnerViewModel.taskResponseCode.collectAsState();
    val inputDate by taskToLearnerViewModel.date.collectAsState()
    val learner by taskToLearnerViewModel.learnerSuggestions.collectAsState()
  //  val taskAssignee by taskToLearnerViewModel.taskAssignee.collectAsState()
    val userMessage by taskToLearnerViewModel.userMessage.collectAsState()
    val context = LocalContext.current
   Column(modifier = Modifier
       .fillMaxWidth()
       .padding(8.dp).verticalScroll(rememberScrollState(),reverseScrolling = true)) {
       Card(modifier = Modifier
           .padding(1.dp)
           .fillMaxWidth()) {
          Column(modifier = Modifier
              .fillMaxWidth()
              .padding(2.dp), // Internal padding inside the card
              verticalArrangement = Arrangement.spacedBy(0.1.dp)){
              Row(modifier = Modifier
                  .fillMaxWidth().padding(0.1.dp),
                  horizontalArrangement = Arrangement.spacedBy(0.1.dp)) {
                  SuggestionInputField(
                      onInputChange = { taskToLearnerViewModel.onInputChange(0,it)},
                      value = inputStates[0],
                      onSuggestionSelected = {taskToLearnerViewModel.onSuggestionSelected(0,it)},
                      modifier = Modifier.weight(0.65f),
                      label = "Username of Learner", isEnable = true
                  )
//                  DateInputField(
//                      context = context,
//                      state = inputDate,
//                      onShowDatePickerHandled = {
//                          if (!inputDate.datePicker) {
//                              taskToLearnerViewModel.showDatePicker()
//                          } else {
//                              taskToLearnerViewModel.hideDatePicker()
//                          }
//                      },
//                      onDateSelected = {
//                          taskToLearnerViewModel.updateDateValue(it)
//                      },
//                      modifier = Modifier
//                          .weight(0.35f)
//                          .height(56.dp)
//                  )

                  Text(text = taskResponseCodeState)

              }
          }
           SuggestionInputField(
               onInputChange = {taskToLearnerViewModel.onInputChange(1,it)},
               value = inputStates[1],
               onSuggestionSelected = {taskToLearnerViewModel.onSuggestionSelected(1,it)},
               modifier = Modifier.padding(0.1.dp).fillMaxWidth(),
               label = "Task Name",
               isEnable = true
           )
       }
       Text(text = "Process")
       Card(modifier = Modifier
           .fillMaxWidth()
           .padding(1.dp)) {
           repeat(3) { index ->
               ProcessInputField(label = (index + 1).toString(),
                   onInputChange = { taskToLearnerViewModel.onInputChange(index + 2, it) },
                   value = inputStates[index+2],
                   onSuggestionSelected = {taskToLearnerViewModel.onSuggestionSelected(index+2,it)},
                   label1 = "")
           }
       }
       SuggestionInputField(
           onInputChange = {taskToLearnerViewModel.onInputChange(5,it)},
           value = inputStates[5],
           onSuggestionSelected = {taskToLearnerViewModel.onSuggestionSelected(5,it)},
           label = "Enter task making",isEnable = true
       )

       Text("making type",fontWeight = FontWeight.Bold)
       if(!inputStates[6].isActive){
           taskToLearnerViewModel.activateDropDown(index = 6)
       }
       DropDown(inputStates[6],
           { taskToLearnerViewModel.onSuggestionSelected(6,it) },
           { taskToLearnerViewModel.showSuggestions(index = 6) },
           defaultState = "Select"
       )

//       SuggestionInputField(
//           onInputChange = {taskToLearnerViewModel.onInputChange(6,it)},
//           value = inputStates[6],
//           onSuggestionSelected = {taskToLearnerViewModel.onSuggestionSelected(6,it)},
//           label = "Enter task making type"
//       )

       Card(modifier = Modifier
           .fillMaxWidth()
           .padding(10.dp)) {
           Row(modifier = Modifier
               .fillMaxWidth()
               .padding(8.dp),
               horizontalArrangement = Arrangement.spacedBy(8.dp)) {
               TimeInputField(
                   state = timeStates[0],
                   onHoursChange = {taskToLearnerViewModel.updateHours(it,0)},
                   onMinutesChange = {taskToLearnerViewModel.updateMinute(it,0)},
                   modifier = Modifier.weight(0.5f),
                   label = "Expected Time"
               )
//               TimeInputField(
//                   state = timeStates[1],
//                   onHoursChange = {taskToLearnerViewModel.updateHours(it,1)},
//                   onMinutesChange = {taskToLearnerViewModel.updateMinute(it,1)},
//                   modifier = Modifier.weight(0.5f),
//                   label = "Actual Time"
//               )
           }
       }
       Button(
           onClick = { Debounce.debounceEffect {
               taskToLearnerViewModel.submitTaskAssignment()
            }
           },
           modifier = Modifier.fillMaxWidth(),
           enabled = taskToLearnerViewModel.isSubmitButtonEnabled()
       ) {
           Text("Submit")
       }

       UserMessageResponse(
           message = userMessage.message,
           type = userMessage.type,
           onDismiss = {
               taskToLearnerViewModel.clearMessage()
           }
       )
   }
}







@Composable
fun SuggestionInputField(
                         onInputChange: (String) -> Unit,
                         value: SuggestionInputFieldState,
                         onSuggestionSelected: (String) -> Unit,
                         modifier: Modifier = Modifier,
                         label: String, isEnable: Boolean
){

    var anchorBounds by remember { mutableStateOf<Rect?>(null) }
//    var popupOffset by remember { mutableStateOf(IntOffset.Zero) }
    Box(modifier = modifier.onGloballyPositioned { coordinates ->
//        val position = coordinates.positionInWindow()
//        val size = coordinates.size
//        popupOffset = IntOffset(position.x.toInt(), ((position.y + size.height)).toInt())
    }) {
        OutlinedTextField(
            label = { Text(label,fontWeight = FontWeight.Bold) },
            value = value.text,
            onValueChange = { onInputChange(it) },
            modifier = modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    val position = coordinates.positionInWindow()
                    anchorBounds = Rect(
                        position.x,
                        position.y,
                        position.x + coordinates.size.width,
                        position.y + coordinates.size.height
                    )
                },
            isError =   !js.text_input_regex.matches(value.text) && value.isActive || value.text.isEmpty() &&  value.isActive,
            supportingText = {
                when{
                    value.text.isEmpty() && value.isActive ->
                        Text("This field can't be empty")
                    !js.text_input_regex.matches(value.text) && value.isActive ->
                        Text("Only letters, numbers, spaces, and . , | ? ' \" ( ) - are allowed")
                    else -> {
                        Text("")
                    }
                }
            },
            maxLines = 1.coerceAtLeast(value.text.length),
            enabled = isEnable,
            readOnly = !isEnable,

        )

        if (value.expanded && value.suggestions.isNotEmpty() && anchorBounds != null) {
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(
                    x = 0,
                    y = 70
                ),

                properties = PopupProperties(focusable = false)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .width(with(LocalDensity.current) { anchorBounds!!.width.toDp() })
                        .heightIn(max = 400.dp)
                        .padding(10.dp)
                ) {
                    items(
                        count = value.suggestions.size,
                        key = { index -> value.suggestions[index] }
                    ) {
                        ListItem(
                            headlineContent = { Text(value.suggestions[it]) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(5.dp)
                                .clickable {
                                    onSuggestionSelected(value.suggestions[it])
                                }
                        )


                    }
                }
            }
        }

    }
}



@Composable
fun TimeInputField(
    state: TimeInputFieldState,
    onHoursChange: (Int) -> Unit,
    onMinutesChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    label: String
){
        Column(modifier = modifier.padding(4.dp)) {
            Text(label,fontWeight = FontWeight.Bold)
//            OutlinedTextField(
//                label = { Text("Hours") },
//                value = state.hours.toString(),
//                onValueChange = {onHoursChange(it.toIntOrNull() ?: 0)},
//                modifier = Modifier.fillMaxWidth(),
//                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
//                readOnly = true,
//                isError = state.hours == 0  && state.minutes == 0 && state.hasActiveState,
//                supportingText = {if (state.hours==0 && state.minutes == 0 && state.hasActiveState) Text("This field can't be zero")}
//            )

            OutlinedTextField(
                label = { Text("Minutes") },
                value = state.minutes.toString(),
                onValueChange = {onMinutesChange(it.toIntOrNull() ?: 0)},
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                isError = state.minutes == 0  && state.hasActiveState,
                supportingText = {if (state.minutes==0  && state.hasActiveState) Text("This field can't be zero")}

            )
        }
}


@Composable
fun ProcessInputField(
    label: String,
                      onInputChange: (String) -> Unit,
                      value: SuggestionInputFieldState,
                      onSuggestionSelected: (String) -> Unit,
                      label1: String){
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)){
        Text(text = label, modifier = Modifier
            .fillMaxWidth(0.1f)
            .padding(20.dp)
            .align(Alignment.CenterVertically) )
        SuggestionInputField(
            onInputChange = onInputChange,
            value = value,
            onSuggestionSelected = onSuggestionSelected,
            label =label1, isEnable = true
        )
    }
}

@Composable
fun DateInputField(
    context: Context,
    state: DateInputFieldState,
    onShowDatePickerHandled: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val calendar = Calendar.getInstance()

    if (state.datePicker) {
        LaunchedEffect(Unit) {
            onShowDatePickerHandled() // hide picker flag from ViewModel
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
                },
                state.date.year,
                state.date.monthValue - 1,
                state.date.dayOfMonth
            ).show()
        }
    }

    OutlinedTextField(
        value = state.date.toString(),
        onValueChange = {},
        readOnly = true,
        label = { Text("Select Date") },
        modifier = modifier
            .clickable { onShowDatePickerHandled() } // use ViewModel to show dialog
            .focusProperties { canFocus = false }
    )
}



@Composable
fun UserMessageResponse(message: String,
                       type: MessageType,
                        onDismiss: () -> Unit
){

    AnimatedVisibility(
        visible = message.isNotEmpty(),
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        val backgroundColor = when (type) {
            MessageType.SUCCESS -> Color(0xFFDFF0D8) // light green
            MessageType.ERROR -> Color(0xFFF2DEDE)   // light red
            MessageType.INFO -> Color(0xFFD9EDF7)    // light blue
        }

        val textColor = when (type) {
            MessageType.SUCCESS -> Color(0xFF3C763D)
            MessageType.ERROR -> Color(0xFFA94442)
            MessageType.INFO -> Color(0xFF31708F)
        }
        LaunchedEffect(message) {
            val duration = when {
                message.length > 100 -> 5000L
                else -> 3000L
            }

            delay(duration)
            onDismiss()
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(12.dp)
                .clickable { onDismiss() }
        ){
            Text(text = message,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                maxLines = if(message.length > 50 ) 10 else 1
            )
        }
    }
}

enum class MessageType {
  SUCCESS,ERROR,INFO
}
