//package com.example.neptune.ttsapp.Network
//
//import com.example.neptune.ttsapp.DTO.AssignTaskDto
//import com.example.neptune.ttsapp.DTO.MentorTaskResponse
//import com.example.neptune.ttsapp.TaskAssignmentRequest
//import com.example.neptune.ttsapp.TaskSubmissionRequest
//import retrofit2.Call
//import retrofit2.http.Body
//import retrofit2.http.GET
//import retrofit2.http.POST
//import retrofit2.http.PUT
//import retrofit2.http.Path
//import retrofit2.http.Query
//
///**
// * This interface has been converted to Kotlin to support suspend functions,
// * which are essential for correct asynchronous behavior and cancellation.
// */
//interface TaskHandlerInterface {
//
//    @GET("tasksm/learner-task")
//    fun getLearnerTasks(@Query("username") username: String, @Query("status") status: String): Call<ResponseBody>
//
//    @GET("tasksm/completion_details")
//    fun getCompletionDetails(@Query("id") id: Long, @Query("username") username: String): Call<ResponseBody>
//
//    @GET("tasksm/task-details/")
//    fun getLearnerTaskDetails(@Query("id") id: Long): Call<ResponseBody>
//
//    @GET("tasksm/delegated/{TaskOwnerUsername}/list")
//    fun getDelegatedTasks(@Path("TaskOwnerUsername") username: String): Call<ResponseBody>
//
//    @GET("tasksm/{TaskOwnerUsername}/{status}/modified/list")
//    fun getTasksByTaskOwnerUsernameAndStatus(
//        @Path("TaskOwnerUsername") username: String, @Path("status") status: String
//    ): Call<ResponseBody>
//
//    @GET("tasksm/list/accepted/{TaskOwnerUsername}/{status}")
//    fun getTasksByTaskReceiveUsernameAndStatus(
//        @Path("TaskOwnerUsername") username: String, @Path("status") status: String
//    ): Call<ResponseBody>
//
//    @POST("tasksm/taskm")
//    fun addAssignTaskHandler(@Body taskManagement: AssignTaskDto): Call<ResponseBody>
//
//    @POST("tasksm/taskm/assign")
//    fun assignTaskToLearner(@Body taskAssignmentRequest: TaskAssignmentRequest): Call<ResponseBody>
//
//    @GET("tasksm/count/")
//    fun getTaskCountBasedOnStatus(@Query("username") username: String, @Query("status") status: String): Call<ResponseBody>
//
//    @GET("tasksm/{username}/list")
//    fun getTaskList(@Path("username") username: String): Call<ResponseBody>
//
//    @PUT("tasksm/task/{taskID}/{status}/update/")
//    fun updateTaskManagementStatus(@Path("taskID") taskId: Long, @Path("status") status: String): Call<ResponseBody>
//
//    @PUT("tasksm/task/{taskId}/seentime/update/")
//    fun updateSeenTimeTaskManagement(@Path("taskId") taskId: Long): Call<ResponseBody>
//
//    @GET("tasksm/time/assigned/{assignedTaskId}")
//    fun getActualTotalTime(@Path("assignedTaskId") taskId: Long): Call<ResponseBody>
//
//    @PUT("tasksm/new-actual-time")
//    fun updateActualTotalTime(@Query("assignedTaskId") assignedTaskId: Long,
//                                @Query("newActualTotalTime") newActualTotalTime: String): Call<ResponseBody>
//
//    @PUT("tasksm/task/description-status/update")
//    fun updateModifiedTaskStatusAndDescription(@Query("description") description: String,
//                                               @Query("taskId") taskId: Long): Call<ResponseBody>
//
//    @POST("tasksm/submit")
//    fun submitTask(@Query("id") id: Long, @Body request: TaskSubmissionRequest): Call<ResponseBody>
//
//    // This is the critical change: the function is now a proper suspend function.
//    @GET("tasksm/tolearners/{username}/")
//    suspend fun getTasksToLearnersByMentor(
//        @Path("username") username: String,
//        @Query("page") page: Int,
//        @Query("size") size: Int
//    ): MentorTaskResponse
//}
