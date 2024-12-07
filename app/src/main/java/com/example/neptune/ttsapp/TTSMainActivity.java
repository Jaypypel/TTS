package com.example.neptune.ttsapp;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.neptune.ttsapp.DTO.DailyTimeShareDTO;
import com.example.neptune.ttsapp.DTO.DailyTimeShareMeasurable;
import com.example.neptune.ttsapp.Network.APIEmptyResponse;
import com.example.neptune.ttsapp.Network.APIErrorResponse;
import com.example.neptune.ttsapp.Network.APIResponse;
import com.example.neptune.ttsapp.Network.APISuccessResponse;
import com.example.neptune.ttsapp.Network.ActivityServiceInterface;
import com.example.neptune.ttsapp.Network.DTSMeasurableInterface;
import com.example.neptune.ttsapp.Network.DailyTimeShareInterface;
import com.example.neptune.ttsapp.Network.MeasurableServiceInterface;
import com.example.neptune.ttsapp.Network.ProjectServiceInterface;
import com.example.neptune.ttsapp.Network.ResponseBody;
import com.example.neptune.ttsapp.Network.TaskServiceInterface;
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
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.security.auth.login.LoginException;


import dagger.hilt.android.AndroidEntryPoint;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


@AndroidEntryPoint
public class TTSMainActivity extends AppCompatActivity {

    @Inject
    AppExecutors appExecutor;

    @Inject
    DailyTimeShareInterface dailyTimeShareInterface;

    @Inject
    ActivityServiceInterface activityService;

    @Inject
    ProjectServiceInterface projectService;

    @Inject
    TaskServiceInterface taskService;

    @Inject
    MeasurableServiceInterface measurableService;

    @Inject
    DTSMeasurableInterface dtsMeasurableInterface;




    DailyTimeShareDTO dailyTimeShareDTO;



    private String[] mNavigationDrawerItemTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    Toolbar toolbar;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    ActionBarDrawerToggle mDrawerToggle;
    private SessionManager sessionManager;


    private EditText timeShareDate,timeShareStartTime,timeShareEndTime,timeShareDescription,timeShareMeasurableQty;
    private TextView time,date,timeShareProjCode;
    private AutoCompleteTextView timeShareActivityName,timeShareTaskName,timeShareProjName, timeShareMeasurableUnit;
    private Button timeShareCancel,timeShareSubmit,timeShareAddMeasurable;
    private Spinner timeShareMeasurable;

    ArrayList<MeasurableListDataModel> measurableListDataModels = new ArrayList<>();


    private ListView listView;
    private MeasurableListCustomAdapter measurableListCustomAdapter;


    private int mYear, mMonth, mDay, mHour, mMinute;



    // Code for Finishing activity from TimeShareList Activity
    public static TTSMainActivity mainActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // Code for Finishing activity from TimeShareList Activity
        mainActivity = this;

        setContentView(R.layout.activity_ttsmain);

        mTitle = mDrawerTitle = getTitle();
        mNavigationDrawerItemTitles= getResources().getStringArray(R.array.navigation_drawer_items_array);
        mDrawerLayout =  findViewById(R.id.drawer_layout);
        mDrawerList =  findViewById(R.id.left_drawer);
        sessionManager = new SessionManager(getApplicationContext());
        timeShareDate=findViewById(R.id.editTextMainDate);
        timeShareProjCode=findViewById(R.id.editTextMainProjNo);
        timeShareProjName=findViewById(R.id.editTextMainProjName);
        timeShareActivityName=findViewById(R.id.editTextMainActName);
        timeShareTaskName=findViewById(R.id.editTextMainTaskName);
        timeShareStartTime=findViewById(R.id.editTextMainStartTime);
        timeShareEndTime=findViewById(R.id.editTextMainEndTime);
        timeShareDescription=findViewById(R.id.editTextMainDescription);


        timeShareCancel=findViewById(R.id.buttonMainCancel);
        timeShareSubmit=findViewById(R.id.buttonMainSubmit);

        date = findViewById(R.id.textViewMainDate);
        time = findViewById(R.id.textViewMainTime);

        appExecutor.getMainThread().execute(()-> {
            date.setText(DateConverter.currentDate());
            time.setText(DateConverter.currentTime());
        });



        // Code for Measurable list
        listView=findViewById(R.id.listMainMeasurable);
        timeShareAddMeasurable=findViewById(R.id.buttonMainMeasurableAdd);
        timeShareMeasurableQty=findViewById(R.id.editTextMainQty);
        timeShareMeasurableUnit= findViewById(R.id.editTextMainUnit);
        timeShareMeasurable =  findViewById(R.id.measurableSelect);



