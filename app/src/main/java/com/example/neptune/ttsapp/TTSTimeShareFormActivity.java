package com.example.neptune.ttsapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.neptune.ttsapp.DTO.TimeShareDTO;
import com.example.neptune.ttsapp.Network.APIEmptyResponse;
import com.example.neptune.ttsapp.Network.APIErrorResponse;
import com.example.neptune.ttsapp.Network.APIResponse;
import com.example.neptune.ttsapp.Network.APISuccessResponse;
import com.example.neptune.ttsapp.Network.JSONConfig;
import com.example.neptune.ttsapp.Network.MeasurableServiceInterface;
import com.example.neptune.ttsapp.Network.ResponseBody;
import com.example.neptune.ttsapp.Network.TaskHandlerInterface;
import com.example.neptune.ttsapp.Network.TimeShareServiceInterface;
import com.example.neptune.ttsapp.Util.DateConverter;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
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
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


@AndroidEntryPoint
public class TTSTimeShareFormActivity extends AppCompatActivity {

    @Inject
    AppExecutors appExecutor;

    @Inject
    MeasurableServiceInterface measurableService;

    @Inject
    TimeShareServiceInterface timeShareService;

    @Inject
    TaskHandlerInterface taskHandlerService;

    private EditText date,startTime,endTime,description,timeShareMeasurableQty,timeShareMeasurableUnit;
    private Button btnCancel,btnPreview,btnSubmit,addMeasurable;
    private TextView user,activityName,taskName,projCode,projName;
    private Spinner spinnerMeasurableName;
//    private ProgressBar progressBar;
    private int mYear, mMonth, mDay, mHour, mMinute;

    private SessionManager sessionManager;


    private ListView listView;
    private MeasurableListCustomAdapter measurableListCustomAdapter;

    private TaskDataModel allocatedTaskDetails,acceptedTaskDetails,processingTaskDetails;
    Long allocatedDelegationTaskId,acceptedDelegationTaskId,processingDelegationTaskId;
    ArrayList<MeasurableListDataModel> measurables = new ArrayList<>();

        @Override


        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_ttstime_share_form);
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

        user=findViewById(R.id.textViewUser);
        sessionManager = new SessionManager(getApplicationContext());
        user.setText(sessionManager.getToken());

        date=findViewById(R.id.editTextDate);

        activityName=findViewById(R.id.TSTextViewActName);
        taskName=findViewById(R.id.TSTextViewTaskName);
        projCode=findViewById(R.id.TSTextViewProjNo);
        projName=findViewById(R.id.TSTextViewProjName);
        startTime=findViewById(R.id.editTextStartTime);
        endTime=findViewById(R.id.editTextEndTime);
        description=findViewById(R.id.editTextDescription);

        btnCancel=findViewById(R.id.buttonCancel);

        btnSubmit=findViewById(R.id.buttonSubmit);


        // Code for Measurable list
        listView=(ListView)findViewById(R.id.listTimeShareMeasurable);
        addMeasurable=findViewById(R.id.buttonTimeShareMeasurableAdd);
        timeShareMeasurableQty=findViewById(R.id.editTextTimeShareMeasurableQty);
        timeShareMeasurableUnit=findViewById(R.id.editTextTimeShareMeasurableUnit);



            processingTaskDetails =(TaskDataModel) getIntent().getSerializableExtra("TaskProcessingDetails");
            acceptedTaskDetails = (TaskDataModel) getIntent().getSerializableExtra("TaskAcceptedDetails");

