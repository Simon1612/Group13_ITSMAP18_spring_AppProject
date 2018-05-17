package com.itsmap.memoryapp.appprojektmemoryapp.Activities;

import android.Manifest;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.itsmap.memoryapp.appprojektmemoryapp.BaseActivity;
import com.itsmap.memoryapp.appprojektmemoryapp.LogIn.LogInScreen;
import com.itsmap.memoryapp.appprojektmemoryapp.MemoryAppService;
import com.itsmap.memoryapp.appprojektmemoryapp.Models.NoteDataModel;
import com.itsmap.memoryapp.appprojektmemoryapp.NotesListAdapter;
import com.itsmap.memoryapp.appprojektmemoryapp.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    final static int PERMISSIONS_REQUEST = 154;

    MemoryAppService service;
    MemoryAppService.LocalBinder binder;
    Button createQuicknoteButton, createNoteButton;
    ListView homescreenNotesListView;
    EditText quicknoteEdit;
    List<NoteDataModel> recentNotesList;
    Intent serviceIntent;
    String currentLocationReady, notesReady, textSnip, quicknoteText;
    NotesListAdapter notesListAdapter;
    LatLng location;
    boolean amIBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        boolean locationPermission =  getIntent().getExtras().getBoolean("locationPermission");
        boolean storageReadPermission = getIntent().getExtras().getBoolean("storageReadPermission");
        boolean storageWritePermission = getIntent().getExtras().getBoolean("storageWritePermission");

        if(!locationPermission) {
            if(!storageReadPermission || !storageWritePermission){
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST);

            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST);
            }
        } else if(!storageReadPermission || !storageWritePermission) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST);
        }

        currentLocationReady = getResources().getString(R.string.currentLocationReady);
        notesReady = getResources().getString(R.string.notesReady);

        createQuicknoteButton = findViewById(R.id.createQuicknoteButton);
        createNoteButton = findViewById(R.id.newNoteBtn);
        homescreenNotesListView = findViewById(R.id.homescreenNotesList);
        quicknoteEdit = findViewById(R.id.quicknoteEdit);

        recentNotesList = new ArrayList<NoteDataModel>();
        serviceIntent = new Intent(this, MemoryAppService.class);

        bindService(serviceIntent, myServiceConnection, Context.BIND_AUTO_CREATE);

        //Register receiver for currentLocationReady event
        IntentFilter locationFilter = new IntentFilter();
        locationFilter.addAction(currentLocationReady);
        this.registerReceiver(locationBR, locationFilter);

        //Register receiver for notesReady event
        IntentFilter filter = new IntentFilter();
        filter.addAction(notesReady);
        this.registerReceiver(notesBR, filter);

        //Set adapter for listView
        notesListAdapter = new NotesListAdapter(this, recentNotesList);
        homescreenNotesListView.setAdapter(notesListAdapter);
        homescreenNotesListView.setOnItemClickListener(notesListAdapter);

        createQuicknoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quicknoteText = quicknoteEdit.getText().toString();
                NoteDataModel tempNote;

                if(quicknoteText.length() > 10)
                    textSnip = String.format("%s...", quicknoteText.substring(0, 7));
                else
                    textSnip = quicknoteText;

                if(recentNotesList.size() >= 4)
                    recentNotesList.remove(0);

                if(location == null) {
                    location = new LatLng(0,0);
                }
                tempNote = new NoteDataModel(
                    "Quicknote: " + textSnip,
                    quicknoteText,
                    location.latitude, location.longitude, "");

                try{
                    recentNotesList.add(tempNote);
                    service.SaveNote(tempNote);
                }
                catch(Exception e){
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this,  getResources().getString(R.string.NoteCreationFailed), Toast.LENGTH_SHORT).show();
                }

                notesListAdapter.notifyDataSetChanged();
                quicknoteEdit.getText().clear();
            }

        });

        createNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createNoteIntent = new Intent(MainActivity.this, CreateNoteActivity.class);
                startActivity(createNoteIntent);
            }
        });
    }

    private ServiceConnection myServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder serviceBinder){
            binder = (MemoryAppService.LocalBinder) serviceBinder;
            service = binder.getService();
            amIBound = true;

            service.startService(serviceIntent);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            amIBound = false;
            unbindService(myServiceConnection);
        }
    };


    private BroadcastReceiver locationBR = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            //recentNotesList.addAll(service.getLastFourNotes());
            try{
                location = service.getCurrentLocation();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    };

    private BroadcastReceiver notesBR = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            //recentNotesList.addAll(service.getLastFourNotes());
            try{
                recentNotesList.clear();
                recentNotesList.addAll(service.getLastNotes());

                notesListAdapter.notifyDataSetChanged();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    };

    //Inspiration: https://developer.android.com/training/data-storage/shared-preferences.html#java
    @Override
    public void onDestroy() {
        super.onDestroy();

        unbindService(myServiceConnection);
        unregisterReceiver(locationBR);
        unregisterReceiver(notesBR);

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putBinder("Binder", binder);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        binder = (MemoryAppService.LocalBinder) savedInstanceState.getBinder("Binder");
        service = binder.getService();
    }
}
