package com.example.neptune.ttsapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TaskSubmissionActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: TaskSubmissionViewModel = viewModel()
            TaskSubmission(viewModel = viewModel)
        }

    }
}