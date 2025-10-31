package com.example.neptune.ttsapp.Network;

import com.example.neptune.ttsapp.DTO.AssignTaskDto;
import com.example.neptune.ttsapp.DTO.MentorTaskResponse;
import com.example.neptune.ttsapp.TaskAssignmentRequest;
import com.example.neptune.ttsapp.TaskSubmissionRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TaskHandlerInterface {

    @GET("tasksm/learner-task")
    Call<ResponseBody> getLearnerTasks(@Query("username") String username, @Query("status") String status);

    @GET("tasksm/completion_details")
    Call<ResponseBody> getCompletionDetails(@Query("id") Long id, @Query("username") String username);

    @GET("tasksm/task-details/")
    Call<ResponseBody> getLearnerTaskDetails(@Query("id") Long id);

    @GET("tasksm/delegated/{TaskOwnerUsername}/list")
    Call<ResponseBody> getDelegatedTasks(@Path("TaskOwnerUsername") String username);

    @GET("tasksm/{TaskOwnerUsername}/{status}/modified/list")
    Call<ResponseBody> getTasksByTaskOwnerUsernameAndStatus(
            @Path("TaskOwnerUsername") String username, @Path("status") String status);

    @GET("tasksm/list/accepted/{TaskOwnerUsername}/{status}")
    Call<ResponseBody> getTasksByTaskReceiveUsernameAndStatus(
            @Path("TaskOwnerUsername") String username, @Path("status") String status);

    @POST("tasksm/taskm")
    Call<ResponseBody> addAssignTaskHandler(@Body AssignTaskDto taskManagement);

    @POST("tasksm/taskm/assign")
    Call<ResponseBody> assignTaskToLearner(@Body TaskAssignmentRequest taskAssignmentRequest);

    @GET("tasksm/count/")
    Call<ResponseBody> getTaskCountBasedOnStatus(@Query("username") String username, @Query("status") String status);

    @GET("tasksm/{username}/list")
    Call<ResponseBody> getTaskList(@Path("username") String username);

    @PUT("tasksm/task/{taskID}/{status}/update/")
    Call<ResponseBody> updateTaskManagementStatus(@Path("taskID") Long taskId, @Path("status") String status);

    @PUT("tasksm/task/{taskId}/seentime/update/")
    Call<ResponseBody> updateSeenTimeTaskManagement(@Path("taskId") Long taskId);

    @GET("tasksm/time/assigned/{assignedTaskId}")
    Call<ResponseBody> getActualTotalTime(@Path("assignedTaskId") Long taskId);

    @PUT("tasksm/new-actual-time")
    Call<ResponseBody> updateActualTotalTime(@Query("assignedTaskId") Long assignedTaskId,
                                             @Query("newActualTotalTime") String newActualTotalTime);

    @PUT("tasksm/task/description-status/update")
    Call<ResponseBody> updateModifiedTaskStatusAndDescription(@Query("description") String description,
                                                              @Query("taskId") Long taskId);

    @POST("tasksm/submit")
    Call<ResponseBody> submitTask(@Query("id") Long id, @Body TaskSubmissionRequest request);

    // This method signature is now valid Java, returning a Call object.
    @GET("tasksm/tolearners/{username}/")
    Call<MentorTaskResponse> getTasksToLearnersByMentor(
            @Path("username") String username,
            @Query("page") int page,
            @Query("size") int size
    );
}