//            if (acceptedTaskDetails !=null){
//                activityName.setText(acceptedTaskDetails.getActivityName());
//                taskName.setText(acceptedTaskDetails.getTaskName());
//                projCode.setText(acceptedTaskDetails.getProjectCode());
//                projName.setText(acceptedTaskDetails.getProjectName());
//            }

            if (processingTaskDetails!=null)
            {
                processingDelegationTaskId= processingTaskDetails.getId();
                activityName.setText(processingTaskDetails.getActivityName());
                taskName.setText(processingTaskDetails.getTaskName());
                projCode.setText(processingTaskDetails.getProjectCode());
                projName.setText(processingTaskDetails.getProjectName());

            }

        //Code For set measurable list to spinner
            if (InternetConnectivity.isConnected()) {


                appExecutor.getNetworkIO().execute(() -> getAllocatedMeasurableList(processingDelegationTaskId).thenAccept(measurableList -> {
                    Log.e("measurableList"," "+measurableList);
                   appExecutor.getMainThread().execute(() -> {
                        spinnerMeasurableName = findViewById(R.id.spinnerTimeShareMeasurable);
                        ArrayAdapter<MeasurableListDataModel> adapterMeasurable = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, measurableList);
                        adapterMeasurable.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerMeasurableName.setAdapter(adapterMeasurable);

                    });
                }).exceptionally(e ->{    Log.e("Error", "Failed to fetch measurable list: " + e.getMessage());
                    appExecutor.getMainThread().execute(() ->
                            Toast.makeText(getApplicationContext(), "couldn't fetch measurable list", Toast.LENGTH_LONG).show());
                    return null;
                }));

            }else {Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}


    //Add Measurable in the ListView




        addMeasurable.setOnClickListener(v ->
        {
            try
            {
                String tmeShrMsrble =  spinnerMeasurableName.getSelectedItem().toString() != null ? spinnerMeasurableName.getSelectedItem().toString(): "undefined";
                String tmeShreMsrbleQty = timeShareMeasurableQty.getText().toString();
                String tmeShreMsrblUnit = timeShareMeasurableUnit.getText().toString();
                String[]   parts = tmeShrMsrble.split("-");
                String numberPart = parts[0].split("\\.")[0]; // Cast to int to remove decimal
                String wordPart = parts[1];

                if (tmeShreMsrbleQty.isEmpty()){
                    timeShareMeasurableQty.setError("Qty can't be blank");
                    return;
                }
                if (tmeShreMsrblUnit.isEmpty()){
                    timeShareMeasurableUnit.setError("Unit can't be blank");
                    return;
                }

                MeasurableListDataModel m = new MeasurableListDataModel();
                m.setId(numberPart);
                m.setMeasurableName(wordPart);
                m.setMeasurableQty(tmeShreMsrbleQty);
                m.setMeasurableUnit(tmeShreMsrblUnit);

                if(!measurables.contains(m)){
                    measurables.add(m);
                }else {
                    Snackbar snackbar = Snackbar.make(v, "Warning! measurable entry is already present", Snackbar.LENGTH_LONG);
                    View snackbarView = snackbar.getView();
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackbarView.getLayoutParams();
                    params.gravity = Gravity.CENTER;
                    snackbarView.setLayoutParams(params);
                    snackbar.show();
                }
                measurableListCustomAdapter = new MeasurableListCustomAdapter(measurables, getApplicationContext());
                listView.setAdapter(measurableListCustomAdapter);
                clear();
            }catch (Exception e){e.printStackTrace();}
        });


        btnSubmit.setOnClickListener(v -> {
            btnSubmit.setEnabled(false);
              if (InternetConnectivity.isConnected())
                {
                    if (isDateValid().isEmpty()){date.setError("Date Cannot Be Empty");
                        btnSubmit.setEnabled(true);
                        return;
                    }
                   if (isStartTimeValid().isEmpty()){startTime.setError("Start Time Cannot Be Empty");
                       btnSubmit.setEnabled(true);
                       return;
                   }
                    if (isEndTimeValid().isEmpty()){endTime.setError("End Time Cannot Be Empty");
                        btnSubmit.setEnabled(true);
                        return;
                    }
                    if (timeDifference().contains("-")) {
                        Toast.makeText(getApplicationContext(), "Please Enter Valid End Time", Toast.LENGTH_LONG)
                                .show();
                        btnSubmit.setEnabled(true);
                        return;
                    }
                    if (isDescriptionValid().isEmpty()){
                        Toast
                                .makeText(getApplicationContext(), "Description can't be empty", Toast.LENGTH_LONG)
                                .show();
                        btnSubmit.setEnabled(true);
                        return;
                    }
                    if (measurables != null & measurables.isEmpty() || listView == null) {

                        Toast.makeText(getApplicationContext(), "Measurable list is empty", Toast.LENGTH_LONG).show();
                        btnSubmit.setBackgroundResource(android.R.drawable.btn_default);
                        btnSubmit.setEnabled(true);
                        return;
                    }

//                          progressBar.setVisibility(View.VISIBLE);
                        TimeShareDTO timeShare = new TimeShareDTO();
                        timeShare.setTaskHandlerId(processingDelegationTaskId);
                        timeShare.setDate(isDateValid());
                        timeShare.setStartTime(isStartTimeValid());
                        timeShare.setEndTime(isEndTimeValid());
                        timeShare.setTimeDifference(timeDifference());
                        timeShare.setDescription(isDescriptionValid());
                        timeShare.setCreatedOn(delegationTime());
                        appExecutor.getNetworkIO().execute(() -> addTimeShare(timeShare).thenCompose(result -> {
                            Long id = Long.valueOf(result.get(1));
                            return addTimeShareMeasurables(id,measurables).thenAccept(finalResult -> {
                                if (finalResult) {
                                    appExecutor.getMainThread().execute(() -> {
                                        Toast.makeText(getApplicationContext(), "Time Share Inserted", Toast.LENGTH_LONG).show();
                                        clearAll();
                                        btnSubmit.setEnabled(true);

                                        // timeShareSubmit.setBackgroundResource(android.R.drawable.btn_default);
                                    });
                                }
                            });
                        }).exceptionally(e -> {
                            appExecutor.getMainThread().execute(() -> {
                               // timeShareSubmit.setBackgroundResource(android.R.drawable.btn_default);
                                Toast.makeText(getApplicationContext(), "Insertion Failed", Toast.LENGTH_LONG).show();
                                btnSubmit.setEnabled(true);

                            });
                            return null;

                        }).join());


                }
                else
                {
                    Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
                    btnSubmit.setEnabled(true);

//                        progressBar.setVisibility(View.INVISIBLE);
                }


        });

        btnCancel.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), TTSMainActivity.class);
            startActivity(i);
            finish();

        });