        // code for go to Next EditText when press button of DONE on keyboard

        timeShareTaskName.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
            { timeShareDescription.requestFocus(); }
            return false;
        });

        timeShareDescription.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
            { timeShareProjName.requestFocus(); }
            return false;
        });

        timeShareProjName.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
            { timeShareActivityName.requestFocus(); return true;}
            return false;
        });

        timeShareActivityName.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
            { timeShareMeasurable.requestFocus(); }
            return false;
        });

        timeShareMeasurableQty.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
            { timeShareMeasurableUnit.requestFocus(); }
            return false;
        });
        CompletableFuture<List<MeasurableListDataModel>> measurableList  = getMeasurableListAndUpdateUi();
        //Code For set measurable list to spinner
        if (InternetConnectivity.isConnected())
        {
//            appExecutor.getMainThread().execute(() -> {
//                timeShareDate.setText("");
//                timeShareStartTime.setText("");
//                timeShareEndTime.setText("");
//                timeShareDescription.setText("");
//                timeShareMeasurableQty.setText("");
//                timeShareProjCode.setText("");
//                timeShareActivityName.setText("");
//                timeShareTaskName.setText("");
//                timeShareProjName.setText("");
//                timeShareMeasurableUnit.setText("");
//            });

            appExecutor.getNetworkIO().execute(() -> {
                getActivityNames().thenAccept(activityNames -> {
                    appExecutor.getMainThread().execute(() -> {
                        ArrayAdapter<String> activityNamesAdapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_list_item_1,activityNames);
                        timeShareActivityName.setAdapter(activityNamesAdapter);
                    });
                }).exceptionally(e -> {
                    appExecutor.getMainThread().execute(() ->
                            Toast.makeText(getApplicationContext(), "couldn't fetch activity names ", Toast.LENGTH_LONG).show());
                    return null;
                });
            });

            appExecutor.getNetworkIO().execute(() -> {
                getTaskNames().thenAccept(taskNames -> {
                    appExecutor.getMainThread().execute( () -> {
                        ArrayAdapter<String> taskNamesAdapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_list_item_1,taskNames);
                        timeShareActivityName.setAdapter(taskNamesAdapter);
                    });
                }).exceptionally(e -> {
                    appExecutor.getMainThread().execute(() ->
                            Toast.makeText(getApplicationContext(), "couldn't fetch task names ", Toast.LENGTH_LONG).show());
                    return null;
                });
            });
            appExecutor.getNetworkIO().execute(() -> {
                getProjectNames().thenAccept(projectNames -> {
                    appExecutor.getMainThread().execute(() -> {
                        ArrayAdapter<String> projectNamesAdapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_list_item_1, projectNames);
                        timeShareActivityName.setAdapter(projectNamesAdapter);
                    });
                }).exceptionally(e -> {
                    Log.e("Error", "Failed to fetch measurable list: " + e.getMessage());
                    appExecutor.getMainThread().execute(() ->
                            Toast.makeText(getApplicationContext(), "couldn't fetch project names", Toast.LENGTH_LONG).show());
                    return null;
                });
            });


            appExecutor.getNetworkIO().execute(() -> {
                getMeasurableListAndUpdateUi().thenAccept(measurableListDataModels1 -> {
                    appExecutor.getMainThread().execute(() -> {
                        ArrayAdapter<MeasurableListDataModel> adapterMeasurable = new ArrayAdapter<MeasurableListDataModel>(getBaseContext(), android.R.layout.simple_spinner_item, measurableListDataModels1);
                        adapterMeasurable.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        timeShareMeasurable.setAdapter(adapterMeasurable);
                    });
                }).exceptionally(e ->{    Log.e("Error", "Failed to fetch measurable list: " + e.getMessage());
                    appExecutor.getMainThread().execute(() ->
                            Toast.makeText(getApplicationContext(), "couldn't fetch measurable list", Toast.LENGTH_LONG).show());
                    return null;
                });
            });



        }else {Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}

        timeShareMeasurable.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                timeShareMeasurable.performClick(); // Force open
            }
            return false;
        });

        timeShareAddMeasurable.setOnClickListener(v -> {

            try {
                String tmeShrMsrble =  timeShareMeasurable.getSelectedItem().toString() != null ? timeShareMeasurable.getSelectedItem().toString(): "undefined";
                String tmeShreMsrbleQty = timeShareMeasurableQty.getText().toString();

                String tmeShreMsrblUnit = timeShareMeasurableUnit.getText().toString();

                String[]   parts = tmeShrMsrble.split("-");
                Log.e("parts",""+parts);

                String numberPart = parts[0].split("\\.")[0]; // Cast to int to remove decimal
                Log.e("numberPart",""+numberPart);
                // Extract the word part
                String wordPart = parts[1];
                Log.e("wordPart",""+wordPart);

                MeasurableListDataModel m = new MeasurableListDataModel();
                m.setId(numberPart);
                m.setMeasurableName(wordPart);
                m.setMeasurableQty(tmeShreMsrbleQty);
                m.setMeasurableUnit(tmeShreMsrblUnit);

                measurableListDataModels.add(m);
                measurableListCustomAdapter = new MeasurableListCustomAdapter(measurableListDataModels, getApplicationContext());
                listView.setAdapter(measurableListCustomAdapter);
                clear();
            }
            catch (Exception e){e.printStackTrace();}

        });

        timeShareSubmit.setOnClickListener(v -> {
            try {
                if (InternetConnectivity.isConnected())
                {

                    if (isDateValid().isEmpty()) {
                        timeShareDate.setError("Date Cannot Be Empty");
                        return;
                    }
                    if (isStartTimeValid().equals(isEndTimeValid())){
                       Snackbar snackbar = Snackbar.make(v, "Warning! Start and End Time are the same", Snackbar.LENGTH_LONG);
                        View snackbarView = snackbar.getView();
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackbarView.getLayoutParams();
                        params.gravity = Gravity.CENTER;
                        snackbarView.setLayoutParams(params);
                        snackbar.show();
                        return;
                    }
                    if (Integer.valueOf(isStartTimeValid().split(":")[0]) < Integer.valueOf(isEndTimeValid().split(":")[0])){
                        Snackbar.make(v, "Warning! Start time should be greater than End Time", Snackbar.LENGTH_LONG)
                                .show();
                        return;
                    }
                    if (!(sessionManager.getUserID().equals("Prerna") || sessionManager.getUserID().equals("YoKo")) && !isDateValid().equals(getTodayDate()))
                            { Toast.makeText(getApplicationContext(), "Date Has Been Expired Contact to Admin", Toast.LENGTH_LONG).show();
                            return;}
                    if (isStartTimeValid().isEmpty()) { timeShareStartTime.setError("Start Time Cannot Be Empty");
                    return;
                    }
                    if (isEndTimeValid().isEmpty()) { timeShareEndTime.setError("End Time Cannot Be Empty"); return; }
                    if (isTaskNameValid().isEmpty()) { timeShareTaskName.setError("Task Name Cannot Be Empty"); return;}
                     if (isProjectNameValid().isEmpty()) { timeShareProjName.setError("Project Name Cannot Be Empty");return; }
               if (isActivityNameValid().isEmpty()) { timeShareActivityName.setError("Activity Name Cannot Be Empty");return; }
                    if (getConsumedTime().contains("-")) { Toast.makeText(getApplicationContext(), "Please Enter Valid End Time", Toast.LENGTH_LONG).show(); return; }


                    if (measurableListDataModels.isEmpty()){
                        Toast.makeText(getApplicationContext(), "Measurable list is empty", Toast.LENGTH_LONG).show();
                        timeShareSubmit.setBackgroundResource(android.R.drawable.btn_default);
                        return;
                    }
                    timeShareSubmit.setBackgroundColor(Color.GRAY);

                             String projectcode = !isProjectNameValid().isEmpty()? isProjectCodeValid(): "No code received";
                             Log.e("Project code", "printed"+projectcode);
                             User user = new User(sessionManager.getUserID());
                             Log.e("User" , "user name"+user);
                             Log.e("consumed Time",getConsumedTime());
                             DailyTimeShare dailyTimeShare = new DailyTimeShare(isDateValid(),projectcode
                                     ,isProjectNameValid(),isActivityNameValid(),
                                     isTaskNameValid(),isStartTimeValid(),isEndTimeValid(),getConsumedTime(),
                                     isDescriptionValid(),delegationTime(),user );
                                Log.e("Received User Id", "id "+sessionManager.getUserID());
                             dailyTimeShareDTO = new DailyTimeShareDTO(dailyTimeShare,measurableListDataModels);


                            appExecutor.getNetworkIO().execute(() -> {
                                addDailyTimeShare(dailyTimeShare).thenCompose(result -> {
                                    Long id = Long.valueOf(result.get(1));
                                    return addDailyTimeShareMeasurables(id,measurableListDataModels).thenAccept(finalResult -> {
                                        if (finalResult) {
                                            appExecutor.getMainThread().execute(() -> {
                                                Toast.makeText(getApplicationContext(), "Time Share Inserted", Toast.LENGTH_LONG).show();
                                                clearAll();
                                                clear();
                                                timeShareSubmit.setBackgroundResource(android.R.drawable.btn_default);
                                            });
                                        }
                                    });
                                }).exceptionally(e -> {
                                    appExecutor.getMainThread().execute(() -> {
                                        timeShareSubmit.setBackgroundResource(android.R.drawable.btn_default);
                                        Toast.makeText(getApplicationContext(), "Insertion Failed", Toast.LENGTH_LONG).show();
                                    });
                                    return null;

                                }).join();
                            });


                } else {
                    Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();

                }
            } catch (Exception e) { e.printStackTrace(); }
        });



        timeShareProjName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String projectName = timeShareProjName.getText().toString().trim();
                if (!projectName.isEmpty()) {
                    // Fetch the project code
                    getProjectCodeAndUpdateUi(projectName).thenAccept(projectCode -> {
                                runOnUiThread(() -> timeShareProjCode.setText(projectCode));

                    }
                        ).exceptionally(e -> {
                        // Handle error and log it
                        Log.e("Project Code Error", "Failed to fetch project code: " + e.getMessage());
                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Failed to fetch project code", Toast.LENGTH_LONG).show());
                        return null;
                    });
                } else {
                    // Handle case when project name is empty
                    timeShareProjName.setError("Project Name Cannot Be Empty");
                }
            }
        });


        timeShareCancel.setOnClickListener(v -> clearAll());



        //  single click view Date and time pickers
        timeShareDate.setFocusable(false);
        timeShareStartTime.setFocusable(false);
        timeShareEndTime.setFocusable(false);


