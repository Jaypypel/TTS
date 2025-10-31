package com.example.neptune.ttsapp.displayTaskToLearners

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.neptune.ttsapp.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LearnerTasks: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val themedContext = ContextThemeWrapper(requireContext(),R.style.AppTheme)

        return ComposeView(themedContext).apply {
            setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                // Your Compose UI now uses the XML theme from themedContext
                val viewModel: LearnerTasksViewModel = viewModel()
                LearnerTasks(viewModel)
            }
        }
    }
}