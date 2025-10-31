package com.example.neptune.ttsapp
import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QueryAgainstTask : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Get taskId passed from previous activity
        val taskId = intent?.getIntExtra(EXTRA_TASK_ID, -1) ?: -1

        super.onCreate(savedInstanceState)

        // Optional: Apply your app theme if needed (or skip if already set in Manifest)
        val themedContext = ContextThemeWrapper(this, R.style.AppTheme)

        setContent {
            // Provide the Hilt ViewModel
            val viewModel: RaiseQueryViewModel = viewModel()
            RaiseQuery(viewModel = viewModel, taskId = taskId)
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "task_id"
    }
}


//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.compose.material.Button
//import androidx.compose.ui.platform.ComposeView
//import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.viewModels

//
//@AndroidEntryPoint
//class QueryAgainstTask: Fragment() {
//
//    private var taskId: Int = 0
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        taskId = arguments?.getInt("TaskId")!!
//    }
//
//    private val viewModel: RaiseQueryViewModel by viewModels()
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val themContext = ContextThemeWrapper(requireContext(),R.style.AppTheme)
//        return ComposeView(themContext).apply {
//            setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
//            setContent {
//                RaiseQuery(viewModel,taskId=taskId)
//            }
//        }
//    }
//
//    companion object{
//        private const val ARG_TASK_ID = "task_id"
//
//        @JvmStatic
//        fun newInstance(taskId: ): QueryAgainstTask {
//            val fragment= QueryAgainstTask()
//            val bundle = Bundle()
//            bundle.putInt(ARG_TASK_ID,taskId)
//            fragment.arguments = bundle
//            return fragment
//        }
//    }
//}
