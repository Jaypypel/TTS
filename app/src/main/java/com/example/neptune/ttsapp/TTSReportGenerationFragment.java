package com.example.neptune.ttsapp;


import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.neptune.ttsapp.Network.APIResponse;
import com.example.neptune.ttsapp.Network.APISuccessResponse;
import com.example.neptune.ttsapp.Network.ResponseBody;
import com.example.neptune.ttsapp.Network.UserServiceInterface;
import com.example.neptune.ttsapp.Util.DateConverter;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.NumberFormat;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class TTSReportGenerationFragment extends Fragment {

    @Inject
    AppExecutors appExecutors;

    @Inject
    UserServiceInterface userService;

//    public TTSReportGenerationFragment() { }

    private EditText startDate,endDate;
    private TextView user,date,time;
    private Button btnReportGenerate;
    private Spinner spinnerSelectUser;
    private int mYear, mMonth, mDay;

    private SessionManager sessionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ttsreport_generation, container, false);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        user=view.findViewById(R.id.textViewRGUser);
        sessionManager = new SessionManager(getActivity().getApplicationContext());
        user.setText(sessionManager.getUserID());

        date=view.findViewById(R.id.textViewRGDate);
        time=view.findViewById(R.id.textViewRGTime);


           appExecutors.getMainThread().execute(() -> {
            date.setText(DateConverter.currentDate());
            time.setText(DateConverter.currentTime());
        });

        startDate=view.findViewById(R.id.editTextRGStartDate);
        endDate=view.findViewById(R.id.editTextRGEndDate);

        btnReportGenerate=view.findViewById(R.id.buttonRGReportGenerate);
        spinnerSelectUser = view.findViewById(R.id.spinnerRGSelectUser);

        if (InternetConnectivity.isConnected()) {

            getUsernames().thenAccept(usernames -> {
                ArrayList<String>  users = usernames;
                //users.add(0,"Select user");
                ArrayAdapter<String> userSelectAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item,users);
                userSelectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerSelectUser.setAdapter(userSelectAdapter);
            }).exceptionally(e -> {Toast.makeText(getActivity().getApplicationContext(), "can't update usernames", Toast.LENGTH_LONG).show();
                return null;
            });
