package com.example.neptune.ttsapp.DTO

import com.google.gson.annotations.SerializedName

/**
 * This class is the blueprint for the paginated response from the server.
 * It exactly matches the JSON structure: {"last": false, "content": [...]}
 */
data class MentorTaskResponse(
    @SerializedName("content")
    val content: List<MentorTaskItem>,

    @SerializedName("last")
    val isLastPage: Boolean
)

/**
 * This class is the blueprint for a single task item within the "content" list.
 */
data class MentorTaskItem(
    @SerializedName("id")
    val id: Int,

    @SerializedName("taskName")
    val taskName: String,

    // Note: SerializedName matches the exact key from the JSON, which is case-sensitive.
    @SerializedName("LearnerName")
    val learnerName: String,

    @SerializedName("assignedOn")
    val assignedOn: String
)
