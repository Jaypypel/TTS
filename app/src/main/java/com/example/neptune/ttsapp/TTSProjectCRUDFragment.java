package com.example.neptune.ttsapp;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.neptune.ttsapp.DTO.TaskManagement;
import com.example.neptune.ttsapp.Network.APIErrorResponse;
import com.example.neptune.ttsapp.Network.APIResponse;
import com.example.neptune.ttsapp.Network.APISuccessResponse;
import com.example.neptune.ttsapp.Network.ActivityServiceInterface;
import com.example.neptune.ttsapp.Network.ProjectServiceInterface;
import com.example.neptune.ttsapp.Network.ResponseBody;
import com.example.neptune.ttsapp.Network.UserServiceInterface;
import com.example.neptune.ttsapp.Util.DateConverter;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Query;


@AndroidEntryPoint
public class TTSProjectCRUDFragment extends Fragment {

    @Inject
    AppExecutors appExecutors;

    @Inject
    ProjectServiceInterface projectService;

   @Inject
    UserServiceInterface userService;

   @Inject
    ActivityServiceInterface activityService;
   //@Inject
   //@Inject


    public TTSProjectCRUDFragment() { }

    private TextView user,date,time;
    private AutoCompleteTextView projectName,projectCode;
    private Button addProject;
    private Spinner userSelect,activitySelect;

    private SessionManager sessionManager;

    private ArrayList<ActivityDataModel> activityDataModels;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ttsproject_crud, container, false);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        user=(TextView)view.findViewById(R.id.textViewProjectCRUDUser);
        sessionManager = new SessionManager(getActivity().getApplicationContext());
        user.setText(sessionManager.getUserID());

        date=(TextView)view.findViewById(R.id.textViewProjectCRUDDate);
        time=(TextView)view.findViewById(R.id.textViewProjectCRUDTime);


           appExecutors.getMainThread().execute(() -> {
            date.setText(DateConverter.currentDate());
            time.setText(DateConverter.currentTime());
        });

        projectCode=(AutoCompleteTextView) view.findViewById(R.id.editTextProjectCRUDProjectCode);
        getProjectCodes().thenAccept(result -> {
            appExecutors.getMainThread().execute(() -> {
                ArrayAdapter<String> projectCodeAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,result);
                projectCode.setAdapter(projectCodeAdapter);
            });
        });
//
//        projectCode=(AutoCompleteTextView) view.findViewById(R.id.editTextProjectCRUDProjectCode);
//        ArrayAdapter<String> projectCodeAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,getProjectCodeList());
//        projectCode.setAdapter(projectCodeAdapter);

        projectName=(AutoCompleteTextView) view.findViewById(R.id.editTextProjectCRUDProjectName);

        getProjectNames().thenAccept(result -> {
            ArrayList<String> projectNames = result;
            Log.e("projectNames",""+projectNames);
            appExecutors.getMainThread().execute(() -> {
                ArrayAdapter<String> projectNameAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,result);
                projectName.setAdapter(projectNameAdapter);
            });


        });
//        ArrayAdapter<String> projectNameAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,getProjectNameList());
//        projectName.setAdapter(projectNameAdapter);

        addProject=(Button)view.findViewById(R.id.buttonProjectCRUDAdd);

        if (InternetConnectivity.isConnected()) {
                        userSelect=(Spinner) view.findViewById(R.id.spinnerProjectCRUDUserSelect);
            getUsernames().thenAccept(usernames -> {
                ArrayList<String>  users = usernames;
                users.add(0,"Select user");
                ArrayAdapter<String> userSelectAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,users);
                userSelectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                userSelect.setAdapter(userSelectAdapter);
            }).exceptionally(e -> {Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
                return null;
            });
