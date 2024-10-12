package com.example.neptune.ttsapp.Network;

import androidx.lifecycle.LiveData;

import com.example.neptune.ttsapp.User;

import retrofit2.Call;
import retrofit2.http.POST;

public interface UserServiceInterface {

    @POST("app/user/register")
    Call<APIResponse<?>> registerUser(User user);
}
