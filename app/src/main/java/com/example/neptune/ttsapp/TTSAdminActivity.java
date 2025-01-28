package com.example.neptune.ttsapp;



import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;


import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.material.navigation.NavigationView;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TTSAdminActivity extends AppCompatActivity {


    private DrawerLayout mDrawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private AppBarConfiguration appBarConfiguration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ttsadmin);


        mDrawerLayout = findViewById(R.id.drawer_layout_admin);
        toolbar = findViewById(R.id.toolbar_admin);
        navigationView = findViewById(R.id.nav_admin_view);

        //set up the toolbar
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        //set up the navigation drawer
        setUpNavigationDrawer();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.admin_nav_host_fragment);
        if(navHostFragment != null){
            NavController navController = navHostFragment.getNavController();
            appBarConfiguration = new AppBarConfiguration.
                    Builder(
                        R.id.nav_home,
                        R.id.nav_activity,
                        R.id.nav_project,
                        R.id.nav_task,
                        R.id.nav_measurable,
                        R.id.nav_other_activity,
                        R.id.nav_report_generation
                    ).
                    setOpenableLayout(mDrawerLayout).
                    build();
            NavigationUI.setupActionBarWithNavController(this,navController,appBarConfiguration);
            NavigationUI.setupWithNavController(navigationView,navController);
            setUpNavigationItemListener(navController);

        }


    }

    private void setUpNavigationItemListener(NavController navController) {
        navigationView.setNavigationItemSelectedListener(item -> {
            if(item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(this,TTSMainActivity.class));
                mDrawerLayout.closeDrawer(GravityCompat.START);
            }
            boolean selectedItem = NavigationUI.onNavDestinationSelected(item,navController);
            if (selectedItem){
                mDrawerLayout.closeDrawer(GravityCompat.START);
            }
            return selectedItem;
        });
    }

    public void setUpNavigationDrawer(){
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this
                ,mDrawerLayout,R.string.navigation_drawer_open,R.string.navigation_drawer_close) {
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
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation
                .findNavController(this, R.id.admin_nav_host_fragment);
        return NavigationUI
                .navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }
}
