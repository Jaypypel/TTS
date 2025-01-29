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


    @GET("tasksm/delegated/{TaskOwnerUsername}/list")
    Call<ResponseBody> getDelegatedTasks(@Path("TaskOwnerUsername") String username);

    @GET("tasksm/{TaskOwnerUsername}/{status}/modified/list")
    Call<ResponseBody> getTasksByTaskOwnerUsernameAndStatus(
            @Path("TaskOwnerUsername") String username, @Path("status") String status);

    @GET("tasksm/list/accepted/{TaskOwnerUsername}/{status}")
    Call<ResponseBody> getTasksByTaskReceiveUsernameAndStatus(
            @Path("TaskOwnerUsername") String username, @Path("status") String status);

    @POST("tasksm/taskm")
    Call<ResponseBody> addAssignTaskHandler(@Body TaskManagement taskManagement);

    @GET("tasksm/pending/count/{username}")
    Call<ResponseBody> getPendingTaskCount(@Path("username") String username );

    @GET("tasksm/approved/count/{username}")
    Call<ResponseBody> getApprovedTaskCount(@Path("username") String username );

    @GET("tasksm/completed/count/{username}")
    Call<ResponseBody> getCompletedTaskCount(@Path("username") String username );

    @GET("tasksm/accepted/count/{username}")
    Call<ResponseBody> getAcceptedTaskCount(@Path("username") String username );

    @GET("tasksm/{username}/list")
    Call<ResponseBody> getTaskList(@Path("username") String username );

    @PUT("tasksm/task/{taskID}/{status}/update/")
    Call<ResponseBody> updateTaskManagementStatus(@Path("taskID") Long taskId,
                                                  @Path("status") String status);


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

}