//  single click view Date and time pickers
        date.setFocusable(false);
        startTime.setFocusable(false);
        endTime.setFocusable(false);

            date.setOnClickListener(v -> {
                MaterialDatePicker<Long> datePicker = MaterialDatePicker
                        .Builder
                        .datePicker()
                        .setTitleText("Select Date")
                        .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                        .build();

                datePicker.show(getSupportFragmentManager(),"Date_Picker");
                datePicker.addOnPositiveButtonClickListener(selection -> {
                    Log.e("Date",""+datePicker.getHeaderText());
                    LocalDate selectedDate = Instant
                            .ofEpochMilli(selection)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

                    DateTimeFormatter df =  DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    String formattedDate = selectedDate.format(df);
                    date.setText(formattedDate);
                });
            });


            startTime.setOnClickListener(view -> {
                MaterialTimePicker timePicker = new MaterialTimePicker
                        .Builder()
                        .setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD)
                        .setTimeFormat(TimeFormat.CLOCK_12H)
                        .setHour(12)
                        .setMinute(10)
                        .setTitleText("Select Start Time")
                        .build();

                timePicker.show(getSupportFragmentManager(),"Time_Picker");

                timePicker.addOnPositiveButtonClickListener(selection -> {
                    int hour = timePicker.getHour();
                    int minute = timePicker.getMinute();
                    String amPm = (hour < 12) ? "AM" : "PM";
                    int formattedHour = (hour == 0 || hour == 12) ? 12 : hour % 12;
                    String formattedTime = String.format("%02d:%02d %s", formattedHour, minute, amPm);
                    startTime.setText(formattedTime);
                });

            });

            endTime.setOnClickListener(view -> {
                MaterialTimePicker timePicker = new MaterialTimePicker
                        .Builder()
                        .setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD)
                        .setTimeFormat(TimeFormat.CLOCK_12H)
                        .setHour(12)
                        .setMinute(10)
                        .setTitleText("Select End Time")
                        .build();

                timePicker.show(getSupportFragmentManager(),"Time_Picker");

                timePicker.addOnPositiveButtonClickListener(selection -> {
                    int hour = timePicker.getHour();
                    int minute = timePicker.getMinute();
                    String amPm = (hour < 12) ? "AM" : "PM";
                    int formattedHour = (hour == 0 || hour == 12) ? 12 : hour % 12;
                    String formattedTime = String.format("%02d:%02d %s", formattedHour, minute, amPm);
                    endTime.setText(formattedTime);
                });

            });

            endTime.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    try
                    {
                        if (timeDifference() != null)
                        {
                            if (timeDifference().contains("-")) {
                                Toast.makeText(getApplicationContext(), "Please Enter Valid End Time", Toast.LENGTH_LONG).show();
                            }
                        }
                    }catch (Exception e){e.printStackTrace();}
                }
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                public void onTextChanged(CharSequence s, int start, int before, int count) { }
                });

    }

    @Override
    public void onBackPressed()
    {
        finish();
    }

    private void clearAll(){
        date.setText("");
        startTime.setText("");
        endTime.setText("");
        description.setText("");
        measurables.removeAll(measurables );
        listView.setAdapter(new MeasurableListCustomAdapter(measurables,getApplicationContext()));
    }

    // Add leading 0 when input date or time is single No like 5
    public String convertDateTime(int input) {
        if (input >= 10) {
            return String.valueOf(input);
        } else {
            return "0" + String.valueOf(input);
        }
    }

    // Clear the EditText of Measurable
    public void clear() {
        timeShareMeasurableQty.setText("");
        timeShareMeasurableUnit.setText("");
    }

    //Validation Start
    private String isDateValid()
    {
        String datets = date.getText().toString().trim().replaceAll("\\s+","").replace("/","-");
        if(datets.isEmpty()) { date.setError("Date Cannot Be Empty"); }
        return datets;
    }


    private String isActivityNameValid()
    {
        String actName = activityName.getText().toString().trim();
        return actName;
    }


    private String isTaskNameValid()
    {
        String tskName = taskName.getText().toString().trim();
        return tskName;
    }

    private String isProjectCodeValid()
    {
        String projectCode = projCode.getText().toString().trim();
        return projectCode;
    }

    private String isProjectNameValid()
    {
        String projectName = projName.getText().toString().trim();
        return projectName;
    }

    private String isStartTimeValid()
    {
        String strtTime= startTime.getText().toString().trim().replaceAll("\\s+","");
        if(strtTime.isEmpty()) { startTime.setError("Start Time Cannot Be Empty"); }
        return strtTime;
    }

    private String isEndTimeValid()
    {
        String edTime= endTime.getText().toString().trim().replaceAll("\\s+","");
        if(edTime.isEmpty()) { endTime.setError("End Time Cannot Be Empty"); }
        return edTime;
    }

    private String isDescriptionValid()
    {
        String descrip= description.getText().toString().trim();

        return descrip;
    }
    //Validation End

    //Getting Current TimeStamp
    private String delegationTime()
    {
        return DateConverter.getCurrentDateTime();
    }

    //Calculate Time Difference between startTime and endTime
    private String timeDifference()
    {
        String start= startTime.getText().toString().trim().replaceAll("\\s+","");
        String end= endTime.getText().toString().trim().replaceAll("\\s+","");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mma");
        LocalTime startTime = LocalTime.parse(start,formatter);
        LocalTime endTime = LocalTime.parse(end,formatter);
        long difference = ChronoUnit.MINUTES.between(startTime, endTime);
        int hours = (int) (difference/ 60);
        int mins = (int) (difference % 60);
        String timeConsumed = hours + " hr : "+mins+" mins";
        return timeConsumed;
    }

    //Calculate Actual Total Time
    private String totalTime() throws ExecutionException, InterruptedException {
        String oldActualTotalTime = getActualTotatTime(processingDelegationTaskId).get();
        String timeDifference =timeDifference();
        String newActualTotalTime = null;
            if(oldActualTotalTime.equals("NO_TIME")) { return timeDifference; }
            else {
                SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                format.setTimeZone(TimeZone.getTimeZone("GMT"));

                Date d1;
                Date d2;

                try {
                    d1 = format.parse(oldActualTotalTime);
                    d2 = format.parse(timeDifference);

                    //in milliseconds
                    long addtionTime = d2.getTime() + d1.getTime();

//            long diffSeconds = diff / 1000 % 60;
                    long diffMinutes = addtionTime / (60 * 1000) % 60;
                    long diffHours = addtionTime / (60 * 60 * 1000) % 24;
//            long diffDays = diff / (24 * 60 * 60 * 1000);
                    newActualTotalTime = diffHours + ":" + diffMinutes;

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return newActualTotalTime;
            }
    }

    public CompletableFuture<ArrayList<MeasurableListDataModel>> getAllocatedMeasurableList(Long taskId){
        CompletableFuture<ArrayList<MeasurableListDataModel>> future = new CompletableFuture<>();
        Call<ResponseBody> call = measurableService.getAllocatedMeasurableList(taskId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    APIResponse<ResponseBody> apiResponse = APIResponse.create(response);
                    if(apiResponse instanceof APISuccessResponse){
                        JsonElement bodyContent = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody();
                        Gson gson = new Gson();
                        Type measurablesType = new TypeToken<ArrayList<MeasurableListDataModel>>(){}.getType();
                        if(bodyContent.isJsonArray()){
                            JsonArray content = bodyContent.getAsJsonArray();
                            ArrayList<MeasurableListDataModel> measurables = gson.fromJson(content,measurablesType);
                            future.complete(measurables);
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

        return future;
    }


    public CompletableFuture<ArrayList<String>> addTimeShare(TimeShareDTO timeShareDTO){
        CompletableFuture<ArrayList<String>> future = new CompletableFuture<>();
        //it is used for to message & dts Id from ResponseBody object
        ArrayList<String> messageAndId = new ArrayList<>();
        appExecutor.getNetworkIO().execute(() -> {
            Call<ResponseBody> call = timeShareService.addTimeShare(timeShareDTO);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    try {
                        APIResponse apiResponse = APIResponse.create(response);
                        if (apiResponse != null) {
                            if (apiResponse instanceof APISuccessResponse) {
                                String message = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getMessage().getAsString();
                                JsonObject dtsobject = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody().getAsJsonObject();
                                String dtsId = dtsobject.get("id").getAsString();
                                if ("successful".equals(message)) {
                                    messageAndId.add(message);
                                    messageAndId.add(dtsId);
                                    future.complete(messageAndId);
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
                            future.completeExceptionally(new Throwable("Exception occured while adding a timeshare due to " + e.getMessage()));
                        }
                    catch (RuntimeException e) {
                            future.completeExceptionally(new Throwable("Unnoticed Exception occurred which is "+ e.getMessage() +   " its cause "+e.getCause()));
                        }

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    future.completeExceptionally(t);
                }
            });
        });

        return future;
    }

    public CompletableFuture<Boolean> addTimeShareMeasurables(Long timeShareId, List<MeasurableListDataModel> measurableListDataModel){
        CompletableFuture<Boolean> future  = new CompletableFuture<>();
        if(measurableListDataModel.isEmpty()) {
            future.complete(false);
            return future;
        }
        AtomicInteger pendingTasks = new AtomicInteger(measurableListDataModel.size());
        AtomicBoolean allSuccessful = new AtomicBoolean(true);
        for (MeasurableListDataModel m: measurableListDataModel
        ) {

            appExecutor.getNetworkIO().execute(() -> {
                Long measurableId = Long.valueOf(m.getId().split("\\.")[0]);
                Long measurableQty =  Long.valueOf(m.getMeasurableQty());
                String measurableUnit =  String.valueOf(m.getMeasurableUnit());
                Call<ResponseBody> call = measurableService.addTimeShareMeasurable(timeShareId,measurableId, measurableQty,measurableUnit);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Log.d("Debug", "Response received for addDailyTimeShareMeasurables"+response.body());
                        APIResponse apiResponse = null;
                        try {
                            apiResponse = APIResponse.create(response);
                            if (apiResponse != null) {
                                if (apiResponse instanceof APISuccessResponse) {
                                    String message = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getMessage().getAsString();
                                    Log.d("Debug", "Message from API: " + message);
                                    if (!"successful".equals(message)) {
                                        Log.d("Debug", "Measurable completed with success");
                                        allSuccessful.set(false);
                                    }
                                }else {
                                    Log.e("Result", "apiresponse is nul");
                                    allSuccessful.set(false);

                                }

                                if (apiResponse instanceof APIErrorResponse) {
                                    allSuccessful.set(false);
                                    String erMsg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                                    future.completeExceptionally(new Throwable(erMsg));

                                }
                                if (apiResponse instanceof APIErrorResponse) {
                                    allSuccessful.set(false);
                                    future.completeExceptionally(new Throwable("empty response"));
                                }
                            }
                    }
                    catch (ClassCastException e){
                        allSuccessful.set(false);
                        future.completeExceptionally(new Throwable("Unable to cast the response into required format due to "+ e.getMessage()));
                    }
                    catch (IOException e) {
                        Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                        allSuccessful.set(false);
                        future.completeExceptionally(new Throwable("Exception occured while performing adding measurables due to" + e.getMessage()));
                    }
                    catch (RuntimeException e) {
                        future.completeExceptionally(new Throwable("Unnoticed Exception occurred which is "+ e.getMessage() +   " its cause "+e.getCause()));
                    }

                        finally {
                            if (pendingTasks.decrementAndGet() == 0){
                                future.complete(allSuccessful.get());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                        allSuccessful.set(false);
                        if (pendingTasks.decrementAndGet() == 0) {
                            future.completeExceptionally(new Throwable(t.getMessage()));
                        }
                    }
                });
            });
        }
        return future;
    }

    //Getting Actual Total Time

    public CompletableFuture<String> getActualTotatTime(Long AssignedTaskId){
            CompletableFuture<String> future = new CompletableFuture<>();
            Call<ResponseBody> call = taskHandlerService.getActualTotalTime(AssignedTaskId);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        APIResponse<ResponseBody> apiResponse = APIResponse.create(response);
                        if (apiResponse instanceof  APISuccessResponse){
                            String actualTotalTime = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody().getAsString();
                            future.complete(actualTotalTime);
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
                    future.completeExceptionally(new Throwable("Exception occured while updating actual total time due to" + e.getMessage()));
                }
                    catch (RuntimeException e) {
                    future.completeExceptionally(new Throwable("Unnoticed Exception occurred which is "+ e.getMessage() +   " its cause "+e.getCause()));
                }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
            return future;
    }


    public CompletableFuture<String> updateActualTotatTime(Long assignedTaskId,String newActualTotalTime){
        CompletableFuture<String> future = new CompletableFuture<>();
        Call<ResponseBody> call = taskHandlerService.updateActualTotalTime(assignedTaskId,newActualTotalTime);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    APIResponse<ResponseBody> apiResponse = APIResponse.create(response);
                    if (apiResponse instanceof  APISuccessResponse){
                        String msg = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getMessage().getAsString();
                        future.complete(msg);
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
                    future.completeExceptionally(new Throwable("Exception occured while performing updateActualTotalTime due to" + e.getMessage()));
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
        return future;
    }
}
