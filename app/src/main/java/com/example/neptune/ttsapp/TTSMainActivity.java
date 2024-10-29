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
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.neptune.ttsapp.Network.DailyTimeShareInterface;
import com.example.neptune.ttsapp.Network.JSONConfig;
import com.example.neptune.ttsapp.Network.MeasurableServiceInterface;
import com.example.neptune.ttsapp.Network.ProjectServiceInterface;
import com.example.neptune.ttsapp.Network.ResponseBody;
import com.example.neptune.ttsapp.Network.TaskServiceInterface;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import hilt_aggregated_deps._dagger_hilt_android_internal_lifecycle_HiltWrapper_HiltViewModelFactory_ActivityCreatorEntryPoint;
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
    ActivityServiceInterface activityInterface;

    @Inject
    ProjectServiceInterface projectServiceInterface;

    @Inject
    TaskServiceInterface taskServiceInterface;

    @Inject
    MeasurableServiceInterface measurableServiceInterface;



    ArrayList<DailyTimeShareMeasurable> dailyTimeShareMeasurableList = new ArrayList<>();
    DailyTimeShareDTO dailyTimeShareDTO;



    private String[] mNavigationDrawerItemTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    Toolbar toolbar;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    ActionBarDrawerToggle mDrawerToggle;
    private SessionManager sessionManager;

//    private Spinner spinnerMeasurable;
    private EditText timeShareDate,timeShareStartTime,timeShareEndTime,timeShareDescription,timeShareMeasurableQty;
    private TextView time,date,timeShareProjCode;
    private AutoCompleteTextView timeShareActivityName,timeShareTaskName,timeShareProjName, timeShareMeasurableUnit;
    private Button timeShareCancel,timeShareSubmit,timeShareAddMeasurable;
    private Spinner timeShareMeasurable;

    ArrayList<MeasurableListDataModel> measurableListDataModels = new ArrayList<>();
    private ListView listView;
    private MeasurableListCustomAdapter measurableListCustomAdapter;


    private int mYear, mMonth, mDay, mHour, mMinute;

//    private ProgressBar progressBar;

    // Code for Finishing activity from TimeShareList Activity
    public static TTSMainActivity mainActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // Code for Finishing activity from TimeShareList Activity
        mainActivity = this;

        setContentView(R.layout.activity_ttsmain);
//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy);

        mTitle = mDrawerTitle = getTitle();
        mNavigationDrawerItemTitles= getResources().getStringArray(R.array.navigation_drawer_items_array);
        mDrawerLayout =  findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        sessionManager = new SessionManager(getApplicationContext());


//        user = (TextView) findViewById(R.id.textViewMainUser);
//        user.setText(sessionManager.getUserID());
        timeShareDate=(EditText)findViewById(R.id.editTextMainDate);

        timeShareProjCode=(TextView)findViewById(R.id.editTextMainProjNo);
        timeShareProjName=(AutoCompleteTextView)findViewById(R.id.editTextMainProjName);
        timeShareActivityName=(AutoCompleteTextView)findViewById(R.id.editTextMainActName);
        timeShareTaskName=(AutoCompleteTextView)findViewById(R.id.editTextMainTaskName);
        timeShareStartTime=(EditText)findViewById(R.id.editTextMainStartTime);
        timeShareEndTime=(EditText)findViewById(R.id.editTextMainEndTime);
        timeShareDescription=(EditText)findViewById(R.id.editTextMainDescription);
