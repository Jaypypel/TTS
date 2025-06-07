package com.example.neptune.ttsapp
//
//import AssignTaskToLearner
//import AssignTaskToLearnerViewModel
import TaskToLearnerComposable
import TaskToLearnerViewModel
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels

class TaskToLearner : Fragment(){
    private val viewModel: TaskToLearnerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // Ensures cleanup when Fragment is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
//                AssignTaskToLearner(viewModel)
                TaskToLearnerComposable(viewModel)
            }
        }
    }
}
