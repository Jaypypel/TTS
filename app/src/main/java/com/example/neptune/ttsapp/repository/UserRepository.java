package com.example.neptune.ttsapp.repository;

import android.util.Log;

import com.example.neptune.ttsapp.AppExecutors;
import com.example.neptune.ttsapp.Network.APIErrorResponse;
import com.example.neptune.ttsapp.Network.APIResponse;
import com.example.neptune.ttsapp.Network.APISuccessResponse;
import com.example.neptune.ttsapp.Network.ResponseBody;
import com.example.neptune.ttsapp.Network.UserServiceInterface;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {

    private final UserServiceInterface userService;
    private final AppExecutors appExecutors;

    public UserRepository(UserServiceInterface userService, AppExecutors appExecutors) {
        this.userService = userService;
        this.appExecutors = appExecutors;
    }


    public CompletableFuture<ArrayList<String>> getUsernames() {
        CompletableFuture<ArrayList<String>> future = new CompletableFuture<>();

        appExecutors.getNetworkIO().execute(() -> {
            Call<ResponseBody> usernamesResponse = userService.getUsernames();
            usernamesResponse.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                    try {
                        APIResponse apiResponse =   APIResponse.create(response);
                        if(apiResponse instanceof APISuccessResponse){
                            JsonElement result = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody();
                            Gson gson = new Gson();
                            Type listType = new TypeToken<ArrayList<String>>() {}.getType();
                            if(result.isJsonArray()){
                                JsonArray usernames = result.getAsJsonArray();
                                ArrayList<String> list = gson.fromJson(usernames, listType);
                                future.complete(list);
                            }
                        }
                        if (apiResponse instanceof APIErrorResponse) {
                            String erMsg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                            future.completeExceptionally(new Exception(erMsg));

                        }
                        if (apiResponse instanceof APIErrorResponse) {
                            future.completeExceptionally(new Exception("empty response"));
                        }
                    }

                    catch (ClassCastException e){
                        future.completeExceptionally(new Exception("Unable to cast the response into required format due to "+ e.getMessage()));
                    }
                    catch (IOException e) {
                        Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                        future.completeExceptionally(new Exception("Exception occured while getting measurables due to" + e.getMessage()));
                    }
                    catch (RuntimeException e) {
                        future.completeExceptionally(new Exception("Unnoticed Exception occurred which is "+ e.getMessage() +   " its cause "+e.getCause()));
                    }


                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    future.completeExceptionally(new Exception(t.getMessage()));
                }
            });

        });

        return future;
    }
}
