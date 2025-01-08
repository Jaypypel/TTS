package com.example.neptune.ttsapp;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;


import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TTSAdminActivity extends AppCompatActivity {

    private String[] mNavigationDrawerItemTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    Toolbar toolbar;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ttsadmin);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mTitle = mDrawerTitle = getTitle();
        mNavigationDrawerItemTitles= getResources().getStringArray(R.array.navigation_drawer_items_array_admin);
        mDrawerLayout = findViewById(R.id.drawer_layout_admin);
        mDrawerList = findViewById(R.id.left_drawer_admin);


        setupToolbar();

        DataModelAdmin[] drawerItem = new DataModelAdmin[7];

        drawerItem[0] = new DataModelAdmin("Home");
        drawerItem[1] = new DataModelAdmin("Add Activity");
        drawerItem[2] = new DataModelAdmin("Add Project");
        drawerItem[3] = new DataModelAdmin("Add Task");
        drawerItem[4] = new DataModelAdmin("Add Measurable");
        drawerItem[5] = new DataModelAdmin("Add Other Activity");
        drawerItem[6] = new DataModelAdmin("Report Generation");


        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);

        DrawerItemCustomAdapterAdmin adapter = new DrawerItemCustomAdapterAdmin(this, R.layout.list_view_item_row_admin, drawerItem);
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerLayout = findViewById(R.id.drawer_layout_admin);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        setupDrawerToggle();

    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {

        Fragment fragment = null;
        Activity mainActivity = null;

        switch (position) {
            case 0:
                mainActivity = new TTSMainActivity();
                break;
            case 1:
                fragment = new TTSActivityCRUDFragment();
                break;
            case 2:
                fragment = new TTSProjectCRUDFragment();
                break;
            case 3:
                fragment = new TTSTaskCRUDFragment();
                break;
            case 4:
                fragment = new TTSMeasurableCRUDFragment();
                break;
            case 5:
                fragment = new TTSOtherActivityCRUDFragment();
                break;
            case 6:
                fragment = new TTSReportGenerationFragment();
                break;

            default:
                break;
        }

        if (mainActivity!=null)
        {
            Intent i = new Intent(TTSAdminActivity.this, mainActivity.getClass());
            startActivity(i);
            finish();
        }

        else if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame_admin, fragment).commit();

            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            setTitle(mNavigationDrawerItemTitles[position]);
            mDrawerLayout.closeDrawer(mDrawerList);

        } else {
            Log.e("AdminActivity", "Error in creating fragment");
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

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
        toolbar = (Toolbar) findViewById(R.id.toolbar_admin);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    void setupDrawerToggle(){
        mDrawerToggle = new androidx.appcompat.app.ActionBarDrawerToggle(this,mDrawerLayout,toolbar,R.string.admin_work, R.string.admin_work);
        //This is necessary to change the icon of the Drawer Toggle upon state change.
        mDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(getApplicationContext(), TTSMainActivity.class);
        startActivity(i);
        finish();
    }

}