//
        timeShareDate.setOnClickListener(v -> {
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
                Log.e("fD",formattedDate);
                Log.e("getCurrentDate", getTodayDate());
                Log.e("isDateValid", isDateValid());
                timeShareDate.setText(formattedDate);
            });
        });
        //Date Picker start
//        timeShareDate.setOnClickListener(v -> {
//
//            //To show current date in the datePicker
//            Calendar mcurrentDate=Calendar.getInstance();
//            mYear=mcurrentDate.get(Calendar.YEAR);
//            mMonth=mcurrentDate.get(Calendar.MONTH);
//            mDay=mcurrentDate.get(Calendar.DAY_OF_MONTH);
//
//            DatePickerDialog mDatePicker=new DatePickerDialog(TTSMainActivity.this, (view, year, month, dayOfMonth) ->
//            timeShareDate.setText(convertDateTime(dayOfMonth) + "/" + convertDateTime((month+1))  + "/" + year),mYear, mMonth, mDay);
//            mDatePicker.getDatePicker().setCalendarViewShown(false);
//            mDatePicker.setTitle("Select date");
//            mDatePicker.show();
//
//        });


        timeShareStartTime.setOnClickListener(view -> {
            MaterialTimePicker timePicker = new MaterialTimePicker
                    .Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(12)
                    .setMinute(10)
                    .setTitleText("Select Start Time")
                    .build();

            timePicker.show(getSupportFragmentManager(),"Time_Picker");

            timePicker.addOnPositiveButtonClickListener(selection -> {
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();

                // Determine AM/PM
                String amPm = (hour < 12) ? "AM" : "PM";

                // Convert to 12-hour format
                int formattedHour = (hour == 0 || hour == 12) ? 12 : hour % 12;

                // Create formatted time string
                String formattedTime = String.format("%02d:%02d %s", formattedHour, minute, amPm);

                Log.d("StartTime",formattedTime);
                timeShareStartTime.setText(formattedTime);
            });

        });


        // Time Picker for Start Time