//            ArrayList users = getUserList();
//            users.add(0, "Select User");
//            spinnerSelectUser = view.findViewById(R.id.spinnerRGSelectUser);
//            ArrayAdapter<String> userAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,users);
//            userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            spinnerSelectUser.setAdapter(userAdapter);
        }else {Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}



        startDate.setFocusable(false);
        endDate.setFocusable(false);

        //Date Picker start
        startDate.setOnClickListener(v -> {
            //To show current date in the datepicker
            Calendar mcurrentDate=Calendar.getInstance();
            mYear=mcurrentDate.get(Calendar.YEAR);
            mMonth=mcurrentDate.get(Calendar.MONTH);
            mDay=mcurrentDate.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog mDatePicker=new DatePickerDialog(getActivity(), (view12, year, month, dayOfMonth) ->
            startDate.setText(convertDateTime(dayOfMonth) + "/" + convertDateTime((month+1))  + "/" + year),mYear, mMonth, mDay);
            mDatePicker.getDatePicker().setCalendarViewShown(false);
            mDatePicker.setTitle("Select date");
            mDatePicker.show();

        });

        endDate.setOnClickListener(v -> {

            //To show current date in the datepicker
            Calendar mcurrentDate=Calendar.getInstance();
            mYear=mcurrentDate.get(Calendar.YEAR);
            mMonth=mcurrentDate.get(Calendar.MONTH);
            mDay=mcurrentDate.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog mDatePicker=new DatePickerDialog(getActivity(), (view1, year, month, dayOfMonth) ->
            endDate.setText(convertDateTime(dayOfMonth) + "/" + convertDateTime((month+1))  + "/" + year),mYear, mMonth, mDay);
            mDatePicker.getDatePicker().setCalendarViewShown(false);
            mDatePicker.setTitle("Select date");
            mDatePicker.show();

        });

        btnReportGenerate.setOnClickListener(v -> {
            try
            {
                if (InternetConnectivity.isConnected())
                {
                    if (isStartDateValid().isEmpty()){startDate.setError("Start Date Cannot Be Empty");}
                    else if (isEndDateValid().isEmpty()){endDate.setError("Start Date Cannot Be Empty");}
                    else
                    {
                        generateUserDTSExcelReport(getUserDTSReportDetails(isUserValid(), isStartDateValid(), isEndDateValid()));
                        startDate.setText("");
                        endDate.setText("");
                    }
                }else {Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}

            } catch (Exception e){e.printStackTrace();}
        });

        return view;
    }

    // Add leading 0 when input date or time is single No like 5
    public String convertDateTime(int input) {
        if (input >= 10) { return String.valueOf(input); }
        else { return "0" + input; }
    }

    private String isUserValid()
    {
        return spinnerSelectUser.getSelectedItem().toString().trim();
    }

    private String isStartDateValid()
    {
        String date= startDate.getText().toString().trim().replaceAll("\\s+","");
        if(date.isEmpty()) { startDate.setError("Start Date Cannot Be Empty"); }
        return date;
    }

    private String isEndDateValid()
    {
        String date= endDate.getText().toString().trim().replaceAll("\\s+","");
        if(date.isEmpty()) { endDate.setError("Start Date Cannot Be Empty"); }
        return date;
    }

    private String getMonth()
    {
        String actualDate= startDate.getText().toString().trim().replaceAll("\\s+","");
        SimpleDateFormat month_date = new SimpleDateFormat("MMM yyyy", Locale.ENGLISH);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        Date date = null;
        try {
            date = sdf.parse(actualDate);
        } catch (ParseException e) { e.printStackTrace(); }

        return month_date.format(date);
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
                        JsonElement result = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody();
                        Gson gson = new Gson();
                        Type listType = new TypeToken<ArrayList<String>>() {}.getType();

                        if(result.isJsonArray()){
                            JsonArray usernames = result.getAsJsonArray();
                            ArrayList<String> list = gson.fromJson(usernames, listType);
                            future.complete(list);

                        }
                    } catch (RuntimeException e) {
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

        });

        return future;
    }

    // Getting Report Generation Details
    public ArrayList <ReportGenerationDataModel> getUserReportDetails(String userID, String startDate,String endDate){

        ArrayList<ReportGenerationDataModel> reportDetails = new ArrayList();
        ReportGenerationDataModel reportGenerationDataModel;
        Connection con;

        try {
            con = DatabaseHelper.getDBConnection();

            String query = "SELECT TASK_MANAGEMENT.*,TIME_SHARE.*,TIME_SHARE_MEASURABLES.MEASURABLE_QUANTITY, TIME_SHARE_MEASURABLES.MEASURABLE_UNIT,MEASURABLES.NAME FROM TASK_MANAGEMENT " +
                    "RIGHT OUTER JOIN TIME_SHARE ON TASK_MANAGEMENT.ID = TIME_SHARE.FK_TASK_MANAGEMENT_ID " +
                    "LEFT JOIN TIME_SHARE_MEASURABLES ON TIME_SHARE.ID = TIME_SHARE_MEASURABLES.FK_TIME_SHARE_ID " +
                    "LEFT JOIN MEASURABLES ON TIME_SHARE_MEASURABLES.FK_MEASURABLE_ID= MEASURABLES.ID " +
                    "WHERE TASK_MANAGEMENT.FK_AUTHENTICATION_RECEIVED_USER_ID = ? AND TIME_SHARE.DATE_OF_TIME_SHARE BETWEEN ? AND ? ";

            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, userID);
            ps.setString(2,startDate);
            ps.setString(3,endDate);


            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                reportGenerationDataModel = new ReportGenerationDataModel();

                reportGenerationDataModel.setTaskOwnerUser(rs.getString("FK_AUTHENTICATION_OWNER_USER_ID"));
                reportGenerationDataModel.setTaskReceivedUser(rs.getString("FK_AUTHENTICATION_RECEIVED_USER_ID"));
                reportGenerationDataModel.setActivityName(rs.getString("ACTIVITY_NAME"));
                reportGenerationDataModel.setTaskName(rs.getString("TASK_NAME"));
                reportGenerationDataModel.setProjectCode(rs.getString("PROJECT_ID"));
                reportGenerationDataModel.setProjectName(rs.getString("PROJECT_NAME"));
                reportGenerationDataModel.setActualTotalTime(rs.getString("ACTUAL_TOTAL_TIME"));
                reportGenerationDataModel.setTimeShareDate(rs.getString("DATE_OF_TIME_SHARE"));
                reportGenerationDataModel.setStartTime(rs.getString("START_TIME"));
                reportGenerationDataModel.setEndTime(rs.getString("END_TIME"));
                reportGenerationDataModel.setTimeDifference(rs.getString("TIME_DIFFERENCE"));
                reportGenerationDataModel.setMeasurableName(rs.getString("NAME"));
                reportGenerationDataModel.setMeasurableQty(rs.getString("MEASURABLE_QUANTITY"));
                reportGenerationDataModel.setMeasurableUnit(rs.getString("MEASURABLE_UNIT"));
                reportGenerationDataModel.setDescription(rs.getString("DESCRIPTION"));

                reportDetails.add(reportGenerationDataModel);
            }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) { e.printStackTrace(); }
        return reportDetails;
    }



    // Getting Report Generation Details
    public ArrayList <ReportDTSDataModel> getUserDTSReportDetails(String userID, String startDate,String endDate){

        ArrayList<ReportDTSDataModel> reportDetails = new ArrayList();
        ReportDTSDataModel reportDTSDataModel;
        Connection con;

        try {
            con = DatabaseHelper.getDBConnection();

            String query = "SELECT DAILY_TIME_SHARE.*,MEASURABLES.NAME,DAILY_TIME_SHARE_MEASURABLE.MEASURABLE_QUANTITY,DAILY_TIME_SHARE_MEASURABLE.MEASURABLE_UNIT FROM DAILY_TIME_SHARE\n" +
                    "LEFT JOIN DAILY_TIME_SHARE_MEASURABLE ON DAILY_TIME_SHARE.ID = DAILY_TIME_SHARE_MEASURABLE.FK_TIME_SHARE_ID\n" +
                    "LEFT JOIN MEASURABLES ON DAILY_TIME_SHARE_MEASURABLE.FK_MEASURABLE_ID = MEASURABLES.ID\n" +
                    "WHERE DAILY_TIME_SHARE.FK_AUTHENTICATION_USER_ID = ? AND \n" +
                    "DAILY_TIME_SHARE.DATE_OF_TIME_SHARE BETWEEN ? AND ? ";

            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, userID);
            ps.setString(2,startDate);
            ps.setString(3,endDate);


            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                reportDTSDataModel = new ReportDTSDataModel();

                reportDTSDataModel.setTimeShareDate(rs.getString("DATE_OF_TIME_SHARE"));
                reportDTSDataModel.setProjectCode(rs.getString("PROJECT_CODE"));
                reportDTSDataModel.setProjectName(rs.getString("PROJECT_NAME"));
                reportDTSDataModel.setActivityName(rs.getString("ACTIVITY_NAME"));
                reportDTSDataModel.setTaskName(rs.getString("TASK_NAME"));
                reportDTSDataModel.setStartTime(rs.getString("START_TIME"));
                reportDTSDataModel.setEndTime(rs.getString("END_TIME"));
                reportDTSDataModel.setConsumedTime(rs.getString("CONSUMED_TIME"));
                reportDTSDataModel.setMeasurableName(rs.getString("NAME"));
                reportDTSDataModel.setMeasurableQty(rs.getString("MEASURABLE_QUANTITY"));
                reportDTSDataModel.setMeasurableUnit(rs.getString("MEASURABLE_UNIT"));
                reportDTSDataModel.setDescription(rs.getString("DESCRIPTION"));

                reportDetails.add(reportDTSDataModel);
            }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) { e.printStackTrace(); }
        return reportDetails;
    }

