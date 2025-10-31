
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.neptune.ttsapp.DTO.MentorTaskItem
import com.example.neptune.ttsapp.Util.Debounce
import com.example.neptune.ttsapp.displayTaskToLearners.TaskSummary
import com.example.neptune.ttsapp.displayTaskToLearners.TaskToLearnerDetailsActivity
import com.example.neptune.ttsapp.mentor.MentorTaskListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorTaskListScreen(
    username: String,
    viewModel: MentorTaskListViewModel = hiltViewModel()
) {
    val lazyPagingItems = viewModel.getMentorTasks(username).collectAsLazyPagingItems()

    // Manage refresh state manually
    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

     Column(modifier = Modifier.fillMaxWidth()) {
         // Sync isRefreshing with actual load state
         LaunchedEffect(lazyPagingItems.loadState.refresh) {
             if (lazyPagingItems.loadState.refresh is LoadState.Loading) {
                 isRefreshing = false
             }
         }
         Row(horizontalArrangement = Arrangement.SpaceAround,
             verticalAlignment = CenterVertically,
             modifier = Modifier.fillMaxWidth().padding(10.dp)) { Text("Sr \nNo.", modifier = Modifier.weight(1f))
             Text("TR Code.",Modifier.weight(1f))
             Text("Task \nDate",modifier = Modifier.weight(2.5f))
             Text("Task \nName",modifier = Modifier.weight(3.5f))
             Text("Learner \nName", modifier = Modifier.weight(1.5f))

         }
         PullToRefreshBox(
             isRefreshing = isRefreshing,
             onRefresh = {
                 isRefreshing = true
                 lazyPagingItems.refresh()
             },
             state = pullToRefreshState,
             modifier = Modifier.fillMaxHeight(0.9f)
         ) {

             LazyColumn(
                 modifier = Modifier.fillMaxSize(),
//            contentPadding = PaddingValues(16.dp),
                 verticalArrangement = Arrangement.spacedBy(0.2.dp)
             ) {
                 if (lazyPagingItems.loadState.refresh is LoadState.Loading && !isRefreshing) {
                     item {
                         LoadingIndicator(modifier = Modifier.fillParentMaxSize())
                     }
                 }

                 // Initial error state
                 if (lazyPagingItems.loadState.refresh is LoadState.Error && lazyPagingItems.itemCount == 0) {
                     item {
                         val error = (lazyPagingItems.loadState.refresh as LoadState.Error).error
                         ErrorMessage(
                             error = error.localizedMessage ?: "Unknown error",
                             modifier = Modifier.fillParentMaxSize()
                         )
                     }
                 }


                 // Display items
                 items(
                     count = lazyPagingItems.itemCount,
                     key = lazyPagingItems.itemKey { it.id }
                 ) { index ->
                     lazyPagingItems[index]?.let { task ->
                         MentorTaskRow(task,index+1)
                     }
                 }

                 // Pagination loading
                 if (lazyPagingItems.loadState.append is LoadState.Loading) {
                     item {
                         LoadingIndicator()
                     }
                 }

                 // Pagination error
                 if (lazyPagingItems.loadState.append is LoadState.Error) {
                     item {
                         val error = (lazyPagingItems.loadState.append as LoadState.Error).error
                         ErrorMessage(error = error.localizedMessage ?: "Unknown error")
                     }
                 }
             }
         }
     }
}

@Composable
fun MentorTaskRow(task: MentorTaskItem,index:Int) {
    val taskSummary = TaskSummary(task.id.toString(),task.assignedOn,task.taskName,task.learnerName,"",false,0)

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
        }
        ,horizontalArrangement = Arrangement.Absolute.SpaceBetween, verticalAlignment = CenterVertically) {
        Text(text = index.toString(),modifier = Modifier.weight(1f))
        Text(text = task.id.toString(),Modifier.weight(1f))

        Text(text = task.assignedOn,Modifier.weight(2f))
        Text(text = task.taskName,Modifier.weight(3f))
        Text(text = task.learnerName,Modifier.weight(3f))

}
}

@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorMessage(
    error: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            if (onRetry != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}
