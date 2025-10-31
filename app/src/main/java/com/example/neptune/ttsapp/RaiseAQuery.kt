package com.example.neptune.ttsapp

import MessageType
import ProcessInputField
import SuggestionInputFieldState
import UserMessageResponse
import android.app.Application
import android.os.Parcelable
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.neptune.ttsapp.repository.QueryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine


@Composable
fun RaiseQuery(viewModel: RaiseQueryViewModel,taskId: Int){
   val queryState by viewModel.queryInput.collectAsState();
    val userMessage by viewModel.userMessage.collectAsState();
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        contentAlignment = Alignment.Center // Centers content vertically and horizontally
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, // Align children horizontally centered
            verticalArrangement = Arrangement.spacedBy(16.dp), // Optional spacing between items
            modifier = Modifier
                .fillMaxWidth(0.9f).wrapContentHeight()
                .padding(16.dp )// Slight margin on the sides for responsiveness
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                QueryInput(viewModel, queryState)
            }

            Button(
                onClick = { viewModel.submitQuery() },
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text("Submit")
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


}


@Composable
fun QueryInput(viewModel: RaiseQueryViewModel, queryState: List<SuggestionInputFieldState>){
    Column(modifier = Modifier.padding(8.dp).fillMaxWidth()
            , verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(3){ index ->
            ProcessInputField(
                label = (index+1).toString(),
                onInputChange = {viewModel.onInputChange(index,it)},
                value = queryState[index],
                onSuggestionSelected = {viewModel.onSuggestionSelected(index,it)},
                label1 = ""
            )
        }
    }
}

@HiltViewModel
class RaiseQueryViewModel @Inject constructor(
    application: Application,
    savedStateHandle: SavedStateHandle,
    private val queryRepository: QueryRepository
) : AndroidViewModel(application) {
    private val _queryInput = MutableStateFlow(List(3) { SuggestionInputFieldState() })
    val queryInput = _queryInput

    val taskId: Int = savedStateHandle.get<Int>("task_id") ?: -1;

    private val _userMessage = MutableStateFlow(UserMessageResponse("",MessageType.INFO))
    val userMessage = _userMessage





    val querySuggestions = listOf(
        "Research",
        "Requirement Gathering",
        "Refinement Session",
        "Review Meeting",
        "Report Writing"
    )


    fun onInputChange(index: Int, input: String){
        val currentList = _queryInput.value.toMutableList()
        val suggestions = querySuggestions
        val filteredSuggestions = if(input.length>=2) suggestions.filter { it.contains(input, ignoreCase = true) } else emptyList()
         currentList[index] = currentList[index].copy(text = input, suggestions = filteredSuggestions, expanded = filteredSuggestions.isNotEmpty())
        _queryInput.value = currentList
    }

    fun onSuggestionSelected(index: Int, suggestion: String){
        val currentList = _queryInput.value.toMutableList()
        currentList[index] = currentList[index].copy(text = suggestion, suggestions = emptyList(), expanded = false)
        _queryInput.value = currentList
    }

    fun clearMessage(){
        _userMessage.value = UserMessageResponse("",MessageType.INFO)
    }

    fun submitQuery() {
        val inputs = _queryInput.value
        val request = RaiseAQueryRequest(
            taskId = taskId,
            point1 = inputs[0].text)

        Log.d("Submit", "Submitting: $request")
        viewModelScope.launch {
            try {
                val status: String = isQueryRaised(request)
                if(status.equals("success")){
                    userMessage.value = UserMessageResponse("Query raised successfully", MessageType.SUCCESS)
                }else{
                    userMessage.value = UserMessageResponse("Query raised failed", MessageType.ERROR)

                }
            }catch (e: Exception){
                userMessage.value = UserMessageResponse("Error occurred" + e.message, MessageType.ERROR)
            }
        }

    }
    private  suspend fun isQueryRaised(request: RaiseAQueryRequest): String =
        suspendCoroutine { continuation ->
            queryRepository.raiseQuery(request).whenComplete { result, exception ->
                if (exception != null) continuation.resumeWith(Result.failure(exception))
                else continuation.resumeWith(Result.success(result))
            }
        }

}

@Parcelize
data class RaiseAQueryRequest(
    val taskId: Int,
    val point1: String,
) : Parcelable


