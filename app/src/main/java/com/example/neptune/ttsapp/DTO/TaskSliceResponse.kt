package com.example.neptune.ttsapp.DTO

import com.example.neptune.ttsapp.displayTaskToLearners.TaskSummary
import com.google.gson.annotations.SerializedName

data class TaskSliceResponse(
    @SerializedName("content") val content: List<TaskSummary>,
    @SerializedName("last") val isLastPage: Boolean
)
