package com.example.neptune.ttsapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;


public class TaskAllocatedListCustomAdapter extends ArrayAdapter<TaskDataModel> implements View.OnClickListener{

    private ArrayList<TaskDataModel> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView txttaskAllocateOwnerName;
        TextView txttaskAllocateTaskDate;
        TextView txttaskAllocateTaskName;
        TextView txttaskAllocateTaskStatus;


    }



    public TaskAllocatedListCustomAdapter(ArrayList<TaskDataModel> data, Context context) {
        super(context, R.layout.task_acceptance_row_item, data);
        this.dataSet = data;
        this.mContext=context;

    }


    @Override
    public void onClick(View v) {


        int position=(Integer) v.getTag();
        Object object= getItem(position);
        TaskDataModel dataModel=(TaskDataModel)object;




        switch (v.getId())
        {

//            case R.id.item_info:
//
////                Snackbar.make(v, "Release date " +dataModel.getFeature(), Snackbar.LENGTH_LONG)
////                        .setAction("No action", null).show();
//
//                break;


        }


    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position

        TaskDataModel dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {


            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.task_acceptance_row_item, parent, false);
            viewHolder.txttaskAllocateOwnerName = (TextView) convertView.findViewById(R.id.taskDeligateOwnerName);
            viewHolder.txttaskAllocateTaskDate = (TextView) convertView.findViewById(R.id.taskDeligateTaskDate);
            viewHolder.txttaskAllocateTaskName = (TextView) convertView.findViewById(R.id.taskDeligateTaskName);
            viewHolder.txttaskAllocateTaskStatus = (TextView) convertView.findViewById(R.id.taskDeligateTaskStatus);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        viewHolder.txttaskAllocateOwnerName.setText(dataModel.getTaskDeligateOwnerUserID());
        viewHolder.txttaskAllocateTaskDate.setText(extractDate(dataModel.getDeligationDateTime()));
        viewHolder.txttaskAllocateTaskName.setText(dataModel.getTaskName());
        viewHolder.txttaskAllocateTaskStatus.setText(dataModel.getStatus());


        return convertView;
    }

    private String extractDate(String timestamp)
    {
        String dateStr = null;
        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sdf.parse(timestamp);

            SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yy");
            dateStr = sdf2.format(date);
        }catch (Exception e){e.printStackTrace();}

        return dateStr;
    }

}