//        btnAddDesSteps = (Button)findViewById(R.id.buttonMainDesSteps);

        timeShareCancel=(Button)findViewById(R.id.buttonMainCancel);
        timeShareSubmit=(Button)findViewById(R.id.buttonMainSubmit);

        date = (TextView) findViewById(R.id.textViewMainDate);
        time = (TextView) findViewById(R.id.textViewMainTime);

        final Handler someHandler = new Handler(getMainLooper());
        someHandler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {

                SimpleDateFormat DateFormatter = new SimpleDateFormat("dd/MM/yyyy");
                Date date1 = new Date();
                String currentDate = DateFormatter.format(date1);
                date.setText("Date :  " +currentDate);

                SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm a");
                Date time1 = new Date();
                String currentTime = timeFormatter.format(time1);
                time.setText("Time :  " +currentTime);
                someHandler.postDelayed(this, 1000);
            }
        }, 10);


        // Code for Measurable list
        listView=(ListView)findViewById(R.id.listMainMeasurable);
        timeShareAddMeasurable=(Button)findViewById(R.id.buttonMainMeasurableAdd);
        timeShareMeasurableQty=(EditText)findViewById(R.id.editTextMainQty);
        timeShareMeasurableUnit=(AutoCompleteTextView) findViewById(R.id.editTextMainUnit);
        timeShareMeasurable = (Spinner) findViewById(R.id.measurableSelect);



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

        //Code For set measurable list to spinner
        if (InternetConnectivity.isConnected())
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
            timeShareMeasurableUnit.setText("");


        /*    ArrayAdapter<String> activityNameAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, getActivityList());
            timeShareActivityName.setAdapter(activityNameAdapter);*/
//            ArrayAdapter<String> taskNameAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, getTaskList());
//            timeShareTaskName.setAdapter(taskNameAdapter);
//
//            ArrayAdapter<String> projectNameAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, getProjectNameList());
//            timeShareProjName.setAdapter(projectNameAdapter);
//
//            ArrayAdapter<MeasurableListDataModel> adapterMeasurable = new ArrayAdapter<MeasurableListDataModel>(getBaseContext(), android.R.layout.simple_spinner_item, getMeasurableList());
//            adapterMeasurable.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            timeShareMeasurable.setAdapter(adapterMeasurable);