//            userSelect=(Spinner) view.findViewById(R.id.spinnerProjectCRUDUserSelect);
//            ArrayList users = getUserList();
//            users.add(0, "Select User");
//            ArrayAdapter<String> adapterMeasurable = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,users);
//            adapterMeasurable.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            userSelect.setAdapter(adapterMeasurable);
        }else { Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}


    activitySelect=(Spinner) view.findViewById(R.id.spinnerProjectCRUDActivitySelect);
        if (InternetConnectivity.isConnected()) {
            userSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    appExecutors.getNetworkIO().execute(() -> {
                        getActivities(getUser()).thenAccept(activities ->{
                            activityDataModels = activities;
                            ArrayAdapter<ActivityDataModel> activitySelectAdapter = new ArrayAdapter<ActivityDataModel>
                                    (getActivity(), android.R.layout.simple_spinner_item,activityDataModels);
                            activitySelectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            activitySelect.setAdapter(activitySelectAdapter);
                        }).exceptionally(e -> {
                            Toast.makeText(getActivity().getApplicationContext(),"Failed to get activities",Toast.LENGTH_LONG).show();
                            return null;
                        });
                    });

//                    activityDataModels = getActivityList(getUser());
//                    ArrayAdapter<ActivityDataModel> activitySelectAdapter = new ArrayAdapter<ActivityDataModel>
//                            (getActivity(), android.R.layout.simple_spinner_item,activityDataModels);
//                    activitySelectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                    activitySelect.setAdapter(activitySelectAdapter);

                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }else {Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}



        addProject.setOnClickListener(v -> {

            try
            {
                if (InternetConnectivity.isConnected())
                {
                   if (isProjectCode().isEmpty()){projectCode.setError("Project Code Be Empty");}
                   else if (isProjectName().isEmpty()){projectName.setError("Project Name Be Empty");}
                   else
                   {

                       addProject(getUser(), getAct(), isProjectCode(), isProjectName(), createdOn()).thenAccept(isProjectAdded -> {
                           if(isProjectAdded.equals("successful")){
                               appExecutors.getMainThread().execute(() ->
                               {
                                   Toast.makeText(getActivity().getApplicationContext(), "Project Inserted ", Toast.LENGTH_LONG).show();
                                   projectCode.setText("");
                                   projectName.setText("");
                               });
                           }else {
                               appExecutors.getMainThread().execute(() ->
                               {
                                   Toast.makeText(getActivity().getApplicationContext(), "Insertion Failed ", Toast.LENGTH_LONG).show();

                               });
                           }
                       }).exceptionally(e -> {
                           Toast.makeText(getActivity().getApplicationContext(), "Failed to add activity due to "+e.getMessage(), Toast.LENGTH_LONG).show();

                           return null;
                       });
//
//                       String result = insertProject(getUser(), getAct(), isProjectCode(), isProjectName(), createdOn());
//                       if (result.equals("true")) {
//                           Toast.makeText(getActivity().getApplicationContext(), "Project Inserted ", Toast.LENGTH_LONG).show();
//                           projectCode.setText("");
//                           projectName.setText("");
//                       } else {
//                           Toast.makeText(getActivity().getApplicationContext(), "Insertion Failed", Toast.LENGTH_LONG).show();
//                       }
                   }
                }else {Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}


            }
            catch (Exception e){e.printStackTrace();}
        });

        return view;
    }

    private String getUser()
    {
        return userSelect.getSelectedItem().toString().trim();
    }

    private Long getAct()
    {
        String activity = activitySelect.getSelectedItem().toString().trim();
        return Long.valueOf(activity.split("-")[0]);


    }

    private String isProjectCode()
    {
        String projCode = projectCode.getText().toString();
        if(projCode.isEmpty()) { projectCode.setError("Project Code Be Empty"); }
        return projCode;
    }

    private String isProjectName()
    {
        String projName = projectName.getText().toString();
        if(projName.isEmpty()) { projectName.setError("Project Name Be Empty"); }
        return projName;
    }

    private String createdOn()
    {
//        Calendar calendar = Calendar.getInstance();
//        Timestamp delegationTimestamp = new Timestamp(calendar.getTime().getTime());
//        return delegationTimestamp.toString();
        return DateConverter.getCurrentDateTime();
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
                      if (apiResponse instanceof  APISuccessResponse){
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
                            future.completeExceptionally(new Throwable(erMsg));

                        }
                        if (apiResponse instanceof APIErrorResponse) {
                            future.completeExceptionally(new Throwable("empty response"));
                        }

                    }
                    catch (ClassCastException e){
                        future.completeExceptionally(new Throwable("Unable to cast the response into required format due to "+ e.getMessage()));
                    }
                    catch (IOException e) {
                        Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                        future.completeExceptionally(new Throwable("Exception occured while getting  usernames due to " + e.getMessage()));
                    }
                    catch (RuntimeException e) {
                        future.completeExceptionally(new Throwable("Unnoticed Exception occurred which is "+ e.getMessage() +   " its cause "+e.getCause()));
                    }

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    future.completeExceptionally(new Throwable(t.getMessage()));

                }
            });

        });

        return future;
    }
