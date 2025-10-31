//package com.example.neptune.ttsapp.DTO
//
//import com.google.gson.annotations.SerializedName
//
///**
// * Represents the entire paginated response from the server.
// */
//data class PagedResponse<T>(
//    @SerializedName("content") val content: List<T>,
//    @SerializedName("last") val isLastPage: Boolean,
//    @SerializedName("pageable") val pageable: PageableInfo
//)
//
///**
// * Represents a single task item in the paginated list.
// */
//data class MentorTask(
//    @SerializedName("id") val id: Int,
//    @SerializedName("taskName") val taskName: String,
//    @SerializedName("LearnerName") val learnerName: String,
//    @SerializedName("assignedOn") val assignedOn: String
//)
//
///**
// * Represents the pageable information from the server response.
// */
//data class PageableInfo(
//    @SerializedName("pageNumber") val pageNumber: Int,
//    @SerializedName("pageSize") val pageSize: Int
//)
