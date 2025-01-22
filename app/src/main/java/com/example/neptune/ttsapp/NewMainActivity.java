//package com.example.neptune.ttsapp;
//
//import android.os.Bundle;
//
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.databinding.DataBindingUtil;
//import androidx.lifecycle.ViewModelProvider;
//
//import com.example.neptune.ttsapp.databinding.ActivityTtsmainBinding;
//
//public class NewMainActivity extends AppCompatActivity {
//    private MainActivityViewModel viewModel;
//    private ActivityTtsmainBinding activityTtsmainBinding;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
////        activityTtsmainBinding = DataBindingUtil.setContentView(this,R.layout.activity_ttsadmin);
////
////        activityTtsmainBinding.setViewModel(viewModel);
////        activityTtsmainBinding.setLifecycleOwner(this);
//
//        viewModel.getTimeShareDate().observe(this, date -> activityTtsmainBinding
//                .editTextMainDate.setText(date)
//        );
//
//        viewModel.getTimeShareActivityName().observe(this, activity -> activityTtsmainBinding
//                .editTextMainActName.setText(activity)
//        );
//
//        viewModel.getTimeShareDescription().observe(this, descripton -> activityTtsmainBinding
//                .editTextMainDescription.setText(descripton)
//        );
//
//        viewModel.getTimeShareProjName().observe(this, pn -> activityTtsmainBinding
//                .editTextMainProjName.setText(pn)
//        );
//
//        viewModel.getTimeShareTaskName().observe(this, t -> activityTtsmainBinding
//                .editTextMainTaskName.setText(t)
//        );
//
//        viewModel.getTimeShareStartTime().observe(this, st -> activityTtsmainBinding
//                .editTextMainStartTime.setText(st));
//
//        viewModel.getTimeShareEndTime().observe(this, et -> activityTtsmainBinding
//                .editTextMainEndTime.setText(et));
//
//        viewModel.getTimeShareMeasurableUnit().observe(this, u -> activityTtsmainBinding
//                .editTextMainUnit.setText(u));
//
//        viewModel.getTimeShareMeasurableQty().observe(this, q -> activityTtsmainBinding
//                .editTextMainQty.setText(q) );
//    }
//}
