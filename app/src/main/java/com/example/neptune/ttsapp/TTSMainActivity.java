package com.example.neptune.ttsapp;


import android.content.Intent;
import android.graphics.Color;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Looper;
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
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import java.util.List;

import java.util.concurrent.CompletableFuture;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;


import dagger.hilt.android.AndroidEntryPoint;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


@AndroidEntryPoint
public class TTSMainActivity extends AppCompatActivity {





    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private SessionManager sessionManager;

    public static TTSMainActivity mainActivity;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainActivity = null; // Prevent memory leaks
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ttsmain);

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        sessionManager = new SessionManager(getApplicationContext());

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        // Setup navigation drawer
        setupNavigationDrawer();

        // Setup NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();

            // Configure app bar
            mAppBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.nav_daily_time_share,
                    R.id.nav_dts_view,
                    R.id.nav_received_tasks,
                    R.id.nav_committed_tasks,
                    R.id.nav_approval_completion_tasks,
                    R.id.nav_work_done_status,
                    R.id.nav_assign_task,
                    R.id.nav_assigned_tasks,
                    R.id.nav_accepted_tasks,
                    R.id.nav_completed_tasks,
                    R.id.nav_modified_tasks,
                    R.id.nav_task_admin)
                    .setOpenableLayout(mDrawerLayout)
                    .build();

            // Setup navigation UI
            NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
            NavigationUI.setupWithNavController(navigationView, navController);

            // Custom navigation listener
            setupNavigationItemListener(navController);
        }
    }

    private void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        ) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setupNavigationItemListener(NavController navController) {
        navigationView.setNavigationItemSelectedListener(item -> {
            // Handle admin navigation separately
            if (item.getItemId() == R.id.nav_task_admin) {
                if (isAdmin()) {
                    startActivity(new Intent(this, TTSAdminActivity.class));
                } else {
                    Toast.makeText(getApplicationContext(), "You Are Not Admin", Toast.LENGTH_LONG).show();
                }
                mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }

            // Handle regular navigation
            boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
            if (handled) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            }
            return handled;
        });
    }

    private boolean isAdmin() {
        String token = sessionManager.getToken();
        return token.equals("Prerna") || token.equals("YoKo") ||
                token.equals("swar") || token.equals("mangal");
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("EXIT")
                    .setMessage(Html.fromHtml("<b>Do You Want To Logged Out..?</b>"))
                    .setPositiveButton("Yes", (dialog, which) -> {
                        sessionManager.logout();
                        startActivity(new Intent(TTSMainActivity.this, TTSLoginActivity.class));
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }


}