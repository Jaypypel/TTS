package com.example.neptune.ttsapp

import MessageType
import SuggestionInputField
import SuggestionInputFieldState
import UserMessageResponse
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.sp
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


data class QueryResolutionRequest(
    val queryId: Int,
    var resolution: String,
    var isQueryResolved: Boolean = false,
    var queryPopUp: Boolean = false
)


@Parcelize
data class Query(
    val queryId: Int,
    val point1: String,
    val resolution: String,
    val createdOn: String,
    val isEditable: Boolean = false
) : Parcelable

@HiltViewModel class DisplayQueriesViewModel @Inject constructor (
    private val queryRepository: QueryRepository,
    savedStateHandle: SavedStateHandle,
    application: Application,
) : AndroidViewModel(application) {

    // Allows letters, numbers, spaces, and basic punctuation
    // Length: 1–200 characters
    var text_input_regex= Regex(pattern = """^[\p{L}\p{N}\s.,|?'"()\-]{1,200}$""",
        options =setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE)
    )

    private val _buttonState = MutableStateFlow(false);
    val buttonState = _buttonState

    private val _queries = MutableStateFlow(List(3){ Query(0,"N/A","N/A","", false)})
    val queries = _queries

    private val _userMessage = MutableStateFlow(UserMessageResponse("",
        MessageType.INFO))
    val userMessage = _userMessage

    private val _queryResolution = MutableStateFlow(List(queries.value.size){ QueryResolutionRequest(0,"",false,false)})
    val queryResolution = _queryResolution

     val taskId: Int = savedStateHandle.get<Int>("task_id") ?: -1
    private var taskStatus: String = savedStateHandle.get<String>("task_status") ?: ""



    private val _queryInput = MutableStateFlow(List(3){SuggestionInputFieldState()} )
    val queryInput = _queryInput




    val querySuggestions = listOf(
        "Research",
        "Requirement Gathering",
        "Refinement Session",
        "Review Meeting",
        "Report Writing"
    )


    fun onInputChange(index: Int,input: String){
        val suggestions = querySuggestions
        val filteredSuggestions = if(input.length>=2)
            suggestions
                .filter {
                    it.contains(input, ignoreCase = true) }
        else
            emptyList()
        val currentValue = queryInput.value.toMutableList()
//        queryInput.value =  queryInput.value.map { s ->SuggestionInputFieldState(text = input, suggestions = filteredSuggestions, expanded = filteredSuggestions.isNotEmpty())}
        currentValue[index] = currentValue[index].copy(text = input, suggestions = filteredSuggestions, expanded = filteredSuggestions.isNotEmpty(), isActive = true)
        queryInput.value = currentValue
     }

    fun onSuggestionSelected(suggestion: String){
        queryInput.value = queryInput.value
            .map { s ->  SuggestionInputFieldState(text = suggestion, suggestions = emptyList(), expanded = false)}

    }

    fun clearMessage(){
        _userMessage.value = UserMessageResponse("",MessageType.INFO)
    }

    fun submitQuery() {
        val inputs = queryInput.value.first { value -> value.text.isNotEmpty()}.text
        val request = RaiseAQueryRequest(
            taskId = taskId,
            point1 = inputs)

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

    /* v*/
    fun isAcceptedOrNotMentor(): Boolean {
        Log.e("Mentor", "Mentor: $taskStatus")
//       return roles?.contains("ROLE_MENTOR")==false || taskStatus=="Pending"
//        return (!roles!!.contains("ROLE_MENTOR")) || "Pending" == taskStatus;
        val isNotMentor = roles?.any { it.trim().equals("ROLE_MENTOR", ignoreCase = true) } != true
//        val isPending = taskStatus.trim().equals("Pending", ignoreCase = true)

        if (isNotMentor) {
            Log.d("DEBUG", "Condition passed: not a mentor or task is pending")
            // do action
            return true
        } else {
            Log.d("DEBUG", "Condition failed: is mentor and task is not pending")
            return false
        }

    }
    val roles: Set<String>? by lazy {
        val sessionManager = SessionManager(application.applicationContext)
        sessionManager.roles
    }

    fun updateQuery(value: String,index: Int, id: Int){
        val list = _queryResolution.value.toMutableList()
        list[index] = list[index].copy(resolution = value, queryId = id)
        _queryResolution.value = list;
    }

    fun enableQueryPopUp(index: Int){
        val list = _queryResolution.value.toMutableList();
        list[index] = list[index].copy(queryPopUp = true);
        _queryResolution.value = list;
    }

    fun closeQueryPopUp(index: Int){
        val list = _queryResolution.value.toMutableList();
        list[index] = list[index].copy(queryPopUp = false);
        _queryResolution.value = list;
    }

    fun validationInputField(input: String): Boolean{
        return input.isEmpty() || input.length < 8
    }
    fun resolveQuery(index: Int){




        val id = taskId
        val query = queryInput.value.first { it.isActive}.text;

//        val id = _queryResolution.value[index].queryId;
//        val resolution = _queryResolution.value[index].resolution;
//        val queryResolution = QueryResolutionRequest(id,resolution,
//            isQueryResolved = true,
//            queryPopUp = false
//        )
        val request = RaiseAQueryRequest(id,query)
        if(validationInputField(query)){
            userMessage.value = UserMessageResponse("Please enter a valid input", MessageType.ERROR)
        }else {
        Log.d("Submit", "Raise query: $queryResolution")


        viewModelScope.launch {
            try {
            val status = isQuerySubmitted(request);
            if(status == "success") {
                userMessage.value = UserMessageResponse("Task assigned successfully", MessageType.SUCCESS)
            }else{
                userMessage.value = UserMessageResponse("Task assigned failed", MessageType.ERROR)
            }
        }catch (e: Exception){
            userMessage.value = UserMessageResponse("Error occurred" + e.message, MessageType.ERROR)
        }
        }
        }
        _queryResolution.value[index].resolution = "";
    }


    fun allowEdits(){
//        queries.value = queries.value.toMutableList().map { q -> Query(q.queryId,q.point1,"",q.createdOn,true) }
        buttonState.value = true
    }

    fun isEnable(index: Int, query: Query) : Boolean {
        return isAcceptedOrNotMentor() && query.point1 == "NA"
    }

    private suspend fun isQuerySubmitted(request:   RaiseAQueryRequest): String =
        suspendCoroutine { continuation ->
           queryRepository.raiseQuery(request) .whenComplete { result, exception ->
                if (exception != null) continuation.resumeWith(Result.failure(exception))
                else continuation.resumeWith(Result.success(result))
            }
        }
    init {
        loadQueries()
    }



    private  fun loadQueries() {
        viewModelScope.launch {
            try {
                val queriesFetched = getQueriesAsync()
                val updatedQueries = queriesFetched.toMutableList()
                while(updatedQueries.size < 3){
                    updatedQueries.add(Query(0,"NA","NA","", true))
                }
                queries.value = updatedQueries.take(3)
                queryResolution.value = List(updatedQueries.size) { QueryResolutionRequest(updatedQueries[it].queryId, "",
                        isQueryResolved = false,
                        queryPopUp = false
                    )
                }
            } catch (e: Exception) {
                userMessage.value = UserMessageResponse("Error occurred while fetching usernames" + e.message, MessageType.ERROR)
            }
        }
    }

    private suspend fun getQueriesAsync(): List<Query> =
        suspendCoroutine { continuation ->
            queryRepository.getQueries(taskId.toLong()) .whenComplete { result, exception ->
                if (exception != null) continuation.resumeWith(Result.failure(exception))
                else continuation.resumeWith(Result.success(result))
            }
        }

    fun moveToRaiseQueryScreen(context: Context, intent: Intent){
        intent.putExtra("task_id",taskId);
        context.startActivity(intent);
    }

}




