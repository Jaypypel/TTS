package com.example.neptune.ttsapp.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.neptune.ttsapp.AppExecutors;
import com.example.neptune.ttsapp.LearnerTaskCompletionDetails;
import com.example.neptune.ttsapp.Network.APIErrorResponse;
import com.example.neptune.ttsapp.Network.APIResponse;
import com.example.neptune.ttsapp.Network.APISuccessResponse;
import com.example.neptune.ttsapp.Network.ResponseBody;
import com.example.neptune.ttsapp.Network.TaskHandlerInterface;
import com.example.neptune.ttsapp.TaskAssignmentRequest;
import com.example.neptune.ttsapp.TaskSubmissionRequest;
import com.example.neptune.ttsapp.displayTaskToLearners.TaskDetails;
import com.example.neptune.ttsapp.displayTaskToLearners.TaskSummary;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskRepository {
    private final TaskHandlerInterface taskService;
    private final AppExecutors appExecutors;

    public TaskRepository(TaskHandlerInterface taskService, AppExecutors appExecutors) {
        this.taskService = taskService;
        this.appExecutors = appExecutors;
    }

//    public LiveData<Resource<ArrayList<String>>> getTaskNames(){
//        MutableLiveData<Resource<ArrayList<String>>> result = new MutableLiveData<>();
//        result.postValue(Resource.loading(null));
//        appExecutors.getNetworkIO().execute(() -> {
//            try{
//                Response<ArrayList<String>> response = taskService.getTaskNames();
//                if(response.isSuccessful() && response.body() != null){
//                    result.postValue(Resource.success(response.body()));
//                }else {
//                    result.postValue(Resource.error("Failed to fetch",null));
//
//                }
//            }catch (IOException e){
//                result.postValue(Resource.error("Network error",null));
//            }
//        });
//        return result;
//    }


    public CompletableFuture<ArrayList<TaskSummary>> getTasksAssignToLearner(String username, String status){
        CompletableFuture<ArrayList<TaskSummary>> future = new CompletableFuture<>();
        Call<ResponseBody> call = taskService.getLearnerTasks(username,status);
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
                        Type taskType = new TypeToken<ArrayList<TaskSummary>>() {
                        }.getType();
                        if (bodyContent.isJsonArray()) {
                            JsonArray content = bodyContent.getAsJsonArray();
                            ArrayList<TaskSummary> queries = gson.fromJson(content, taskType);
                            Log.e("data fetch",""+queries);
                            future.complete(queries);
                        }
                    }

                    if (apiResponse instanceof APIErrorResponse) {
                        String erMsg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                        future.completeExceptionally(new Exception(erMsg));

                    }
                    if (apiResponse instanceof APIErrorResponse) {
                        future.completeExceptionally(new Exception("empty response"));
                    }
                } catch (ClassCastException e) {
                    future.completeExceptionally(new Exception("Unable to cast the response into required format due to " + e.getMessage()));
                } catch (IOException e) {
                    Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                    future.completeExceptionally(new Exception("Exception occured while getting no. of completed tasks due to" + e.getMessage()));
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

    public CompletableFuture<LearnerTaskCompletionDetails> getTaskCompletionsDetails(Long id, String username){
        CompletableFuture<LearnerTaskCompletionDetails> future = new CompletableFuture<>();
        Call<ResponseBody> call = taskService.getCompletionDetails(id,username);
        call.enqueue( new Callback<>(){

            /**
             * @param call
             * @param response
             */
            @Override
            public void onResponse(Call call, Response response) {
                try {
                    APIResponse apiResponse = APIResponse.create(response);
                    if (apiResponse instanceof APISuccessResponse) {
                        JsonElement bodyContent = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody();
                        Gson gson = new Gson();
                        Type taskType = new TypeToken<LearnerTaskCompletionDetails>() {
                        }.getType();
                        if (bodyContent.isJsonObject()) {
                            JsonObject content = bodyContent.getAsJsonObject();
                            LearnerTaskCompletionDetails taskDetails = gson.fromJson(content, taskType);
                            Log.e("data fetch",""+taskDetails);
                            future.complete(taskDetails);
                        }
                    }

                    if (apiResponse instanceof APIErrorResponse) {
                        String erMsg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                        future.completeExceptionally(new Exception(erMsg));

                    }
                    if (apiResponse instanceof APIErrorResponse) {
                        future.completeExceptionally(new Exception("empty response"));
                    }

                } catch (ClassCastException e) {
                    future.completeExceptionally(new Exception("Unable to cast the response into required format due to " + e.getMessage()));
                } catch (IOException e) {
                    Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                    future.completeExceptionally(new Exception("Exception occured while getting no. of completed tasks due to" + e.getMessage()));
                } catch (RuntimeException e) {
                    future.completeExceptionally(new Exception("Unnoticed Exception occurred which is " + e.getMessage() + " its cause " + e.getCause()));
                }
            }

            /**
             * @param call
             * @param t
             */
            @Override
            public void onFailure(@NonNull Call call, @NonNull Throwable t) {
                future.completeExceptionally(new Exception(t.getMessage()));
            }
        });
        return future;

    }


    public CompletableFuture<TaskDetails> getTaskDetails(Long id){
        CompletableFuture<TaskDetails> future = new CompletableFuture<>();
        Call<ResponseBody> call = taskService.getLearnerTaskDetails(id);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
//                ArrayList<TaskDataModel> tasks = new ArrayList<>();
//                TaskDataModel task;
                try {
                    APIResponse<ResponseBody> apiResponse = APIResponse.create(response);
                    if (apiResponse instanceof APISuccessResponse) {
                        JsonElement bodyContent = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody();
                        Gson gson = new Gson();
                        Type taskType = new TypeToken<TaskDetails>() {
                        }.getType();
                        if (bodyContent.isJsonObject()) {
                            JsonObject content = bodyContent.getAsJsonObject();
                            TaskDetails taskDetails = gson.fromJson(content, taskType);
                            Log.e("data fetch",""+taskDetails);
                            future.complete(taskDetails);
                        }
                    }

                    if (apiResponse instanceof APIErrorResponse) {
                        String erMsg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                        future.completeExceptionally(new Exception(erMsg));

                    }
                    if (apiResponse instanceof APIErrorResponse) {
                        future.completeExceptionally(new Exception("empty response"));
                    }
                } catch (ClassCastException e) {
                    future.completeExceptionally(new Exception("Unable to cast the response into required format due to " + e.getMessage()));
                } catch (IOException e) {
                    Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                    future.completeExceptionally(new Exception("Exception occured while getting no. of completed tasks due to" + e.getMessage()));
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


    public CompletableFuture<ArrayList<String>> assignTaskToUser(TaskAssignmentRequest taskManagement){
        CompletableFuture<ArrayList<String>> future = new CompletableFuture<>();
        Call<ResponseBody> call = taskService.assignTaskToLearner(taskManagement);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {

                try {
                    APIResponse apiResponse = APIResponse.create(response);
                    if (apiResponse != null) {
                        if (apiResponse instanceof APISuccessResponse) {
                            String message = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getMessage().getAsString();
                            String taskResponse = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody().getAsString();
                            if ("Successful".equals(message)) {
                               ArrayList<String> arr = new ArrayList<>();
                               arr.add(message);
                               arr.add(taskResponse);
                               future.complete(arr);
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
                    future.completeExceptionally(new Exception("Exception occured while performing input output of task due to" + e.getMessage()));
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



    public CompletableFuture<String> updateTaskStatus(Long id, String status){
        CompletableFuture<String> future = new CompletableFuture<>();
        Call<ResponseBody> call = taskService.updateTaskManagementStatus(id,status);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {

                try {
                    APIResponse apiResponse = APIResponse.create(response);
                    if (apiResponse != null) {
                        if (apiResponse instanceof APISuccessResponse) {
                            String message = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getMessage().getAsString();
                            if ("Success".equals(message)) {
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
                    future.completeExceptionally(new Exception("Exception occured while updating task due to" + e.getMessage()));
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

    public CompletableFuture<String> submitTask(Long id, TaskSubmissionRequest request) {
        CompletableFuture<String> future = new CompletableFuture<>();
        Call<ResponseBody> call = taskService.submitTask(id,request);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    APIResponse apiResponse = APIResponse.create(response);
                    if (apiResponse != null) {
                        if (apiResponse instanceof APISuccessResponse) {
                            String message = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getMessage().getAsString();
                            if ("Successful".equals(message)) {
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
                    future.completeExceptionally(new Exception("Exception occured while performing input output of task due to" + e.getMessage()));
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
