package com.example.neptune.ttsapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MeasurableAdapter extends ArrayAdapter<MeasurableListDataModel> implements Filterable {


    Context context;
    List<MeasurableListDataModel> measurables, filterMeasurables;

    LayoutInflater layoutInflater;

  public MeasurableAdapter(Context context, List<MeasurableListDataModel> measurableListDataModels ){
      super(context, android.R.layout.simple_dropdown_item_1line,measurableListDataModels   );
      this.context = context;
      this.measurables  = new ArrayList<>();
      this.filterMeasurables = new ArrayList<>();
      this.layoutInflater   = LayoutInflater.from(context);

  }


    @Override
    public int getCount() {
        return filterMeasurables.size();
    }


    @Nullable
    @Override
    public MeasurableListDataModel getItem(int currentItemPosition) {
        return filterMeasurables.get(currentItemPosition);
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView != null){
            convertView = layoutInflater.inflate(android.R.layout.simple_dropdown_item_1line,parent,false);
        }

        assert convertView != null;
        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(getItem(position).getId() + Objects.requireNonNull(getItem(position)).getMeasurableName());
        return  convertView;
    }


    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                List<MeasurableListDataModel> measurables = new ArrayList<>();
                if(constraint == null ||  constraint.length() == 0) {
                    measurables.addAll(measurables);
                }
                return null;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

            }
        };
    }
}
