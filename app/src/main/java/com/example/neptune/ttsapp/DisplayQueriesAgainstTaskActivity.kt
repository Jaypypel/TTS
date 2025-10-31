package com.example.neptune.ttsapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DisplayQueriesAgainstTaskActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val queries = intent?.getParcelableArrayListExtra<RaiseAQueryRequest>(QUERIES)
        val taskId = intent?.getIntExtra("task_id", -1)
        setContent {
            // Provide the Hilt ViewModel
            val viewModel: DisplayQueriesViewModel = viewModel()
            ShowQueriesAgainstTask(viewModel = viewModel)
        }
    }

    companion object {
        const val QUERIES = "queries"
    }
}