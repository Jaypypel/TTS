package com.example.neptune.ttsapp;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.neptune.ttsapp.Network.APIErrorResponse;
import com.example.neptune.ttsapp.Network.APIResponse;
import com.example.neptune.ttsapp.Network.APISuccessResponse;
import com.example.neptune.ttsapp.Network.ActivityServiceInterface;
import com.example.neptune.ttsapp.Network.ResponseBody;
import com.example.neptune.ttsapp.Network.RoleServiceInterface;
import com.example.neptune.ttsapp.Util.DateConverter;
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
public class CreateRoleFragment extends Fragment {

    @Inject
    AppExecutors appExecutors;

    @Inject
    RoleServiceInterface roleService;

    private TextView user,date,time;
    private AutoCompleteTextView otherActivityName;
    private Button CreateRole;

    private SessionManager sessionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.create_role_fragment, container, false);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        user=view.findViewById(R.id.textViewCreateRoleUser);
        sessionManager = new SessionManager(getActivity().getApplicationContext());
        user.setText(sessionManager.getToken());

        date=view.findViewById(R.id.textViewCreateRoleDate);
        time=view.findViewById(R.id.textViewCreateRoleTime);


        appExecutors.getMainThread().execute(() -> {
            date.setText(DateConverter.currentDate());
            time.setText(DateConverter.currentTime());
        });

        otherActivityName= view.findViewById(R.id.editTextCreateRole);
        if (InternetConnectivity.isConnected()) {
            getOtherActivityNames().thenAccept(names -> {
                ArrayAdapter<String> userSelectAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, names);
                userSelectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                otherActivityName.setAdapter(userSelectAdapter);
            }).exceptionally(e -> {
                Toast.makeText(getActivity().getApplicationContext(), "Failure: "+e.getMessage(), Toast.LENGTH_LONG).show();
                return null;
            });

        }else {Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}


        CreateRole= view.findViewById(R.id.buttonCreateRole);


        CreateRole.setOnClickListener(v -> {
            CreateRole.setEnabled(false);
            if (InternetConnectivity.isConnected())
            {
                if (isOtherActivityName().isEmpty()){otherActivityName.setError("Role Name Be Empty");
                    CreateRole.setEnabled(true);
                    return;
                }
                addRole(isOtherActivityName()).thenAccept(isMeasurableAdded -> {
                    if(isMeasurableAdded.equals("successful")){
                        appExecutors.getMainThread().execute(() ->
                        {
                            Toast.makeText(getActivity().getApplicationContext(), "Role Added Successfully ", Toast.LENGTH_LONG).show();
                            otherActivityName.setText("");
                            CreateRole.setEnabled(true);

                        });
                    }else {
                        appExecutors.getMainThread().execute(() -> { Toast
                                .makeText(getActivity()
                                        .getApplicationContext(), "Insertion Failed", Toast.LENGTH_LONG)
                                .show();CreateRole.setEnabled(true);
                        });
                    }
                }).exceptionally(e -> {
                    Toast.makeText(getActivity().getApplicationContext(), "Failure: "+e.getMessage(), Toast.LENGTH_LONG).show();
                    CreateRole.setEnabled(true);

                    return null;
                });

            }else {Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();CreateRole.setEnabled(true);
            }



        });

        return view;
    }

    private String isOtherActivityName()
    {
        String actName = otherActivityName.getText().toString();
        if(actName.isEmpty()) { otherActivityName.setError("Role name Name Be Empty"); }
        return actName;
    }

    private String createdOn()
    {
        return DateConverter.getCurrentDateTime();
    }


    private CompletableFuture<String> addRole(String roleName) {
        CompletableFuture<String> future = new CompletableFuture<>();
        Call<ResponseBody> call = roleService.addRole(roleName);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    APIResponse apiResponse = APIResponse.create(response);
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
                    future.completeExceptionally(new Throwable("Exception occured while adding other activity due to " + e.getMessage()));
                }
                catch (RuntimeException e) {
                    future.completeExceptionally(new Throwable("Unnoticed Exception occurred which is "+ e.getMessage() +   " its cause "+e.getCause()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    public CompletableFuture<ArrayList<String>> getOtherActivityNames() {
        CompletableFuture<ArrayList<String>> future = new CompletableFuture<>();

        appExecutors.getNetworkIO().execute(() -> {
            Call<ResponseBody> usernamesResponse = roleService.getRoles();
            usernamesResponse.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                    try {
                        APIResponse apiResponse =   APIResponse.create(response);
                        JsonElement result = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody();
                        Gson gson = new Gson();
                        Type listType = new TypeToken<ArrayList<String>>() {}.getType();

                        if(result.isJsonArray()){
                            JsonArray usernames = result.getAsJsonArray();
                            ArrayList<String> list = gson.fromJson(usernames, listType);
                            future.complete(list);

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
                        future.completeExceptionally(new Throwable("Exception occured while getting usernames names due to " + e.getMessage()));
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

}    }