//    private void sendEmail(){
//        String filename = "vilas Feb 2019 Report.xls";
//        File fileLocation = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ "Feb 2019 Reports" + filename);
//       // Uri path = Uri.fromFile(fileLocation);
//        Intent emailIntent = new Intent(Intent.ACTION_SEND);
//        // set the type to 'email'
//        emailIntent .setType("vnd.android.cursor.dir/email");
//        emailIntent.setType("application/x-vcard");
//        String to[] = {"harshadgondil25@gmail.com"};
//        emailIntent .putExtra(Intent.EXTRA_EMAIL, to);
//        // the attachment
//        emailIntent .putExtra(Intent.EXTRA_STREAM,Uri.parse( "file://"+fileLocation.getAbsolutePath()));
//        // the mail subject
//        emailIntent .putExtra(Intent.EXTRA_SUBJECT, "Feb 2019 Report");
//        startActivity(Intent.createChooser(emailIntent , "Send email..."));
//    }


    private void generateUserDTSExcelReport(ArrayList<ReportDTSDataModel> reportDTSDataModels)
    {
        File sd = Environment.getExternalStorageDirectory();
        // File Name
        String fileName = isUserValid() + " " + getMonth() + " " + "Report.xls";
        // Folder Name
        String folderName = getMonth() + " " + "Reports";

        File directory = new File(sd.getAbsolutePath() + "/" + folderName);
        //create directory if not exist
        if (!directory.isDirectory()) { directory.mkdirs(); }

        try {

            //file path
            File file = new File(directory, fileName);
            WorkbookSettings wbSettings = new WorkbookSettings();
            wbSettings.setLocale(new Locale("en", "EN"));
            WritableWorkbook workbook;

            workbook = Workbook.createWorkbook(file, wbSettings);

            //Excel sheet name. 0 represents first sheet
//            WritableSheet planSheet = workbook.createSheet("Planning", 0);
            WritableSheet sheet = workbook.createSheet("Daily Worksheet", 0);

//            NumberFormat numberFormat = new NumberFormat("00:00");
//            WritableCellFormat nFormat = new WritableCellFormat(numberFormat);


            WritableCellFormat cFormat = new WritableCellFormat();
            WritableFont font = new WritableFont(WritableFont.ARIAL, 16, WritableFont.BOLD);
            cFormat.setFont(font);

            // column and row for Daily Worksheet
            sheet.addCell(new Label(0, 0, "Date Of TimeShare",cFormat));
            sheet.addCell(new Label(1, 0, "Project Code",cFormat));
            sheet.addCell(new Label(2, 0, "Project Name",cFormat));
            sheet.addCell(new Label(3, 0, "Activity Name",cFormat));
            sheet.addCell(new Label(4, 0, "Task Name",cFormat));
            sheet.addCell(new Label(5, 0, "Start Time",cFormat));
            sheet.addCell(new Label(6, 0, "End Time",cFormat));
            sheet.addCell(new Label(7, 0, "Consumed Time",cFormat));
            sheet.addCell(new Label(8, 0, "Measurable Name",cFormat));
            sheet.addCell(new Label(9, 0, "Measurable Quantity",cFormat));
            sheet.addCell(new Label(10, 0, "Measurable Unit",cFormat));
            sheet.addCell(new Label(11, 0, "Description",cFormat));


            int j = 1;
            for (ReportDTSDataModel reportDataModel : reportDTSDataModels){

                sheet.addCell(new Label(0, j, reportDataModel.getTimeShareDate()));
                sheet.addCell(new Label(1, j, reportDataModel.getProjectCode()));
                sheet.addCell(new Label(2, j, reportDataModel.getProjectName()));
                sheet.addCell(new Label(3, j, reportDataModel.getActivityName()));
                sheet.addCell(new Label(4, j, reportDataModel.getTaskName()));
                sheet.addCell(new Label(5, j, reportDataModel.getStartTime()));
                sheet.addCell(new Label(6, j, reportDataModel.getEndTime()));
                sheet.addCell(new Label(7, j, reportDataModel.getConsumedTime()));
                sheet.addCell(new Label(8, j, reportDataModel.getMeasurableName()));
                sheet.addCell(new Label(9, j, reportDataModel.getMeasurableQty()));
                sheet.addCell(new Label(10, j, reportDataModel.getMeasurableUnit()));
                sheet.addCell(new Label(11, j, reportDataModel.getDescription()));


                j++;

            }


            workbook.write();
            workbook.close();
            Toast.makeText(getActivity().getApplicationContext(), isUserValid() + "  " + getMonth() + "  " + "Report Generated", Toast.LENGTH_LONG).show();

        } catch(Exception e){ e.printStackTrace(); }
    }


    private void generateUserExcelReport(ArrayList<ReportGenerationDataModel> reportGenerationDetails)
    {
        File sd = Environment.getExternalStorageDirectory();
        // File Name
        String fileName = isUserValid() + " " + getMonth() + " " + "Report.xls";
        // Folder Name
        String folderName = getMonth() + " " + "Reports";

        File directory = new File(sd.getAbsolutePath() + "/" + folderName);
        //create directory if not exist
        if (!directory.isDirectory()) { directory.mkdirs(); }

        try {

            //file path
            File file = new File(directory, fileName);
            WorkbookSettings wbSettings = new WorkbookSettings();
            wbSettings.setLocale(new Locale("en", "EN"));
            WritableWorkbook workbook;

            workbook = Workbook.createWorkbook(file, wbSettings);

            //Excel sheet name. 0 represents first sheet
            WritableSheet planSheet = workbook.createSheet("Planning", 0);
            WritableSheet sheet = workbook.createSheet("Daily Worksheet", 1);


            WritableCellFormat cFormat = new WritableCellFormat();
            WritableFont font = new WritableFont(WritableFont.ARIAL, 16, WritableFont.BOLD);
            cFormat.setFont(font);

            // column and row for Daily Worksheet
            sheet.addCell(new Label(0, 0, "Task Owner User",cFormat));
            sheet.addCell(new Label(1, 0, "Task Received User",cFormat));
            sheet.addCell(new Label(2, 0, "Activity Name",cFormat));
            sheet.addCell(new Label(3, 0, "Task Name",cFormat));
            sheet.addCell(new Label(4, 0, "Project Code",cFormat));
            sheet.addCell(new Label(5, 0, "Project Name",cFormat));
            sheet.addCell(new Label(6, 0, "Date Of TimeShare",cFormat));
            sheet.addCell(new Label(7, 0, "Start Time",cFormat));
            sheet.addCell(new Label(8, 0, "End Time",cFormat));
            sheet.addCell(new Label(9, 0, "Time Difference",cFormat));
            sheet.addCell(new Label(10, 0, "Measurable Name",cFormat));
            sheet.addCell(new Label(11, 0, "Measurable Quantity",cFormat));
            sheet.addCell(new Label(12, 0, "Measurable Unit",cFormat));
            sheet.addCell(new Label(13, 0, "Description",cFormat));

            // column and row for Planning
            planSheet.addCell(new Label(0, 0, "Project Name",cFormat));
            planSheet.addCell(new Label(1, 0, "Activity Name",cFormat));
            planSheet.addCell(new Label(2, 0, "Task Name",cFormat));
            planSheet.addCell(new Label(3, 0, "Actual Time",cFormat));


            int j = 1;
            for (ReportGenerationDataModel reportGenerationDataModel : reportGenerationDetails){

                sheet.addCell(new Label(0, j, reportGenerationDataModel.getTaskOwnerUser()));
                sheet.addCell(new Label(1, j, reportGenerationDataModel.getTaskReceivedUser()));
                sheet.addCell(new Label(2, j, reportGenerationDataModel.getActivityName()));
                sheet.addCell(new Label(3, j, reportGenerationDataModel.getTaskName()));
                sheet.addCell(new Label(4, j, reportGenerationDataModel.getProjectCode()));
                sheet.addCell(new Label(5, j, reportGenerationDataModel.getProjectName()));
                sheet.addCell(new Label(6, j, reportGenerationDataModel.getTimeShareDate()));
                sheet.addCell(new Label(7, j, reportGenerationDataModel.getStartTime()));
                sheet.addCell(new Label(8, j, reportGenerationDataModel.getEndTime()));
                sheet.addCell(new Label(9, j, reportGenerationDataModel.getTimeDifference()));
                sheet.addCell(new Label(10, j, reportGenerationDataModel.getMeasurableName()));
                sheet.addCell(new Label(11, j, reportGenerationDataModel.getMeasurableQty()));
                sheet.addCell(new Label(12, j, reportGenerationDataModel.getMeasurableUnit()));
                sheet.addCell(new Label(13, j, reportGenerationDataModel.getDescription()));

                planSheet.addCell(new Label(0, j, reportGenerationDataModel.getProjectName()));
                planSheet.addCell(new Label(1, j, reportGenerationDataModel.getActivityName()));
                planSheet.addCell(new Label(2, j, reportGenerationDataModel.getTaskName()));
                planSheet.addCell(new Label(3, j, reportGenerationDataModel.getActualTotalTime()));
                j++;

            }


            workbook.write();
            workbook.close();
            Toast.makeText(getActivity().getApplicationContext(), isUserValid() + "  " + getMonth() + "  " + "Report Generated", Toast.LENGTH_LONG).show();

        } catch(Exception e){ e.printStackTrace(); }
    }
}