//            ArrayAdapter<String> unitNameAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, getMeasurableUnit());
//            timeShareMeasurableUnit.setAdapter(unitNameAdapter);
            getActivityListAndUpdateUi();
            getTaskNameListAndUpdateUi();
            getProjectNameListAndUpdateUi();
            getMeasurableListAndUpdateUi().thenAccept(measurableListDataModels1 -> {
                ArrayAdapter<MeasurableListDataModel> adapterMeasurable = new ArrayAdapter<MeasurableListDataModel>(getBaseContext(), android.R.layout.simple_spinner_item, measurableListDataModels1);
                adapterMeasurable.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                timeShareMeasurable.setAdapter(adapterMeasurable);}).exceptionally(e -> {

                   Log.e("Error", "Failed to fetch measurable list: " + e.getMessage());
                appExecutor.getMainThread().execute(() ->
                              Toast.makeText(getApplicationContext(), "couldn't fetch measurable list", Toast.LENGTH_LONG).show());
                return null;
            });



        }else {Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}



        timeShareAddMeasurable.setOnClickListener(v -> {
            getMeasurableListAndUpdateUi().thenAccept(measurableListDataModels1 -> {
                ArrayAdapter<MeasurableListDataModel> adapterMeasurable = new ArrayAdapter<MeasurableListDataModel>(getBaseContext(), android.R.layout.simple_spinner_item, measurableListDataModels1);
                adapterMeasurable.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                timeShareMeasurable.setAdapter(adapterMeasurable);}).exceptionally(e -> {

                Log.e("Error", "Failed to fetch measurable list: " + e.getMessage());
                appExecutor.getMainThread().execute(() ->
                        Toast.makeText(getApplicationContext(), "couldn't fetch measurable list", Toast.LENGTH_LONG).show());
                return null;
            });

            try {
                String tmeShrMsrble =  timeShareMeasurable.getSelectedItem() != null ? timeShareMeasurable.getSelectedItem().toString(): "undefined";
                String tmeShreMsrbleQty = timeShareMeasurableQty.getText().toString() != null ? timeShareMeasurableQty.getText().toString(): "1";
                String tmeShreMsrblUnit = timeShareMeasurableUnit.getText().toString() != null ? timeShareMeasurableQty.getText().toString(): "Unit";

                measurableListDataModels.add(new MeasurableListDataModel(tmeShrMsrble,tmeShreMsrbleQty,tmeShreMsrblUnit));
                measurableListCustomAdapter = new MeasurableListCustomAdapter(measurableListDataModels, getApplicationContext());
//                dailyTimeShareMeasurableList.add(new DailyTimeShareMeasurable(Long.parseLong(timeShareMeasurableQty.getText().toString().replaceAll("[^0-9]"," ")),timeShareMeasurableUnit.getText().toString()));
                listView.setAdapter(measurableListCustomAdapter);
                clear();
                }
            catch (Exception e){e.printStackTrace();}

        });


        timeShareSubmit.setOnClickListener(v -> {
            try {
                if (InternetConnectivity.isConnected())
                {

                    if (isDateValid().isEmpty()) { timeShareDate.setError("Date Cannot Be Empty"); }
                    else if (!(sessionManager.getUserID().equals("Prerna") || sessionManager.getUserID().equals("YoKo")) && !isDateValid().equals(getTodayDate()))
                            { Toast.makeText(getApplicationContext(), "Date Has Been Expired Contact to Admin", Toast.LENGTH_LONG).show(); }
                    else if (isStartTimeValid().isEmpty()) { timeShareStartTime.setError("Start Time Cannot Be Empty"); }
                    else if (isEndTimeValid().isEmpty()) { timeShareEndTime.setError("End Time Cannot Be Empty"); }
                    else if (isTaskNameValid().isEmpty()) { timeShareTaskName.setError("Task Name Cannot Be Empty"); }
                    else if (isProjectNameValid().isEmpty()) { timeShareProjName.setError("Project Name Cannot Be Empty"); }
                    else if (isActivityNameValid().isEmpty()) { timeShareActivityName.setError("Activity Name Cannot Be Empty"); }
                    else if (getConsumedTime().contains("-")) { Toast.makeText(getApplicationContext(), "Please Enter Valid End Time", Toast.LENGTH_LONG).show(); }
                    else
                         {
                            timeShareSubmit.setBackgroundColor(Color.GRAY);
//                            Toast.makeText(getApplicationContext(), "Wait For Inserting TimeShare", Toast.LENGTH_LONG).show()
                             //getProjectCodeAndUpdateUi();

                             String projectcode = !isProjectNameValid().isEmpty()? isProjectCodeValid(): "No code received";
                             Log.e("Project code", "printed"+projectcode);
                             User user = new User(sessionManager.getUserID());
                             Log.e("User" , "user name"+user);

                             DailyTimeShare dailyTimeShare = new DailyTimeShare(isDateValid(),projectcode
                                     ,isProjectNameValid(),isActivityNameValid(),
                                     isTaskNameValid(),isStartTimeValid(),isEndTimeValid(),getConsumedTime(),
                                     isDescriptionValid(),delegationTime().toString(),user );
                                Log.e("Received User Id", "id "+sessionManager.getUserID());
//                            String result = insertDailyTimeShare(getMaxTimeShareTaskId(), sessionManager.getUserID(), isDateValid(), isProjectCodeValid(), isProjectNameValid(), isActivityNameValid(),
//                                    isTaskNameValid(), isStartTimeValid(), isEndTimeValid(), getConsumedTime(),isDescriptionValid(), delegationTime(), measurableListDataModels);
                             dailyTimeShareDTO = new DailyTimeShareDTO(dailyTimeShare,measurableListDataModels);
                             AtomicBoolean isRequestPass = addDailyTimeShare(dailyTimeShareDTO);
                            if (isRequestPass.get()) {
                                Toast.makeText(getApplicationContext(), "Time Share Inserted", Toast.LENGTH_LONG).show();
                                clearAll();
//                                timeShareSubmit.setBackgroundColor(Color.LTGRAY);
                                timeShareSubmit.setBackgroundResource(android.R.drawable.btn_default);
                            } else {
//                                timeShareSubmit.setBackgroundColor(Color.LTGRAY);
                                timeShareSubmit.setBackgroundResource(android.R.drawable.btn_default);
                                Toast.makeText(getApplicationContext(), "Insertion Failed", Toast.LENGTH_LONG).show();
                            }
                         }
                } else {
                    Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
//                        progressBar.setVisibility(View.INVISIBLE);
                }
            } catch (Exception e) { e.printStackTrace(); }
        });


        // Get Project Code Against Project Name And Set To Project Code TextView