@Composable
fun ShowQueriesAgainstTask(viewModel: DisplayQueriesViewModel){
    val context: Context = LocalContext.current
    val intent = Intent(context, QueryAgainstTask::class.java)
    val queries by viewModel.queries.collectAsState()
    val queryResolution by viewModel.queryResolution.collectAsState()
    val queryInput by viewModel.queryInput.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val buttonState by viewModel.buttonState.collectAsState()
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth()) {
            Text(viewModel.taskId.toString(), fontWeight = FontWeight.Bold)
            Text(text = "Task Date", fontWeight = FontWeight.Bold)
            Text(text = "Mentor", fontWeight = FontWeight.Bold)
        }
        Text("Task name", modifier = Modifier.fillMaxWidth())
        Queries(queries,queryResolution, viewModel = viewModel,queryInput,buttonState)

//        if(viewModel.isAcceptedOrNotMentor()){
//            SuggestionInputField(
//                onInputChange = {viewModel.onInputChange(it)},
//                value = queryInput,
//                onSuggestionSelected ={viewModel.onSuggestionSelected(it)},
//                modifier = Modifier.fillMaxWidth(),
//                label = "Enter task query"
//            )
//        Button(
//            onClick = {
//                Debounce.debounceEffect {
//                    if(queryInput.text.isEmpty()){
//                    viewModel.userMessage.value = UserMessageResponse(
//                        "Please enter a valid input",
//                        MessageType.ERROR
//                        )
//                    }else{
//                    viewModel.submitQuery() }
//                }
//            },
//            modifier = Modifier
//                .fillMaxWidth()
//        ) {
//            Text("Submit")
//        }}

        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick ={
                if(viewModel.isAcceptedOrNotMentor())
                    viewModel.submitQuery() else viewModel.resolveQuery(0)
            }) {
                Text("Done")
            }
            Button(onClick = {
                viewModel.allowEdits()
            }) {
                Text("Edit")
            }
        }

        UserMessageResponse(
            message = userMessage.message,
            type = userMessage.type,
            onDismiss = {viewModel.clearMessage()}
        )
    }
}


