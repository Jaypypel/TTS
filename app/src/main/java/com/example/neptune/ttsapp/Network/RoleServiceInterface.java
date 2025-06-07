package com.example.neptune.ttsapp.Network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface RoleServiceInterface {

    @POST("role/new")
    Call<ResponseBody> addRole(@Query("name") String name);

    @GET("role/roles")
    Call<ResponseBody> getRoles();

    @PUT("role/")
    Call<ResponseBody> assignRole(@Query("username") String username, @Query("role") String role);
}
