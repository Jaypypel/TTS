package com.example.neptune.ttsapp.Network;

import androidx.lifecycle.LiveData;

import com.example.neptune.ttsapp.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface UserServiceInterface {

    @POST("/app/user/register")
    Call<APIResponse<Object>> registerUser(@Body User user);

    @GET("/app/user/login")
    Call<APIResponse<Object>> login(@Query("username") String username,@Query("password") String password);
}
