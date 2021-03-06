package com.itsmap.memoryapp.appprojektmemoryapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.itsmap.memoryapp.appprojektmemoryapp.Activities.MainActivity;
import com.itsmap.memoryapp.appprojektmemoryapp.Activities.ViewNotesActivity;
import com.itsmap.memoryapp.appprojektmemoryapp.LogIn.ProfileScreen;

//http://www.ottodroid.net/?p=501
public class BaseActivity extends AppCompatActivity {

    Toolbar toolbar;
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    ConstraintLayout constraintLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        drawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_base, null);
        constraintLayout = drawerLayout.findViewById(R.id.activity_content);
        navigationView = drawerLayout.findViewById(R.id.navigationViewBase);

        toolbar = constraintLayout.findViewById(R.id.toolbarBase);

        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_black);
    }

    @Override
    public void setContentView(int layoutResId)
    {
        getLayoutInflater().inflate(layoutResId, constraintLayout, true);
        super.setContentView(drawerLayout);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId())
                {
                    case R.id.HomeItem:
                        Intent homeIntent = new Intent(getApplicationContext(), MainActivity.class);
                        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(homeIntent);
                        break;

                    case R.id.NotesItem:
                        Intent notesIntent = new Intent(getApplicationContext(), ViewNotesActivity.class);
                        notesIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(notesIntent);
                        break;

                    case R.id.ProfileItem:
                        Intent profileIntent = new Intent(getApplicationContext(), ProfileScreen.class);
                        profileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(profileIntent);
                        break;

                }
                drawerLayout.closeDrawers();
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
