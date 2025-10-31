package com.example.neptune.ttsapp;


import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.WindowCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import java.util.Set;

import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class TTSMainActivity extends AppCompatActivity {


    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_MENTOR = "ROLE_MENTOR";
    private static final String ROLE_LEARNER = "ROLE_LEARNER";
    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private SessionManager sessionManager;

    public static TTSMainActivity mainActivity;

    private AlertDialog mLogoutConfirmationDialog;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainActivity = null; // Prevent memory leaks
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Go edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

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
            // Title will be set dynamically by the destination change listener
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
                    R.id.nav_task_admin,
                    R.id.nav_task_to_learner, R.id.nav_learner_tasks,
                    R.id.nav_learner_assigned_tasks,R.id.nav_learner_accepted_tasks)
                    .setOpenableLayout(mDrawerLayout)
                    .build();

            // Setup navigation UI
            NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
            NavigationUI.setupWithNavController(navigationView, navController);

            // Add a listener to update the title with both username and fragment label
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (getSupportActionBar() != null) {
                    String username = sessionManager.getUsername();
                    CharSequence fragmentLabel = destination.getLabel();
                    if (username != null && !username.isEmpty() && fragmentLabel != null && !fragmentLabel.toString().isEmpty()) {
                        getSupportActionBar().setTitle(

                                fragmentLabel + "            " + username);
                    } else if (username != null && !username.isEmpty()) {
                        getSupportActionBar().setTitle(username);
                    } else {
                        getSupportActionBar().setTitle(fragmentLabel);
                    }
                }
            });


            // Custom navigation listener
            setupNavigationItemListener(navController);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // If the activity is stopped and the dialog is still showing,
        // dismiss it to prevent a window leak.
        if (mLogoutConfirmationDialog != null && mLogoutConfirmationDialog.isShowing()) {
            mLogoutConfirmationDialog.dismiss();
            mLogoutConfirmationDialog = null; // Clear the reference
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

    private boolean notRequireToLearner(int itemId) {
        return itemId == R.id.nav_approval_completion_tasks ||
                itemId == R.id.nav_assign_task ||
                itemId == R.id.nav_work_done_status ||
                itemId == R.id.nav_committed_tasks ||
                itemId == R.id.nav_assigned_tasks ||
                itemId == R.id.nav_modified_tasks ||
                itemId == R.id.nav_received_tasks;
    }

    private boolean denyAccess() {
        Toast.makeText(getApplicationContext(), "Not allowed", Toast.LENGTH_LONG).show();
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setupNavigationItemListener(NavController navController) {
        navigationView.setNavigationItemSelectedListener(item -> {

            if (notRequireToLearner(item.getItemId()) && isLearner()) {
                return denyAccess();
            } else if (item.getItemId() == R.id.nav_task_admin) {
                if (!isAdmin()) return denyAccess();
                else startActivity(new Intent(this, TTSAdminActivity.class));
            } else if (item.getItemId() == R.id.nav_task_to_learner) {
                if (!isMentor()) return denyAccess();
            } else if (item.getItemId() == R.id.nav_learner_tasks) {
                if (!isLearner()) return denyAccess();
                if (!isLearner()) {
                    // Temporarily disabling denyAccess to test the Toast in isLearner()
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                    return true; // We handled the click.
                }
            }
//            switch (item.getItemId()){
//                case R.id.nav_task_admin:
//                    if(!isAdmin()) denyAccess("You are not an admin");
//                    else startActivity(new Intent(this, TTSAdminActivity.class));
//                    break;
//                case R.id.nav_task_to_learner:
//                    if(!isMentor()) denyAccess("You are not a mentor");
//                    break;
//            }


            // Handle admin navigation separately
//            if (item.getItemId() == R.id.nav_task_admin) {
//                if (isAdmin()) {
//                    startActivity(new Intent(this, TTSAdminActivity.class));
//                } else {
//                    Toast.makeText(getApplicationContext(), "You Are Not Admin", Toast.LENGTH_LONG).show();
//                }
//
//            }
//            if(item.getItemId() == R.id.nav_task_to_learner){
//                if(isMentor()){
//                    navController.navigate("task_to_learner");
//                }else{
//                    Toast.makeText(getApplicationContext(), "You Are not mentor", Toast.LENGTH_LONG).show();
//                }
//                mDrawerLayout.closeDrawer(GravityCompat.START);
//                return true;
//            }
//            if(item.getItemId() == R.id.nav_approval_completion_tasks
//            || item.getItemId()== R.id.nav_assign_task || item.getItemId() == R.id.nav_assigned_tasks
//                    || item.getItemId() == R.id.nav_modified_tasks || item.getItemId() == R.id.nav_received_tasks){
//                if(isLearner()){
//                    Toast.makeText(getApplicationContext(), "not allowed", Toast.LENGTH_LONG).show();
//                }
//                mDrawerLayout.closeDrawer(GravityCompat.START);
//                return true;
//            }
//
//            if(item.getItemId() == R.id.nav_learner_tasks && isLearner()){
//                navController.navigate("learner_tasks");
//                mDrawerLayout.closeDrawer(GravityCompat.START);
//                return true;
//            }else {
//                Toast.makeText(getApplicationContext(), "not allowed", Toast.LENGTH_LONG).show();
//            }

            // Handle regular navigation
            boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
            if (handled) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            }
            return handled;
        });
    }

    private boolean isLearner() {
        Set<String> roles = sessionManager.getRoles();
        return roles != null && roles.contains(ROLE_LEARNER);
    }


    private boolean isAdmin() {
        Set<String> roles = sessionManager.getRoles();
        return roles != null && roles.contains(ROLE_ADMIN);
    }

    private boolean isMentor() {
        Set<String> roles = sessionManager.getRoles();
        return roles != null && roles.contains(ROLE_MENTOR);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation
                .findNavController(this, R.id.nav_host_fragment);
        return NavigationUI
                .navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
//        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
//            mDrawerLayout.closeDrawer(GravityCompat.START);
//        } else {
//            new AlertDialog.Builder(this)
//                    .setIcon(android.R.drawable.ic_dialog_alert)
//                    .setTitle("EXIT")
//                    .setMessage(Html.fromHtml("<b>Do You Want To Logged Out..?</b>"))
//                    .setPositiveButton("Yes", (dialog, which) -> {
//                        sessionManager.logout();
//                        startActivity(new Intent(TTSMainActivity.this, TTSLoginActivity.class));
//                        finish();
//                    })
//                    .setNegativeButton("No", null)
//                    .show();
//        }
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            // Back press event is handled by closing the drawer.
        } else {
            // Drawer is closed, show the logout confirmation dialog.

            // First, ensure the activity is not already finishing.
            if (isFinishing() || isDestroyed()) {
                return; // Don't show a dialog if the activity is already going away.
            }

            // If the dialog is already showing, don't create/show another one.
            if (mLogoutConfirmationDialog != null && mLogoutConfirmationDialog.isShowing()) {
                return;
            }

            mLogoutConfirmationDialog = new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("EXIT") // Consider using R.string.exit_title
                    .setMessage(Html.fromHtml("<b>Do You Want To Logged Out..?</b>")) // Consider using R.string.logout_confirmation
                    .setPositiveButton("Yes", (dialog, which) -> { // Consider R.string.yes
                        // The dialog will typically dismiss itself when a button is pressed.
                        sessionManager.logout();
                        startActivity(new Intent(TTSMainActivity.this, TTSLoginActivity.class));
                        finish(); // Finish this activity.
                    })
                    .setNegativeButton("No", (dialog, which) -> { // Consider R.string.no
                        // Dialog dismisses itself. No further action needed for "No".
                    })
                    .setOnDismissListener(dialogInterface -> {
                        mLogoutConfirmationDialog = null; // Clear the reference when dialog is dismissed
                    })
                    .create();
            mLogoutConfirmationDialog.show();
            // IMPORTANT: Do NOT call super.onBackPressed() here.
            // Showing the dialog means you are handling the back press.
        }
    }
}
