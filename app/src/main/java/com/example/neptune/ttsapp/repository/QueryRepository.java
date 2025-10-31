package com.example.neptune.ttsapp.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.neptune.ttsapp.Network.APIErrorResponse;
import com.example.neptune.ttsapp.Network.APIResponse;
import com.example.neptune.ttsapp.Network.APISuccessResponse;
import com.example.neptune.ttsapp.Network.QueryServiceInterface;
import com.example.neptune.ttsapp.Network.ResponseBody;
import com.example.neptune.ttsapp.Query;
import com.example.neptune.ttsapp.QueryResolutionRequest;
import com.example.neptune.ttsapp.QueryResolutionRequest;
import com.example.neptune.ttsapp.RaiseAQueryRequest;
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

public class QueryRepository {

    private final QueryServiceInterface queryService;

    public QueryRepository(QueryServiceInterface queryService) {
        this.queryService = queryService;
    }

    public CompletableFuture<String> raiseQuery(RaiseAQueryRequest queryRequest){
        CompletableFuture<String> future = new CompletableFuture<>();
        Call<ResponseBody> call = queryService.raiseQuery(queryRequest);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {

                try {
                    APIResponse<?> apiResponse = APIResponse.create(response);
                    if (apiResponse != null) {
                        if (apiResponse instanceof APISuccessResponse) {
                            String message = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getMessage().getAsString();
                            if ("success".equals(message)) {
                                future.complete(message);
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
                } catch (ClassCastException e) {
                    future.completeExceptionally(new Exception("Unable to cast the response into required format due to " + e.getMessage()));
                } catch (IOException e) {
                    Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                    future.completeExceptionally(new Exception("Exception occurred while performing input output of query due to" + e.getMessage()));
                } catch (RuntimeException e) {
                    future.completeExceptionally(new Exception("Unnoticed Exception occurred which is " + e.getMessage() + " its cause " + e.getCause()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                future.completeExceptionally(new Exception(t.getMessage()));
            }
        });
        return future;
    }

    public CompletableFuture<ArrayList<Query>> getQueries(Long taskId){
        CompletableFuture<ArrayList<Query>> future = new CompletableFuture<>();
        Call<ResponseBody> call = queryService.queriesAgainstTask(  taskId);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
//                ArrayList<TaskDataModel> tasks = new ArrayList<>();
//                TaskDataModel task;
                try {
                    APIResponse apiResponse = APIResponse.create(response);
                    if (apiResponse instanceof APISuccessResponse) {
                        JsonElement bodyContent = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody().getAsJsonArray();
                        Gson gson = new Gson();
                        Type taskType = new TypeToken<ArrayList<Query>>() {
                        }.getType();
                        if (bodyContent.isJsonArray()) {
                            JsonArray content = bodyContent.getAsJsonArray();
                            ArrayList<Query> queries = gson.fromJson(content, taskType);
                            future.complete(queries);
                        }
                    }

                    if (apiResponse instanceof APIErrorResponse) {
                        String erMsg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                        future.completeExceptionally(new Throwable(erMsg));

                    }
                    if (apiResponse instanceof APIErrorResponse) {
                        future.completeExceptionally(new Throwable("empty response"));
                    }
                } catch (ClassCastException e) {
                    future.completeExceptionally(new Throwable("Unable to cast the response into required format due to " + e.getMessage()));
                } catch (IOException e) {
                    Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                    future.completeExceptionally(new Throwable("Exception occured while getting no. of completed tasks due to" + e.getMessage()));
                } catch (RuntimeException e) {
                    future.completeExceptionally(new Throwable("Unnoticed Exception occurred which is " + e.getMessage() + " its cause " + e.getCause()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                future.completeExceptionally(new Throwable(t.getMessage()));
            }
        });

        return future;
    }


    public CompletableFuture<String> Submit(QueryResolutionRequest queryResolution){
        CompletableFuture<String> future = new CompletableFuture<>();
        Call<ResponseBody> call = queryService.resolveQuery(queryResolution);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {

                try {
                    APIResponse<?> apiResponse = APIResponse.create(response);
                    if (apiResponse != null) {
                        if (apiResponse instanceof APISuccessResponse) {
                            String message = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getMessage().getAsString();
                            if ("success".equals(message)) {
                                future.complete(message);
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
                } catch (ClassCastException e) {
                    future.completeExceptionally(new Exception("Unable to cast the response into required format due to " + e.getMessage()));
                } catch (IOException e) {
                    Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                    future.completeExceptionally(new Exception("Exception occured while performing input output of query due to" + e.getMessage()));
                } catch (RuntimeException e) {
                    future.completeExceptionally(new Exception("Unnoticed Exception occurred which is " + e.getMessage() + " its cause " + e.getCause()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                future.completeExceptionally(new Exception(t.getMessage()));
            }
        });
        return future;
    }
}