public CompletableFuture<ArrayList<String>> getProjectNames() {
        CompletableFuture<ArrayList<String>> future = new CompletableFuture<>();

        appExecutors.getNetworkIO().execute(() -> {
            Call<ResponseBody> call = projectService.getProjectNameList();
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                    try {
                        APIResponse apiResponse =   APIResponse.create(response);
                       if (apiResponse instanceof APISuccessResponse){
                           JsonElement result = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody();
                           Gson gson = new Gson();
                           Type listType = new TypeToken<ArrayList<String>>() {}.getType();
                           if(result.isJsonArray()){
                               JsonArray projectNames = result.getAsJsonArray();
                               ArrayList<String> list = gson.fromJson(projectNames, listType);
                               Log.e("ProjectNames", ""+list);
                               future.complete(list);

                           }
                       }
                        if (apiResponse instanceof APIErrorResponse) {
                            String erMsg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                            future.completeExceptionally(new Throwable(erMsg));

                        }
                        if (apiResponse instanceof APIErrorResponse) {
                            future.completeExceptionally(new Throwable("empty response"));
                        }

                    }
                    catch (ClassCastException e){
                        future.completeExceptionally(new Throwable("Unable to cast the response into required format due to "+ e.getMessage()));
                    }
                    catch (IOException e) {
                        Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                        future.completeExceptionally(new Throwable("Exception occured while getting project names due to " + e.getMessage()));
                    }
                    catch (RuntimeException e) {
                        future.completeExceptionally(new Throwable("Unnoticed Exception occurred which is "+ e.getMessage() +   " its cause "+e.getCause()));
                    }

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                 future.completeExceptionally(new Throwable(t.getMessage()));

                }
            });

        });

        return future;
    }