@SuppressLint("SuspiciousIndentation")
@Composable
fun Queries(queries: List<Query>,
            queryResolutions: List<QueryResolutionRequest>,
            viewModel: DisplayQueriesViewModel,
            input: List<SuggestionInputFieldState>, buttonState: Boolean) {

//    if(queries.isEmpty())
//        queries.toMutableList().add( Query(0,"NA","NA", "NA",true))
//        queryResolutions.toMutableList().add(QueryResolutionRequest(0,"NA",
//            isQueryResolved = false,
//            queryPopUp = false
//        ))
//    if(queries.isEmpty()){
//        Text(text = "No Queries")
//    }else
   val count = if(!viewModel.isAcceptedOrNotMentor()) queries.count { it.point1 != "NA" } - 1 else queries.count{it.point1 != "NA"}
//   val resolutionCount = count - 1;
    val final = when(count) {
         0 -> 1
        1 -> 2
        2 -> 3
        else -> 0
    }
//    val itemCount = 1


    LazyColumn {
        items(queries.size){ index ->
            DataCard(
                queries[index],
                queryResolutions[index],
                viewModel,
                index = index,
                input = input[index], buttonState = buttonState &&  if(!viewModel.isAcceptedOrNotMentor()) index<=count else count == index
            )
        }
    }


//    LazyColumn {
//        items(3 - count){ index ->
//            DataCard(
//                queries[index],
//                queryResolutions[index],
//                viewModel,
//                index = index,
//                input = input[index], buttonState = buttonState && count == index
//            )
//        }
//    }
//    LazyColumn(modifier=Modifier.fillMaxWidth() ) {
//        items(final) { index ->
//            DataCard(
//                queries[index],
//                 queryResolutions[index],
//                viewModel,
//                index = index,
//                input = input[index], buttonState = buttonState
//            )
//        }
//    }
//    LazyColumn {
//        items(queries.size - final) { index ->
//            DataCard(
//                queries[index],
//                queryResolutions[index],
//                viewModel,
//                index = index,
//                input = input[index], buttonState = false
//            )
//        }
//    }
}

