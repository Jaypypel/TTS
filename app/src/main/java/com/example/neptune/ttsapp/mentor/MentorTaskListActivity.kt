package com.example.neptune.ttsapp.mentor

import MentorTaskListScreen
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.neptune.ttsapp.R
import com.example.neptune.ttsapp.SessionManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MentorTaskList : Fragment() {
    val username: String? by lazy {
        val sessionManager = SessionManager(activity?.applicationContext)
        sessionManager.username
    }
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val themeContext = ContextThemeWrapper(requireContext(), R.style.AppTheme)
        return ComposeView(themeContext).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val viewModel: MentorTaskListViewModel = viewModel()
                MentorTaskListScreen(
                    username = username.toString(),
                    viewModel =viewModel
                )
            }
        }

    }
}
