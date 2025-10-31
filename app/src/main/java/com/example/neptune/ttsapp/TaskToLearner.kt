package com.example.neptune.ttsapp

import TaskToLearnerComposable

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TaskToLearner : Fragment(){
    private val viewModel: TaskToLearnerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

//        return ComposeView(requireContext()).apply {
//            // Ensures cleanup when Fragment is destroyed
//            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
//            setContent {
////                AssignTaskToLearner(viewModel)
//
//                    TaskToLearnerComposable(viewModel)
//            }
//        }
        val themedContext = ContextThemeWrapper(requireContext(),R.style.AppTheme)

        return ComposeView(themedContext).apply {
            setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                // Your Compose UI now uses the XML theme from themedContext
                TaskToLearnerComposable(viewModel)
            }
        }
    }
}