@Composable
fun DataCard(query: Query,
             queryResolution: QueryResolutionRequest,
             viewModel: DisplayQueriesViewModel,
             input: SuggestionInputFieldState,
             index: Int,
             buttonState: Boolean
) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()){
                Text(
                    text = "TQ:  ${index+1}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

             val updatedState = if(query.point1 != "NA")
                 SuggestionInputFieldState(text = query.point1, suggestions = emptyList(), expanded = false, isActive = false)
             else input

                SuggestionInputField(
                    onInputChange = {viewModel.onInputChange(index,it)},
                    value =  updatedState,
                    onSuggestionSelected ={viewModel.onSuggestionSelected(it)},
                    modifier = Modifier.fillMaxWidth(),
                    label = if(query.point1 != "NA") " " else "Enter task query", isEnable = buttonState && viewModel.isAcceptedOrNotMentor()

                )
                val isEnabled = viewModel.isAcceptedOrNotMentor() && query.point1 == "N/A"
                Log.e("Mentor", "Mentor: $isEnabled")
                Log.e("Mentor", "Mentor: ${viewModel.isAcceptedOrNotMentor()}")
                Log.e("Mentor", "Mentor: ${query.point1 == "N/A"}")
                Text(text = "",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
//            Row(verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()){
//                Text(
//                    text = "Query",
//                    fontWeight = FontWeight.Bold,
//                    fontSize = 18.sp
//                )
//                Text(text =  query.point1)
//            }

            Spacer(modifier = Modifier.height(4.dp))


            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                val resolutionState = if(query.resolution!="NA" && !query.resolution.isNullOrBlank())
                    SuggestionInputFieldState(text = query.resolution, suggestions = emptyList(), expanded = false, isActive = false)
                else input


                Text(
                    text = "TQA: ${index+1}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                SuggestionInputField(
                    onInputChange = {viewModel.onInputChange(index,it)},
                    value =  resolutionState,
                    onSuggestionSelected ={viewModel.onSuggestionSelected(it)},
                    modifier = Modifier.fillMaxWidth(),
                    label = if(query.resolution !=  "NA" && !query.resolution.isNullOrBlank()) " " else "Enter resolution ", isEnable = buttonState && !viewModel.isAcceptedOrNotMentor()

                )
            }

//            if(queryResolution.queryPopUp ){
//                OutlinedTextField(
//                    label = { Text("Resolve Query") },
//                    value = queryResolution.resolution.trim(),
//                    onValueChange = {it -> viewModel.updateQuery(it,index,query.queryId) },
//                    modifier = Modifier.fillMaxWidth(),
//                    isError = queryResolution.resolution.isEmpty() ||
//                            viewModel.text_input_regex.matches(queryResolution.resolution)
//                            || queryResolution.resolution.length < 8,
//                    supportingText = {
//                        if (queryResolution.resolution.length < 8 || queryResolution.resolution.isEmpty() ) {
//                           Text("Please enter a valid input")
//                        }
//                    },
//
//                    )
//                Button(onClick = {
//                    Debounce.debounceEffect {
//                        viewModel.resolveQuery(index)
//                        viewModel.closeQueryPopUp(index)
//                    }
//                }) { Text("Submit") }
//            }
//            if(viewModel.roles?.contains("ROLE_MENTOR") == true && query.resolution.isNullOrBlank()){
//                Button(onClick = {
//                    viewModel.enableQueryPopUp(index)
//                }) {
//                    Text("Resolve Query")
//                }
//            }else {
//                Row(verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()){
//                    Text(
//                        text = "TQA: ${index + 1}",
//                        fontWeight = FontWeight.Bold,
//                        fontSize = 18.sp
//                    )
//                    Text(if(query.resolution.isNullOrBlank()) "Not answered yet" else query.resolution)
//                }
//
//
//            }
//            val context: Context = LocalContext.current
//            Row(verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceBetween,
//                modifier = Modifier.fillMaxWidth()){
//                Button(onClick = {
//                    Toast.makeText(context,"Not implemented", Toast.LENGTH_SHORT).show()
//                }) { Text("Ok") }
//
//                Button(onClick = {
//                    Toast.makeText(context,"Not implemented", Toast.LENGTH_SHORT).show()
//                }) {  Text("NOk")}
//            }

//        }
    }
}



//@Preview(showBackground = true)
//@Composable
//fun ResolutionInputFieldPreview() {
//    var text by remember { mutableStateOf("") }
//
//    val textInputRegex = Regex(
//        pattern = """^[\p{L}\p{N}\s.,|?'"()\-]{1,200}$""",
//        options = setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE)
//    )
//
//    val isError = text.isEmpty() ||
//            !textInputRegex.matches(text) ||
//            text.length < 8
//
//    OutlinedTextField(
//        value = text,
//        onValueChange = { text = it },
//        label = { Text("Resolution") },
//        isError = isError,
//        supportingText = {
//            if (isError) {
//                Text("Please enter a valid input (min 8 characters, allowed chars only)")
//            }
//        },
//        singleLine = true,
//        modifier = Modifier.fillMaxWidth()
//    )
//}