//        timeShareStartTime.setOnClickListener(v -> {
//            final Calendar c = Calendar.getInstance();
//            mHour = c.get(Calendar.HOUR_OF_DAY);
//            mMinute = c.get(Calendar.MINUTE);
//
//            // Launch Time Picker Dialog
//            TimePickerDialog timePickerDialog = new TimePickerDialog(TTSMainActivity.this, (view, hourOfDay, minute) ->
//                    timeShareStartTime.setText(convertDateTime(hourOfDay) + ":" + convertDateTime(minute)), mHour, mMinute, true);
//            timePickerDialog.show();
//
//        });


        timeShareEndTime.setOnClickListener(view -> {
            MaterialTimePicker timePicker = new MaterialTimePicker
                    .Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(12)
                    .setMinute(10)
                    .setTitleText("Select End Time")
                    .build();

            timePicker.show(getSupportFragmentManager(),"Time_Picker");

            timePicker.addOnPositiveButtonClickListener(selection -> {
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();

                // Determine AM/PM
                String amPm = (hour < 12) ? "AM" : "PM";

                // Convert to 12-hour format
                int formattedHour = (hour == 0 || hour == 12) ? 12 : hour % 12;

                // Create formatted time string
                String formattedTime = String.format("%02d:%02d %s", formattedHour, minute, amPm);

                Log.d("StartTime",formattedTime);
                timeShareEndTime.setText(formattedTime);
            });

        });