public CompletableFuture<ArrayList<String>> getProjectCodes() {
        CompletableFuture<ArrayList<String>> future = new CompletableFuture<>();

        appExecutors.getNetworkIO().execute(() -> {
            Call<ResponseBody> call = projectService.getProjectCodesList();
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                    try {
                        APIResponse apiResponse =   APIResponse.create(response);

                        if (apiResponse instanceof APISuccessResponse){
                            JsonElement result = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody();
                            Gson gson = new Gson();
                            Type listType = new TypeToken<ArrayList<String>>() {}.getType();
                            if(result.isJsonArray()){
                                JsonArray projectCodes = result.getAsJsonArray();
                                ArrayList<String> list = gson.fromJson(projectCodes, listType);
                                future.complete(list);
                            }
                        }

                        if (apiResponse instanceof APIErrorResponse) {
                            String erMsg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                            future.completeExceptionally(new Throwable(erMsg));

                        }
                        if (apiResponse instanceof APIErrorResponse) {
                            future.completeExceptionally(new Throwable("empty response"));
                        }

                    }
                    catch (ClassCastException e){
                        future.completeExceptionally(new Throwable("Unable to cast the response into required format due to "+ e.getMessage()));
                    }
                    catch (IOException e) {
                        Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                        future.completeExceptionally(new Throwable("Exception occured while getting project codes due to " + e.getMessage()));
                    }
                    catch (RuntimeException e) {
                        future.completeExceptionally(new Throwable("Unnoticed Exception occurred which is "+ e.getMessage() +   " its cause "+e.getCause()));
                    }

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    future.completeExceptionally(new Throwable(t.getMessage()));

                }
            });

        });

        return future;
    }

    public CompletableFuture<ArrayList<ActivityDataModel>> getActivities(String username) {
        CompletableFuture<ArrayList<ActivityDataModel>> future = new CompletableFuture<>();

        appExecutors.getNetworkIO().execute(() -> {
            Call<ResponseBody> call = activityService.getActivities(username);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                    try {
                        APIResponse apiResponse =   APIResponse.create(response);
                        if (apiResponse instanceof APISuccessResponse){
                            JsonElement result = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody();
                            Gson gson = new Gson();
                            Type listType = new TypeToken<ArrayList<ActivityDataModel>>() {}.getType();

                            if(result.isJsonArray()){
                                JsonArray jsonArray = result.getAsJsonArray();
                                ArrayList<ActivityDataModel> list = gson.fromJson(jsonArray, listType);
                                future.complete(list);

                            }
                        }
                        if (apiResponse instanceof APIErrorResponse) {
                            String erMsg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                            future.completeExceptionally(new Throwable(erMsg));

                        }
                        if (apiResponse instanceof APIErrorResponse) {
                            future.completeExceptionally(new Throwable("empty response"));
                        }

                }
                catch (ClassCastException e){
                    future.completeExceptionally(new Throwable("Unable to cast the response into required format due to "+ e.getMessage()));
                }
                catch (IOException e) {
                    Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                    future.completeExceptionally(new Throwable("Exception occured while getting activities due to" + e.getMessage()));
                }
                catch (RuntimeException e) {
                    future.completeExceptionally(new Throwable("Unnoticed Exception occurred which is "+ e.getMessage() +   " its cause "+e.getCause()));
                }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    future.completeExceptionally(new Throwable(t.getMessage()));

                }
            });

        });

        return future;
    }

     private CompletableFuture<String> addProject(String username,
                                                  Long activityId, String projectCode, String prjNme
             ,String createdOn) {
    CompletableFuture<String> future = new CompletableFuture<>();
        Call<ResponseBody> call = projectService.addProject(username,
                        activityId, projectCode, prjNme
                , createdOn);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                  APIResponse  apiResponse = APIResponse.create(response);
                    if (apiResponse != null) {
                        if (apiResponse instanceof APISuccessResponse) {
                            String message = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getMessage().getAsString();
                           // JsonObject dtsobject = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody().getAsJsonObject();
                            if ("successful".equals(message)) {
                                future.complete(message);
                            }
                        }

                        if (apiResponse instanceof APIErrorResponse) {
                            String erMsg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                            future.completeExceptionally(new Throwable(erMsg));

                        }
                        if (apiResponse instanceof APIErrorResponse) {
                            future.completeExceptionally(new Throwable("empty response"));
                        }
                    }
                }
                catch (ClassCastException e){
                        future.completeExceptionally(new Throwable("Unable to cast the response into required format due to "+ e.getMessage()));
                    }
                catch (IOException e) {
                        Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                        future.completeExceptionally(new Throwable("Exception occured while  adding a project due to" + e.getMessage()));
                    }
                catch (RuntimeException e) {
                        future.completeExceptionally(new Throwable("Unnoticed Exception occurred which is "+ e.getMessage() +   " its cause "+e.getCause()));
                    }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                future.completeExceptionally(new Throwable(t.getMessage()));
            }
        });
         return future;
     }
}
