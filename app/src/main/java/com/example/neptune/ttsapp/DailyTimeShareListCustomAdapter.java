package com.example.neptune.ttsapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Objects;


public class DailyTimeShareListCustomAdapter extends ArrayAdapter<DailyTimeShareDataModel>{

    private ArrayList<DailyTimeShareDataModel> dataSet;
    private Context Context;

    // View lookup cache
    private static class ViewHolder {

        TextView txtdailyTimeShareProjName;
        TextView txtdailyTimeShareTaskName;
        TextView txtdailyTimeShareStartTime;
        TextView txtdailyTimeShareEndTime;

    }



    public DailyTimeShareListCustomAdapter(ArrayList<DailyTimeShareDataModel> data, Context context) {
        super(context, R.layout.daily_time_share_row_item, data);
        this.dataSet = data;
        this.Context=context;

    }




    @NonNull
    @Override
    public View getView(int position,@NonNull View convertView,@NonNull ViewGroup parent) {

        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.daily_time_share_row_item, parent, false);

            // Initialize views with null safety
            viewHolder.txtdailyTimeShareProjName = convertView.findViewById(R.id.dailyTimeShareProjName);
            viewHolder.txtdailyTimeShareTaskName = convertView.findViewById(R.id.dailyTimeShareTaskName);
            viewHolder.txtdailyTimeShareStartTime = convertView.findViewById(R.id.dailyTimeShareStartTime);
            viewHolder.txtdailyTimeShareEndTime = convertView.findViewById(R.id.dailyTimeShareEndTime);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Get the data item for this(current) position
        DailyTimeShareDataModel dataModel = getItem(position);

        //Null-safe data population
        if(dataModel != null) {
            // Use Objects.toString to handle potential null values
            viewHolder.txtdailyTimeShareProjName.setText(Objects.toString(dataModel.getProjectName(), "Not Exist"));
            viewHolder.txtdailyTimeShareTaskName.setText(Objects.toString(dataModel.getTaskName(), "Not Exist"));
            viewHolder.txtdailyTimeShareStartTime.setText(Objects.toString(dataModel.getStartTime(), "Not Exist"));
            viewHolder.txtdailyTimeShareEndTime.setText(Objects.toString(dataModel.getEndTime(), "Not Exist"));
        }

        return convertView;
    }


}