//        // Time Picker for End Time
//        timeShareEndTime.setOnClickListener(v -> {
//            final Calendar c = Calendar.getInstance();
//            mHour = c.get(Calendar.HOUR_OF_DAY);
//            mMinute = c.get(Calendar.MINUTE);
//
//            // Launch Time Picker Dialog
//            TimePickerDialog timePickerDialog = new TimePickerDialog(TTSMainActivity.this,
//                    (view, hourOfDay, minute) -> timeShareEndTime.setText(convertDateTime(hourOfDay) + ":" + convertDateTime(minute)), mHour, mMinute, true);
//            timePickerDialog.show();
//        });


        timeShareEndTime.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s)
            {
                    try
                    {
                        if (getConsumedTime() != null)
                        {
                            if (getConsumedTime().contains("-"))
                            {
                                Toast.makeText(getApplicationContext(), "Please Enter Valid End Time", Toast.LENGTH_LONG).show();
                            }
                        }
                    } catch (Exception e) { e.printStackTrace(); }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            public void onTextChanged(CharSequence s, int start, int before, int count) { }
        });

        setupToolbar();

        DataModel[] drawerItem = new DataModel[12];

        drawerItem[0] = new DataModel("Daily Time Share");
        drawerItem[1] = new DataModel("DTS View");
        drawerItem[2] = new DataModel("Received Tasks");
        drawerItem[3] = new DataModel("Committed Tasks");
        drawerItem[4] = new DataModel("Approval Completion Tasks");
        drawerItem[5] = new DataModel("Work Done Status");
        drawerItem[6] = new DataModel("Assign Task");
        drawerItem[7] = new DataModel("Assigned Tasks");
        drawerItem[8] = new DataModel("Accepted Tasks");
        drawerItem[9] = new DataModel("Completed Tasks");
        drawerItem[10] = new DataModel("Modification Tasks");
        drawerItem[11] = new DataModel("Task Admin");

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);

        DrawerItemCustomAdapter adapter = new DrawerItemCustomAdapter(this, R.layout.list_view_item_row, drawerItem);
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        setupDrawerToggle();

    }



    private class DrawerItemClickListener implements ListView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            selectItem(position);
        }
    }




    private void selectItem(int position) {

        Fragment fragment = null;
        Activity adminActivity=null;
        Activity mainActivity = null;

        if (position!=0)
        {
             timeShareMeasurable.setVisibility(View.INVISIBLE);
             timeShareDate.setVisibility(View.INVISIBLE);
             timeShareStartTime.setVisibility(View.INVISIBLE);
             timeShareEndTime.setVisibility(View.INVISIBLE);
             timeShareDescription.setVisibility(View.INVISIBLE);
             timeShareMeasurableQty.setVisibility(View.INVISIBLE);
             timeShareProjCode.setVisibility(View.INVISIBLE);
             timeShareActivityName.setVisibility(View.INVISIBLE);
             timeShareTaskName.setVisibility(View.INVISIBLE);
             timeShareProjName.setVisibility(View.INVISIBLE);
             timeShareMeasurableUnit.setVisibility(View.INVISIBLE);
             timeShareCancel.setVisibility(View.INVISIBLE);
             timeShareSubmit.setVisibility(View.INVISIBLE);
             timeShareAddMeasurable.setVisibility(View.INVISIBLE);
             listView.setVisibility(View.INVISIBLE);
//            user.setVisibility(View.INVISIBLE);
             date.setVisibility(View.INVISIBLE);
             time.setVisibility(View.INVISIBLE);

        }

        switch (position) {
            case 0:
                mainActivity = new TTSMainActivity();
                break;
            case 1:
                fragment = new TTSDailyTimeShareListFragment();
                break;
            case 2:
                fragment = new TTSTaskAllocatedListFragment();
                break;
            case 3:
                fragment = new TTSTaskCommittedListFragment();
                break;
            case 4:
                fragment = new TTSTaskApprovalCompletionListFragment();
                break;
            case 5:
                fragment = new TTSTaskCountFragment();
                break;
            case 6:
                fragment = new TTSTaskDelegationFragment();
                break;
            case 7:
                fragment = new TTSTaskDelegatedListFragment();
                break;
            case 8:
                fragment = new TTSTaskAcceptedListFragment();
                break;
            case 9:
                fragment = new TTSTaskCompletedListFragment();
                break;
            case 10:
                fragment = new TTSTaskModificationListFragment();
                break;
            case 11:
                if (sessionManager.getUserID().equals("Prerna") || sessionManager.getUserID().equals("YoKo") || sessionManager.getUserID().equals("swar") || sessionManager.getUserID().equals("mangal"))
                { adminActivity = new TTSAdminActivity();}
                else {Toast.makeText(getApplicationContext(), "You Are Not Admin", Toast.LENGTH_LONG).show(); }
                break;


            default:
                break;
        }

        if (adminActivity!=null)
        {
            Intent i = new Intent(TTSMainActivity.this, adminActivity.getClass());
            startActivity(i);
            finish();
        }
        else if (mainActivity!=null){
            Intent i = new Intent(TTSMainActivity.this, mainActivity.getClass());
            startActivity(i);
            finish();
        }


        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            setTitle(mNavigationDrawerItemTitles[position]);
            mDrawerLayout.closeDrawer(mDrawerList);

        } else { Log.e("MainActivity", "Error in creating fragment"); }
    }

    // Add leading 0 when input date or time is single No like 5
    public String convertDateTime(int input)
    {
        if (input >= 10) { return String.valueOf(input); }
        else { return "0" + input; }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) { return true; }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    void setupToolbar(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    void setupDrawerToggle(){
        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,toolbar,R.string.app_name, R.string.app_name);
        //This is necessary to change the icon of the Drawer Toggle upon state change.
        mDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed()
    {

        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("EXIT")
                .setMessage(Html.fromHtml("<b>"+"Do You Want To Logged Out..?"+"</b>"))
                .setPositiveButton("Yes", (dialog, which) -> finish())
                .setNegativeButton("No", null)
                .show();
    }

    private String getTodayDate()
    {
     return DateConverter.currentDate();
    }

    //Validation Start
    private String isDateValid() {
        if(timeShareDate !=  null){
//            return timeShareDate.getText().toString().trim().replaceAll("\\s+","");
            return String.valueOf(timeShareDate.getText());
        }else
            return "time share date is not iniaitzed";
    }

    private String isActivityNameValid() {
        if(timeShareActivityName != null){
            return timeShareActivityName.getText().toString().trim();
        }
        return "not initaiztied";
    }

    private String isTaskNameValid() {
        if(timeShareTaskName != null){
        return timeShareTaskName.getText().toString().trim();
        }
        return "N.I";

    }

    private String isProjectCodeValid() { if (timeShareProjCode != null){
        return timeShareProjCode.getText().toString().trim();
     }else {
        return "N.I";
      }
    }

    private String isProjectNameValid() {
        if(timeShareProjName != null) return timeShareProjName.getText().toString().trim();
        else return "N.I";
    }

    private String isStartTimeValid() {
        if(timeShareStartTime !=null)return timeShareStartTime.getText().toString().trim();
        else return "N.I";
    }

    private String isEndTimeValid() {
        if(timeShareEndTime != null)return timeShareEndTime.getText().toString().trim();
        else return "N.I";
    }

    private String isDescriptionValid() {
        if(timeShareDescription  != null)return timeShareDescription.getText().toString().trim();
        else return "N.I";
    }
    //Validation End

    //Getting Current TimeStamp
    private String delegationTime()
    {
        return DateConverter.getCurrentDateTime();
    }


    // Clear All EditText
    public void clearAll()
    {
        timeShareDate.setText("");
        timeShareStartTime.setText("");
        timeShareEndTime.setText("");
        timeShareDescription.setText("");
        timeShareMeasurableQty.setText("");
        timeShareProjCode.setText("");
        timeShareActivityName.setText("");
        timeShareTaskName.setText("");
        timeShareProjName.setText("");
        listView.setAdapter(null);
        timeShareMeasurableUnit.setText("");
    }

    // Clear the EditText of Measurable
    public void clear(){
        timeShareMeasurableQty.setText("");
        timeShareMeasurableUnit.setText("");
    }

    //Calculate Time Difference between startTime and endTime
    private String getConsumedTime()
    {
        String start= timeShareStartTime!=null ? timeShareStartTime.getText().toString().trim().replaceAll("\\s+","") : "N.I";
        String end=timeShareEndTime !=null ? timeShareEndTime.getText().toString().trim().replaceAll("\\s+","") : "N.I" ;
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        format.setTimeZone(TimeZone.getTimeZone("IST"));

        String difference=null;

        if(!start.isEmpty() && !end.isEmpty()) {
            try {
                Date date1 = format.parse(start);
                Date date2 = format.parse(end);
                long mills = date2.getTime() - date1.getTime();
                int hours = (int) (mills / (1000 * 60 * 60));
                int mins = (int) (mills / (1000 * 60)) % 60;
                difference = convertDateTime(hours) + ":" + convertDateTime(mins);

            } catch (ParseException e) { e.printStackTrace(); }

        }
        return difference;
    }

    //Getting Maximum Id from TIME_SHARE Table
    public Long getMaxTimeShareTaskId() {
        long id=0;
        Connection con;
        ResultSet rs ;
        try {
            con=DatabaseHelper.getDBConnection();
            PreparedStatement ps = con.prepareStatement("select max(ID) maxnum from DAILY_TIME_SHARE");

            rs=ps.executeQuery();
            if (rs.next())
            {
                id=rs.getLong("maxnum");
                System.out.println(id);
                id++;
            }else {
                id++;
            }
            rs.close();
            ps.close();
            con.close();

        } catch (SQLException e) { e.printStackTrace(); }

        return id;
    }


    private CompletableFuture<ArrayList<String>> addDailyTimeShare(DailyTimeShare dailyTimeShare) {
    CompletableFuture<ArrayList<String>> future = new CompletableFuture<>();
    //it is used for to message & dts Id from ResponseBody object
    ArrayList<String> messageAndId = new ArrayList<>();
    appExecutor.getNetworkIO().execute(() -> {
        Call<ResponseBody> call = dailyTimeShareInterface.addDailyTimeShare(dailyTimeShare);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                APIResponse apiResponse = null;
                try {
                    apiResponse = APIResponse.create(response);
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
                    }
                } catch (IOException e) {
                    Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                    future.completeExceptionally(new Exception("API request failed: " + response.code()));
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

      public CompletableFuture<Boolean> addDailyTimeShareMeasurables(Long timeShareId,List<MeasurableListDataModel> measurableListDataModel){
            CompletableFuture<Boolean> future  = new CompletableFuture<>();
          if(measurableListDataModel.isEmpty()) {
              future.complete(false);
              return future;
          }
          AtomicInteger pendingTasks = new AtomicInteger(measurableListDataModels.size());
          AtomicBoolean allSuccessful = new AtomicBoolean(true);
            for (MeasurableListDataModel m: measurableListDataModel
               ) {

              appExecutor.getNetworkIO().execute(() -> {
                  Long measurableId = Long.valueOf(m.getId().split("\\.")[0]);
                  Long measurableQty =  Long.valueOf(m.getMeasurableQty());
                  String measurableUnit =  String.valueOf(m.getMeasurableUnit());
                  Call<ResponseBody> call = dtsMeasurableInterface.addDailyTimeShareMeasurable(timeShareId,measurableId, measurableQty,measurableUnit);
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
                              }
                          } catch (IOException e) {

                              Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                              allSuccessful.set(false);
                          }
                          finally {
                              if (pendingTasks.decrementAndGet() == 0){
                                  future.complete(allSuccessful.get());
                              }
                          }
                      }

                      @Override
                      public void onFailure(Call<ResponseBody> call, Throwable t) {
                          Log.e("IOException", "Exception occurred: " + t.getMessage(), t);
                          allSuccessful.set(false);
                          if (pendingTasks.decrementAndGet() == 0) {
                              future.complete(false);
                          }
                      }
                  });
              });
          }
          return future;
      }



    public CompletableFuture<ArrayList<String>> getProjectNames() {
        CompletableFuture<ArrayList<String>> future = new CompletableFuture();
//        AtomicArrayList<String> projectNames;
        Call<ResponseBody> call = projectService.getProjectNameList();
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
                            JsonArray projectNames = result.getAsJsonArray();
                            ArrayList<String> list = gson.fromJson(projectNames, listType);
                            future.complete(list);

                        }
                    }
                    if (apiResponse instanceof APIErrorResponse) {
                        String erMsg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                        Log.e("Error Received", "" + erMsg);
                    }
                    if (apiResponse instanceof APIErrorResponse) {
                        Log.e("Response", "is empty repsonse" + apiResponse);
                    }
                }
                catch (ClassCastException e) {
                    Log.e("Error", ""+e.getMessage());
                }
                catch (RuntimeException e) {
                    Log.e("Error", ""+e.getMessage());
                } catch (IOException e) {
                    Log.e("Error", ""+e.getMessage());
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Network Request", "Failed: " + t.getMessage());

            }
        });

        return future;
    }


    public CompletableFuture<ArrayList<String>> getActivityNames() {
        CompletableFuture<ArrayList<String>> future = new CompletableFuture<>();


            Call<ResponseBody> call = activityService.getActivitiesName();
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
                                 JsonArray activityNames = result.getAsJsonArray();
                                 ArrayList<String> list = gson.fromJson(activityNames, listType);
                                 future.complete(list);

                             }
                         }
                        if (apiResponse instanceof APIErrorResponse) {
                            String erMsg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                            Log.e("Error Received", "" + erMsg);
                        }
                        if (apiResponse instanceof APIErrorResponse) {
                            Log.e("Response", "is empty repsonse" + apiResponse);
                        }
                    }
                    catch (ClassCastException e) {
                        Log.e("Error", ""+e.getMessage());
                    }
                    catch (RuntimeException e) {
                        Log.e("Error", ""+e.getMessage());
                    } catch (IOException e) {
                        Log.e("Error", ""+e.getMessage());
                    }

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("Network Request", "Failed: " + t.getMessage());

                }
            });



        return future;
    }


    public CompletableFuture<ArrayList<String>> getTaskNames() {
        CompletableFuture<ArrayList<String>> future = new CompletableFuture<>();


        Call<ResponseBody> call = taskService.getTaskNames();
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
                                 JsonArray activityNames = result.getAsJsonArray();
                                 ArrayList<String> list = gson.fromJson(activityNames, listType);
                                 future.complete(list);

                             }
                         }
                        if (apiResponse instanceof APIErrorResponse) {
                            String erMsg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                            Log.e("Error Received", "" + erMsg);
                        }
                        if (apiResponse instanceof APIErrorResponse) {
                            Log.e("Response", "is empty repsonse" + apiResponse);
                        }
                    }
                    catch (ClassCastException e) {
                        Log.e("Error", ""+e.getMessage());
                    }
                    catch (RuntimeException e) {
                        Log.e("Error", ""+e.getMessage());
                    } catch (IOException e) {
                        Log.e("Error", ""+e.getMessage());
                    }

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("Network Request", "Failed: " + t.getMessage());

                }
            });



        return future;
    }



    public CompletableFuture<String> getProjectCodeAndUpdateUi(String projectName) {
        CompletableFuture<String> future = new CompletableFuture<>();
        appExecutor.getNetworkIO().execute(() -> {

            Call<ResponseBody> projectCodeResponse = projectService.getProjectCodeViaProjectName(projectName);
            projectCodeResponse.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    APIResponse apiResponse = null;
                    try {
                        apiResponse = APIResponse.create(response);
                        if(apiResponse instanceof APISuccessResponse) {
                            JsonElement jsonElement = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody();
                            String projectCode = jsonElement != null ? jsonElement.getAsString(): "";

                             future.complete(projectCode);  Log.e("ProjectCode",projectCode);
                        }
                        if (apiResponse instanceof APIErrorResponse) {
                            String erMsg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                            Log.e("Error Received", "" + erMsg);
                        }
                        if (apiResponse instanceof APIErrorResponse) {
                            Log.e("Response", "is empty repsonse" + apiResponse);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }


                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    future.completeExceptionally(t);

                }
            });
        } );
        return future;
    }



    public CompletableFuture<List<MeasurableListDataModel>> getMeasurableListAndUpdateUi() {
        CompletableFuture<List<MeasurableListDataModel>> future = new CompletableFuture<>();
        appExecutor.getNetworkIO().execute(() -> {
            Call<ResponseBody> measurableListResponse = measurableService.getMeasurableList();
            measurableListResponse.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    APIResponse apiResponse;
                    try {

                        apiResponse = APIResponse.create(response);
                        if (apiResponse instanceof APISuccessResponse) {
                            JsonElement responseElement =((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody();
                            if (responseElement.isJsonArray()) {
                                JsonArray responseBody = responseElement.getAsJsonArray();
                                ArrayList<MeasurableListDataModel> measurableListDataModels = new ArrayList<>();
                                for (JsonElement element : responseBody) {
                                    JsonObject obj = element.getAsJsonObject();
                                    double id = obj.get("id").getAsDouble();
                                    String measurableName = obj.get("measurableName").getAsString();
                                    MeasurableListDataModel m = new MeasurableListDataModel();
                                    m.setId(String.valueOf(id));
                                    m.setMeasurableName(measurableName);
                                    measurableListDataModels.add(m);
                                }
                                future.complete(measurableListDataModels);
                                Log.d("Meaurable List", "list" + measurableListDataModels);



                            }
                        }
                        if (apiResponse instanceof APIErrorResponse) {
                            String erMsg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                            Log.e("Error Received", "" + erMsg);
                        }
                        if (apiResponse instanceof APIErrorResponse) {
                            Log.e("Response", "is empty repsonse" + apiResponse);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }


                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("Network Request", "Failed: " + t.getMessage()+"  "+t.getStackTrace());

                }
            });
        });
            return future;
    }

}
