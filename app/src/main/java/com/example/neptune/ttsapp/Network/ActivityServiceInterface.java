package com.example.neptune.ttsapp.Network;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ActivityServiceInterface {

    @GET("/activities/names")
    Call<ResponseBody> getActivitiesName();

    @GET("/activities/activityNames")
    Call<ResponseBody> getActivitiesNamebyUsername(@Query("username") String username);

    @GET("/activities/names/{username}")
    Call<ResponseBody> getActivities(@Path("username") String username);

    @POST("/activities/activity")
    Call<ResponseBody> addActivity(@Query("username") String username, @Query("actvtyNme") String activityName, @Query("createdOn") String createdOn);

    @POST("OtherActivities/activity")
    Call<ResponseBody> addOtherActivity(@Query("otherActiName") String name, @Query("createdOn") String createdOn);

    @GET("OtherActivities/list")
    Call<ResponseBody> getOtherActivityNames();
}
