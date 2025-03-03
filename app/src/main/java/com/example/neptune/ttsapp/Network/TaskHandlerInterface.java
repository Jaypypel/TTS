package com.example.neptune.ttsapp.Network;

import android.telecom.CallScreeningService;

import com.example.neptune.ttsapp.DTO.TaskManagement;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TaskHandlerInterface {


    @GET("tasksm1/delegated/{TaskOwnerUsername}/list")
    Call<ResponseBody> getDelegatedTasks(@Path("TaskOwnerUsername") String username);

    @GET("tasksm1/{TaskOwnerUsername}/{status}/modified/list")
    Call<ResponseBody> getTasksByTaskOwnerUsernameAndStatus(
            @Path("TaskOwnerUsername") String username, @Path("status") String status);

    @GET("tasksm1/list/accepted/{TaskOwnerUsername}/{status}")
    Call<ResponseBody> getTasksByTaskReceiveUsernameAndStatus(
            @Path("TaskOwnerUsername") String username, @Path("status") String status);

    @POST("tasksm1/taskm")
    Call<ResponseBody> addAssignTaskHandler(@Body TaskManagement taskManagement);


    @GET("tasksm1/count/")
    Call<ResponseBody> getTaskCountBasedOnStatus(@Query("username") String username ,
                                                 @Query("status") String status);


    @GET("tasksm1/{username}/list")
    Call<ResponseBody> getTaskList(@Path("username") String username );

    @PUT("tasksm1/task/{taskID}/{status}/update/")
    Call<ResponseBody> updateTaskManagementStatus(@Path("taskID") Long taskId,
                                                  @Path("status") String status);


    @PUT("tasksm1/task/{taskId}/seentime/update/")
    Call<ResponseBody> updateSeenTimeTaskManagement(@Path("taskId") Long taskId);

    @GET("tasksm1/time/assigned/{assignedTaskId}")
    Call<ResponseBody> getActualTotalTime(@Path("assignedTaskId") Long taskId);

    @PUT("tasksm1/new-actual-time")
    Call<ResponseBody> updateActualTotalTime(@Query("assignedTaskId") Long assignedTaskId,
                                             @Query("newActualTotalTime") String newActualTotalTime);

    @PUT("tasksm1/task/description-status/update")
    Call<ResponseBody> updateModifiedTaskStatusAndDescription(@Query("description") String description,
                                                   @Query("taskId") Long taskId);

}
