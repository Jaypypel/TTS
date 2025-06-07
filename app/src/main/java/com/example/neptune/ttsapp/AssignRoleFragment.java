package com.example.neptune.ttsapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.neptune.ttsapp.Network.APIErrorResponse;
import com.example.neptune.ttsapp.Network.APIResponse;
import com.example.neptune.ttsapp.Network.APISuccessResponse;
import com.example.neptune.ttsapp.Network.ActivityServiceInterface;
import com.example.neptune.ttsapp.Network.ResponseBody;
import com.example.neptune.ttsapp.Network.RoleServiceInterface;
import com.example.neptune.ttsapp.Network.UserServiceInterface;
import com.example.neptune.ttsapp.Util.DateConverter;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class AssignRoleFragment extends Fragment {
    @Inject
    AppExecutors appExecutors;

    @Inject
    UserServiceInterface userService;

   // @Inject
   // ActivityServiceInterface activityService;

    @Inject
    RoleServiceInterface roleService;

    private TextView user,date,time;
    private AutoCompleteTextView roleName;

    private Button assignRole;
    private MaterialAutoCompleteTextView userSelect;

    private SessionManager sessionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.assign_role_fragment, container, false);

        user=view.findViewById(R.id.textViewUser);
        sessionManager = new SessionManager(getActivity().getApplicationContext());
        user.setText(sessionManager.getToken());

        date=view.findViewById(R.id.textViewDate);
        time=view.findViewById(R.id.textViewTime);


        appExecutors.getMainThread().execute(() -> {
            date.setText(DateConverter.currentDate());
            time.setText(DateConverter.currentTime());
        });


        roleName =view.findViewById(R.id.editTextAssignRole);

        userSelect=view.findViewById(R.id.spinnerActCRUDUserSelect);
        if (InternetConnectivity.isConnected()) {

            appExecutors.getNetworkIO().execute(() ->
                    getUsernames()
                            .thenAccept(this::accept)
                            .exceptionally(e -> {
                                Toast.makeText(getActivity().getApplicationContext(), "Failure: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                return null;
                            }));

        }else {Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}


        try {
            userSelect.setOnItemClickListener((parent, v, position, id) -> {
                if (InternetConnectivity.isConnected()) {

                    roleName.setText("");
//                    Log.e("Debugging", "Checking userSelect.isSelected value"+userSelect.isSelected());
//                    Log.e("Debugging", "Checking !getUser().isEmpty()) value"+!getUser().isEmpty());
//                    if(!getUser().equals("Select user") && !getUser().isBlank()) {
                        appExecutors.getNetworkIO().execute(() -> getRoleNames().thenAccept(roleNames -> {
                            appExecutors.getMainThread().execute(() -> {
                                ArrayAdapter<String> roleNameAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, roleNames);
                                roleName.setAdapter(roleNameAdapter);
                            });
                        }).exceptionally(e -> {
                            Toast.makeText(requireContext(), "Failure: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            return null;
                        }));

                    //}
                } else {
                    Toast.makeText(requireContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
                }
            });
        }catch (Exception e){e.printStackTrace();}

        assignRole= view.findViewById(R.id.buttonAssignRole);


        assignRole.setOnClickListener(v -> {
            assignRole.setEnabled(false);

            if (InternetConnectivity.isConnected())
            {
                if(isroleName().isEmpty()){roleName.setError("Role name can not be empty");
                    assignRole.setEnabled(true);
                    return ;
                }

                assignRole(getUser(),isroleName(),createdOn()).thenAccept(isActivityAdded -> {
                    if(isActivityAdded.equals("successful")){
                        appExecutors.getMainThread().execute(() ->
                        {
                            Toast.makeText(getActivity().getApplicationContext(), "Role assigned successfully ", Toast.LENGTH_LONG).show();
                            roleName.setText("");
                            assignRole.setEnabled(true);

                        });
                    }else {
                        appExecutors.getMainThread().execute(() ->
                        {Toast.makeText(getActivity().getApplicationContext(), "Role couldn't assigned ", Toast.LENGTH_LONG).show();
                            assignRole.setEnabled(true);});
                    }
                }).exceptionally(e -> {
                    Toast.makeText(getActivity().getApplicationContext(), "Failure :"+e.getMessage(), Toast.LENGTH_LONG).show();
                    assignRole.setEnabled(true);
                    return null;
                });

            }else {Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
                assignRole.setEnabled(true);
            }

        });



        return view;
    }


    private String getUser()
    {
        return userSelect.getText().toString().trim();
    }

    private String isroleName()
    {
        String actName = roleName.getText().toString();
        if(actName.isEmpty()) { roleName.setError("Role name is empty"); }
        return actName;
    }

    private String createdOn()
    {
        return DateConverter.getCurrentDateTime();
    }

    private CompletableFuture<String> assignRole(String username, String role, String createdOn) {
        CompletableFuture<String> result = new CompletableFuture<>();
        //it is used for to message & dts Id from ResponseBody object
        appExecutors.getNetworkIO().execute(() -> {
            Call<ResponseBody> call = roleService.assignRole(username,role);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    APIResponse apiResponse = null;
                    try {
                        apiResponse = APIResponse.create(response);
                        if (apiResponse != null) {
                            if (apiResponse instanceof APISuccessResponse) {
                                String message = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getMessage().getAsString();

                                if ("successful".equals(message)) {
                                    result.complete(message);
                                }
                            }
                            if (apiResponse instanceof APIErrorResponse) {
                                String erMsg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                                result.completeExceptionally(new Throwable(erMsg));

                            }
                            if (apiResponse instanceof APIErrorResponse) {
                                result.completeExceptionally(new Throwable("empty response"));
                            }
                        }
                    }
                    catch (ClassCastException e){
                        result.completeExceptionally(new Throwable("Unable to cast the response into required format due to "+ e.getMessage()));
                    }
                    catch (IOException e) {
                        Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                        result.completeExceptionally(new Throwable("Exception occured while getting measurables due to" + e.getMessage()));
                    }
                    catch (RuntimeException e) {
                        result.completeExceptionally(new Throwable("Unnoticed Exception occurred which is "+ e.getMessage() +   " its cause "+e.getCause()));
                    }


                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    result.completeExceptionally(new Throwable(t.getMessage()));
                }
            });
        });

        return result;
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
                        future.completeExceptionally(new Throwable("Exception occured while getting measurables due to" + e.getMessage()));
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

    public CompletableFuture<ArrayList<String>> getRoleNames() {
        CompletableFuture<ArrayList<String>> future = new CompletableFuture<>();

        appExecutors.getNetworkIO().execute(() -> {
            Call<ResponseBody> call = roleService.getRoles();
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        APIResponse apiResponse =   APIResponse.create(response);
                        if(apiResponse instanceof  APISuccessResponse){
                            JsonElement result = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody();
                            Gson gson = new Gson();
                            Type listType = new TypeToken<ArrayList<String>>() {}.getType();
                            if(result.isJsonArray()){
                                JsonArray roleNames = result.getAsJsonArray();
                                ArrayList<String> list = gson.fromJson(roleNames, listType);
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
                        future.completeExceptionally(new Throwable("Exception occured while getting measurables due to" + e.getMessage()));
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

    private void accept(ArrayList<String> usernames) {
        appExecutors.getMainThread().execute(()-> {
            ArrayList<String> users = usernames;
            users.add(0, "Select user");
            ArrayAdapter<String> userSelectAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, users);
//                userSelectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            userSelect.setSimpleItems(users.stream().toArray(String[]::new));
            userSelect.setOnClickListener(v -> userSelect.showDropDown());
        });
    }
}