/*        timeShareProjName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
            {
                String projectName = timeShareProjName.getText().toString().trim();
                if (projectName.length()>0) { timeShareProjCode.setText(getProjectCode(isProjectNameValid())); }
            }
        });*/

        timeShareProjName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String projectName = timeShareProjName.getText().toString().trim();
                if (!projectName.isEmpty()) {
                    // Fetch the project code
                    getProjectCodeAndUpdateUi(projectName).thenAccept(projectCode -> {
                                runOnUiThread(() -> timeShareProjCode.setText(projectCode));

//                        // Update the UI with the fetched project code
//                        if (projectCode instanceof APIResponse){
//                            Log.e("Project code",""+projectCode)
//                            String projectcode = ((APISuccessResponse<String>) projectCode).getBody().toString();
//                            timeShareProjCode.setText(projectCode.toString());
//                        }
//                        if (projectCode instanceof APIEmptyResponse) {
//                            Log.e("Empty Response ", "fetch empty response" );
//                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "empty response", Toast.LENGTH_LONG).show());
//                            // Handle empty response
//                        }
//                        if (projectCode instanceof APIErrorResponse) {
//                            String errorMessage = ((APIErrorResponse<String>) projectCode).getErrorMessage();
//
//                        if (projectCode instanceof APISuccessResponse) {
//                            Log.e("Project code", "" + projectCode);
//                            String code = ((APISuccessResponse<String>) projectCode).getBody();
//                            runOnUiThread(() -> timeShareProjCode.setText(code));  // Safely update the UI
//                        } else if (projectCode instanceof APIEmptyResponse) {
//                            Log.e("Empty Response", "Received empty response");
//                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "No content received", Toast.LENGTH_LONG).show());
//                        } else if (projectCode instanceof APIErrorResponse) {
//                            String errorMessage = ((APIErrorResponse<String>) projectCode).getErrorMessage();
//                            Log.e("Error Response", "Error message: " + errorMessage);
//                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Error: " + errorMessage, Toast.LENGTH_LONG).show());
//                        }
         //                }
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

        //Date Picker start
        timeShareDate.setOnClickListener(v -> {

            //To show current date in the datePicker
            Calendar mcurrentDate=Calendar.getInstance();
            mYear=mcurrentDate.get(Calendar.YEAR);
            mMonth=mcurrentDate.get(Calendar.MONTH);
            mDay=mcurrentDate.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog mDatePicker=new DatePickerDialog(TTSMainActivity.this, (view, year, month, dayOfMonth) ->
            timeShareDate.setText(convertDateTime(dayOfMonth) + "/" + convertDateTime((month+1))  + "/" + year),mYear, mMonth, mDay);
            mDatePicker.getDatePicker().setCalendarViewShown(false);
            mDatePicker.setTitle("Select date");
            mDatePicker.show();

        });





        // Time Picker for Start Time
        timeShareStartTime.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(TTSMainActivity.this, (view, hourOfDay, minute) ->
                    timeShareStartTime.setText(convertDateTime(hourOfDay) + ":" + convertDateTime(minute)), mHour, mMinute, true);
            timePickerDialog.show();

        });


        // Time Picker for End Time
        timeShareEndTime.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(TTSMainActivity.this,
                    (view, hourOfDay, minute) -> timeShareEndTime.setText(convertDateTime(hourOfDay) + ":" + convertDateTime(minute)), mHour, mMinute, true);
            timePickerDialog.show();
        });


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

//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        final View view = View.inflate(getApplicationContext(), R.layout.left_drawer, null);
//
//        if (position == mSelectedItem) {
//            // set your color
//        }
//
//        return view;
//    }


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
//            btnAddDesSteps.setVisibility(View.INVISIBLE);

