package com.example.neptune.ttsapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import android.widget.ImageButton;
import android.widget.TextView;

import com.example.neptune.ttsapp.DTO.DailyTimeShareMeasurable;

import java.util.ArrayList;


public class MeasurableListCustomAdapter extends ArrayAdapter<MeasurableListDataModel> implements View.OnClickListener{

    private ArrayList<MeasurableListDataModel> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView measurableName;
        TextView measurableQty;
        TextView measurableUnit;
        ImageButton deleteMeasurable;


    }



    public MeasurableListCustomAdapter(ArrayList<MeasurableListDataModel> data, Context context) {
        super(context, R.layout.measurable_list_row_item, data);
        this.dataSet = data;
        this.mContext=context;

    }


    @Override
    public void onClick(View v) {


        int position=(Integer) v.getTag();
        Object object= getItem(position);
        MeasurableListDataModel measurableListDataModel=(MeasurableListDataModel)object;




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

        MeasurableListDataModel measurableListDataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.measurable_list_row_item, parent, false);
            viewHolder.measurableName = (TextView) convertView.findViewById(R.id.textViewMeasurableName);
            viewHolder.measurableQty = (TextView) convertView.findViewById(R.id.textViewMeasurableQty);
            viewHolder.measurableUnit = (TextView) convertView.findViewById(R.id.textViewMeasurableUnit);
            viewHolder.deleteMeasurable = (ImageButton) convertView.findViewById(R.id.deleteMeasurable);

            try
            {
                viewHolder.deleteMeasurable.setTag(position);
                viewHolder.deleteMeasurable.setOnClickListener(v -> {
                            Integer index = (Integer) v.getTag();
                            dataSet.remove(index.intValue());
                            notifyDataSetChanged();
                        }
                );
            }catch (Exception e){e.printStackTrace();}

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }


        viewHolder.measurableName.setText(measurableListDataModel.getMeasurableName());
        viewHolder.measurableQty.setText(measurableListDataModel.getMeasurableQty());
        viewHolder.measurableUnit.setText(measurableListDataModel.getMeasurableUnit());

//        viewHolder.info.setOnClickListener(this);
//        viewHolder.info.setTag(position);

        // Return the completed view to render on screen
        return convertView;
    }


}