//            progressBar.setVisibility(View.INVISIBLE);
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
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date1 = new Date();
        return formatter.format(date1);
    }

    //Validation Start
    private String isDateValid() {
        if(timeShareDate !=  null){
            return timeShareDate.getText().toString().trim().replaceAll("\\s+","");
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
        if(timeShareStartTime !=null)return timeShareStartTime.getText().toString().trim().replaceAll("\\s+","");
        else return "N.I";
    }

    private String isEndTimeValid() {
        if(timeShareEndTime != null)return timeShareEndTime.getText().toString().trim().replaceAll("\\s+","");
        else return "N.I";
    }

    private String isDescriptionValid() {
        if(timeShareDescription  != null)return timeShareDescription.getText().toString().trim();
        else return "N.I";
    }
    //Validation End

    //Getting Current TimeStamp
    private Timestamp delegationTime()
    {
        Calendar calendar = Calendar.getInstance();
        return new Timestamp(calendar.getTime().getTime());
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
        format.setTimeZone(TimeZone.getTimeZone("GMT"));

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


      private AtomicBoolean addDailyTimeShare(DailyTimeShareDTO dailyTimeShareDTO) {
          AtomicBoolean isResponseCompleted = new AtomicBoolean(false);
        appExecutor.getNetworkIO().execute(() -> {
            Call<APIResponse<Object>> addDailyTimeShareResponse = dailyTimeShareInterface.addDailyTimeShare(dailyTimeShareDTO);
            addDailyTimeShareResponse.enqueue(new Callback<APIResponse<Object>>() {
                @Override
                public void onResponse(Call<APIResponse<Object>> call, Response<APIResponse<Object>> response) {
                    if(response.isSuccessful() && response.body() != null && response.errorBody()!=null) {
                      isResponseCompleted.set(true);
                    }

                }

                @Override
                public void onFailure(Call<APIResponse<Object>> call, Throwable t) {
                    appExecutor.getMainThread().execute(() -> {
                        Log.e("Network Request", "Failed to add : " + t.getMessage());
                    });
                }
            });
        });
        return isResponseCompleted;
      }


    //Insert Time Share
//    public String insertDailyTimeShare(Long timeShareId,String userId,String timeShareDate,String timeShareProjCode,String timeShareProjName,
//                                       String timeShareActivityName,String timeShareTaskName,String timeShareStartTime,String timeShareEndTime,
//                                       String consumedTime, String description,Timestamp createdOn,ArrayList<MeasurableListDataModel> measurableList){
//        String result="false";
//        int x = 0;
//        Connection con;
//
//        try{
//            con = DatabaseHelper.getDBConnection();
//            PreparedStatement ps;
//
//            ps = con.prepareStatement("insert into DAILY_TIME_SHARE(ID,FK_AUTHENTICATION_USER_ID,DATE_OF_TIME_SHARE,PROJECT_CODE,PROJECT_NAME,ACTIVITY_NAME,TASK_NAME," +
//                                            "START_TIME,END_TIME,CONSUMED_TIME,DESCRIPTION,CREATED_ON) values(?,?,?,?,?,?,?,?,?,?,?,?)");
//
//            ps.setLong(1,timeShareId);
//            ps.setString(2,userId);
//            ps.setString(3,timeShareDate);
//            ps.setString(4,timeShareProjCode);
//            ps.setString(5, timeShareProjName);
//            ps.setString(6, timeShareActivityName);
//            ps.setString(7, timeShareTaskName);
//            ps.setString(8, timeShareStartTime);
//            ps.setString(9, timeShareEndTime);
//            ps.setString(10, consumedTime);
//            ps.setString(11, description);
//            ps.setTimestamp(12, createdOn);
//
//            x = ps.executeUpdate();
//            ps.close();
//
//
//            String sql = "insert into DAILY_TIME_SHARE_MEASURABLE(FK_TIME_SHARE_ID,FK_MEASURABLE_ID,MEASURABLE_QUANTITY,MEASURABLE_UNIT) values(?,?,?,?)";
//            PreparedStatement ps1 = con.prepareStatement(sql);
//            con.setAutoCommit(false);
//            for (MeasurableListDataModel mList:measurableList)
//            {
//                ps1.setLong(1,timeShareId);
//                ps1.setLong(2, Long.parseLong(mList.getMeasurableName().replaceAll("[^0-9]", "")));
//                ps1.setString(3, mList.getMeasurableQty());
//                ps1.setString(4, mList.getMeasurableUnit());
//
//                ps1.addBatch();
//
//            }
//
//            int[] x1=ps1.executeBatch();
//
//            if (x==1 ||  x1.length>0){
//                result = "true";
//            }
//
//            con.commit();
//            ps1.close();
//            con.close();
//        }
//        catch(Exception e){
//            e.printStackTrace();  `
//        }
//
//        return result;
//    }
//
//    // Getting Measurable List
//    public ArrayList<MeasurableListDataModel> getMeasurableList(){
//
//        ArrayList measurableList = new ArrayList();
//        MeasurableListDataModel measurableListDataModel;
//
//        Connection con;
//        try {
//            con = DatabaseHelper.getDBConnection();
//
//            PreparedStatement ps = con.prepareStatement("select * from MEASURABLES ORDER BY NAME ASC");
//
//
//            ResultSet rs = ps.executeQuery();
//
//            while (rs.next()) {
//                measurableListDataModel= new MeasurableListDataModel();
//
//                measurableListDataModel.setId(rs.getString("ID"));
//                measurableListDataModel.setMeasurableName(rs.getString("NAME"));
//
//                measurableList.add(measurableListDataModel);
//
//            }
//            rs.close();
//            ps.close();
//            con.close();
//        } catch (Exception e) { e.printStackTrace(); }
//
//        Log.d("Measurable List",measurableList.toString());
//        return measurableList;
//
//    }
//
//    // Getting Measurable Unit List
//    public ArrayList <String> getMeasurableUnit(){
//
//        ArrayList unitNameList = new ArrayList();
//
//        Connection con;
//        try {
//            con = DatabaseHelper.getDBConnection();
//
//            PreparedStatement ps = con.prepareStatement("select * from MEASURABLE_UNIT");
//
//            ResultSet rs = ps.executeQuery();
//
//            while (rs.next())
//            {
//                String s = rs.getString("UNIT_NAME");
//
//                unitNameList.add(s);
//            }
//            rs.close();
//            ps.close();
//            con.close();
//        } catch (Exception e) { e.printStackTrace(); }
//
//        return unitNameList;
//
//    }

//    public void getActivityList(){
//        appExecutor.getNetworkIO().execute(() -> {
//            Call<ArrayList<String>> activityListResponse = activityInterface.getActivityList();
//            activityListResponse.enqueue(new Callback<ArrayList<String>>() {
//                @Override
//                public void onResponse(Call<ArrayList<String>> call, Response<ArrayList<String>> response) {
//                    if(response.isSuccessful() && response.body() != null){
//                        ArrayList<String> activityList = (ArrayList<String>) response.body().getBody();
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<ArrayList<String>> call, Throwable t) {
//                    appExecutor.getMainThread().execute(() -> {
//                        Log.e("Network Request", "Failed to get" + t.getMessage());
//                    });
//                }
//            });
//        });
//
//
//    }

    public void getActivityListAndUpdateUi() {
        appExecutor.getNetworkIO().execute(() -> {
            Call<List<String>> activityListResponse = activityInterface.getActivitiesName();
            activityListResponse.enqueue(new Callback<List<String>>() {
                @Override
                public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                    if (response.isSuccessful() && response.body() instanceof APISuccessResponse) {
                        // Cast the response to APISuccessResponse to access the body
                        APISuccessResponse<List<String>> successResponse = (APISuccessResponse<List<String>>) response.body();
                        ArrayList<String> activityList = (ArrayList<String>) successResponse.getBody(); // Use the correct getter
                        appExecutor.getMainThread().execute(() -> {
                            ArrayAdapter<String> activityNameAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, activityList);
                            timeShareActivityName.setAdapter(activityNameAdapter);
                        });
                    } else {
                        appExecutor.getMainThread().execute(() ->
                                Toast.makeText(getApplicationContext(), "couldn't fetch activity list", Toast.LENGTH_LONG).show());
                    }
                }

                @Override
                public void onFailure(Call<List<String>> call, Throwable t) {
                    Log.e("Network Request", "Failed: " + t.getMessage());
                    appExecutor.getMainThread().execute( () -> {
                        Toast.makeText(getApplicationContext(), "Error"+t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                    );
                }
            });
        });
    }

    public void getProjectNameListAndUpdateUi() {
        appExecutor.getNetworkIO().execute(() -> {
            Call<List<String>> projectNameResponse = projectServiceInterface.getProjectNameList();
            projectNameResponse.enqueue(new Callback<List<String>>() {
                @Override
                public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                    if (response.isSuccessful() && response.body() instanceof APISuccessResponse) {
                        // Cast the response to APISuccessResponse to access the body
                        APISuccessResponse<List<String>> successResponse = ( APISuccessResponse<List<String>>) response.body();
                        ArrayList<String> projectList = (ArrayList<String>) successResponse.getBody(); // Use the correct getter
                        appExecutor.getMainThread().execute(() -> {
                            ArrayAdapter<String> projectNameAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, projectList);
                            timeShareTaskName.setAdapter(projectNameAdapter);
                        });
                    } else {
                        appExecutor.getMainThread().execute(() ->
                                Toast.makeText(getApplicationContext(), "couldn't fetch project List", Toast.LENGTH_LONG).show());
                    }

                }

                @Override
                public void onFailure(Call<List<String>> call, Throwable t) {
                    Log.e("Network Request", "Failed: " + t.getMessage());
                    appExecutor.getMainThread().execute( () -> {
                                Toast.makeText(getApplicationContext(), "Error"+t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                    );

                }
            });
        });

    }

    public void getTaskNameListAndUpdateUi() {
        appExecutor.getNetworkIO().execute(() -> {
            Call<List<String>> taskNameResponse = taskServiceInterface.getTaskNames();
            taskNameResponse.enqueue(new Callback<List<String>>() {
                @Override
                public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                    if (response.isSuccessful() && response.body() instanceof APISuccessResponse) {
                        // Cast the response to APISuccessResponse to access the body
                        APISuccessResponse<List<String>> successResponse = (APISuccessResponse<List<String>>) response.body();
                        ArrayList<String> taskList = (ArrayList<String>) successResponse.getBody(); // Use the correct getter
                        appExecutor.getMainThread().execute(() -> {
                            ArrayAdapter<String> taskNameAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, taskList);
                            timeShareTaskName.setAdapter(taskNameAdapter);
                        });
                    } else {
                        appExecutor.getMainThread().execute(() ->
                                Toast.makeText(getApplicationContext(), "couldn't fetch task List", Toast.LENGTH_LONG).show());
                    }

                }

                @Override
                public void onFailure(Call<List<String>> call, Throwable t) {
                    Log.e("Network Request", "Failed: " + t.getMessage());
                    appExecutor.getMainThread().execute( () -> {
                                Toast.makeText(getApplicationContext(), "Error"+t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                    );

                }
            });
        });

    }
    public CompletableFuture<String> getProjectCodeAndUpdateUi(String projectName) {
        CompletableFuture<String> future = new CompletableFuture<>();
        appExecutor.getNetworkIO().execute(() -> {

            Call<ResponseBody> projectCodeResponse = projectServiceInterface.getProjectCodeViaProjectName(projectName);
            projectCodeResponse.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    APIResponse apiResponse = null;
                    try {
                        apiResponse = APIResponse.create(response);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if(apiResponse instanceof APISuccessResponse) {
                        String responseBody = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().toString();
                        JSONConfig<String> jsonConfig = new JSONConfig<>();
                        try {

                            String projectCode = jsonConfig.extractBodyFromJson(responseBody);
                            future.complete(projectCode);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }


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
            Call<ResponseBody> measurableListResponse = measurableServiceInterface.getMeasurableList();
            measurableListResponse.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    APIResponse apiResponse;
                    try {
                        apiResponse = APIResponse.create(response);
                        if (apiResponse instanceof APISuccessResponse) {
                            String rspnseBdy = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().toString();
                            rspnseBdy = rspnseBdy.replaceAll("\"body\":\"\\[", "\"body\":[");
                            rspnseBdy = rspnseBdy.replaceAll("=\\{", ":{")
                                    .replaceAll("=", ":")
                                    .replaceAll(", ", "\",\"")
                                    .replaceAll(",", "\"")
                                    .replaceAll("},", "},")
                                    .replaceAll("}]", "}]")
                                    .replaceAll("\"", "\"");
                            Log.e("responsebody",""+rspnseBdy);
                            JSONConfig<MeasurableListDataModel> jsonConfig = new JSONConfig<>();
                            List<MeasurableListDataModel> measurableListDataModels1 = jsonConfig.extractListFromBodyFromJson(rspnseBdy,MeasurableListDataModel.class);
                            Log.e("measurable list",""+measurableListDataModels1);
                            future.complete(measurableListDataModels1);

                        }else if(apiResponse instanceof APIErrorResponse){
                            String erMsg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                            Log.e("Error Received",""+erMsg);
                        }else if(apiResponse instanceof APIErrorResponse){
                            Log.e("Response","is empty repsonse"+apiResponse);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }



//                    if (response.isSuccessful() && response.body() instanceof APISuccessResponse) {
//                        // Cast the response to APISuccessResponse to access the body
//                        APISuccessResponse<List<MeasurableListDataModel>> successResponse = (APISuccessResponse<List<MeasurableListDataModel>>) response.body();
//                        ArrayList<MeasurableListDataModel> measurableList = (ArrayList<MeasurableListDataModel>) successResponse.getBody(); // Use the correct getter
//                        appExecutor.getMainThread().execute(() -> {
//
//                            ArrayAdapter<MeasurableListDataModel> adapterMeasurable = new ArrayAdapter<MeasurableListDataModel>(getBaseContext(), android.R.layout.simple_spinner_item, measurableList);
//                            adapterMeasurable.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                            timeShareMeasurable.setAdapter(adapterMeasurable);
//                        });
//
//                    } else {
//                        appExecutor.getMainThread().execute(() ->
//                                Toast.makeText(getApplicationContext(), "couldn't fetch measurable list", Toast.LENGTH_LONG).show());
//                    }
//
               }




                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("Network Request", "Failed: " + t.getMessage());
//                    appExecutor.getMainThread().execute( () -> {
//                                Toast.makeText(getApplicationContext(), "Error"+t.getMessage(), Toast.LENGTH_LONG).show();
//                            }
//                    );

                }
            });
        });
            return future;
    }
    // Getting Activity List
//    public ArrayList<String> getActivityList(){
//
//        ArrayList<String> activityList = new ArrayList();
//
//        Connection con;
//        try {
//            con = DatabaseHelper.getDBConnection();
//
//            PreparedStatement ps = con.prepareStatement("select DISTINCT NAME from ACTIVITY");
//
//            ResultSet rs = ps.executeQuery();
//
//            while (rs.next()) {
//                String s = rs.getString("NAME");
//
//                activityList.add(s);
//
//            }
//            rs.close();
//            ps.close();
//            con.close();
//        } catch (Exception e) { e.printStackTrace(); }
//
//        return activityList;
//
//    }
/*
    // Getting Task List
    public ArrayList<String> getTaskList(){

        ArrayList<String> activityList = new ArrayList();

        Connection con;
        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("select DISTINCT NAME from TASK");


            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String s = rs.getString("NAME");

                activityList.add(s);

            }
            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        return activityList;

    }

    // Getting Project Name List
    public ArrayList<String> getProjectNameList(){

        ArrayList<String> projectNameList = new ArrayList();

        Connection con;
        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("select DISTINCT NAME from PROJECT");


            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String s = rs.getString("NAME");

                projectNameList.add(s);

            }
            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        return projectNameList;

    }
*/

    // Getting Project Code List
//    public String getProjectCode(String projectName){
//
//        String projectCode=null;
//
//        Connection con;
//        try {
//            con = DatabaseHelper.getDBConnection();
//
//            PreparedStatement ps = con.prepareStatement("select DISTINCT CODE from PROJECT WHERE NAME = ?");
//            ps.setString(1, projectName);
//
//            ResultSet rs = ps.executeQuery();
//
//            while (rs.next()) {
//                projectCode = rs.getString("CODE");
//
//            }
//            rs.close();
//            ps.close();
//            con.close();
//        } catch (Exception e) { e.printStackTrace(); }
//
//        return projectCode;
//
//    }

}
