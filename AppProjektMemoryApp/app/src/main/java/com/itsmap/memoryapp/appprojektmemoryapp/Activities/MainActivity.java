package com.itsmap.memoryapp.appprojektmemoryapp.Activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.itsmap.memoryapp.appprojektmemoryapp.BaseActivity;
import com.itsmap.memoryapp.appprojektmemoryapp.MemoryAppService;
import com.itsmap.memoryapp.appprojektmemoryapp.Models.NoteDataModel;
import com.itsmap.memoryapp.appprojektmemoryapp.NotesListAdapter;
import com.itsmap.memoryapp.appprojektmemoryapp.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    MemoryAppService service;
    MemoryAppService.LocalBinder binder;
    Button createQuicknoteButton, createNoteButton, editNoteButton;
    ListView homescreenNotesListView;
    EditText quicknoteEdit;
    CheckBox remindMeLaterCheckbox;
    List<NoteDataModel> recentNotesList;
    Intent serviceIntent;
    String currentLocationReady, notesReady, textSnip, quicknoteText;
    NotesListAdapter notesListAdapter;
    LatLng location;
    boolean amIBound = false;

    final static int PERMISSIONS_REQUEST = 154;
    boolean locationPermission = false;
    boolean cameraPermission = false;
    boolean storageReadPermission = false;
    boolean storageWritePermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        checkForPermissions();
        if(locationPermission != true) {
            Toast.makeText(MainActivity.this, getResources().getString(R.string.LocationPermissionsEncourage), Toast.LENGTH_SHORT).show();
        }

        if(cameraPermission != true) {
            Toast.makeText(MainActivity.this, getResources().getString(R.string.CameraPermissionsEncourage), Toast.LENGTH_SHORT).show();
        }

        if(storageReadPermission && storageWritePermission != true) {
            Toast.makeText(MainActivity.this, getResources().getString(R.string.StoragePermissionsEncourage), Toast.LENGTH_SHORT).show();
        }

        currentLocationReady = getResources().getString(R.string.currentLocationReady);
        notesReady = getResources().getString(R.string.notesReady);

        createQuicknoteButton = findViewById(R.id.createQuicknoteButton);
        homescreenNotesListView = findViewById(R.id.homescreenNotesList);
        quicknoteEdit = findViewById(R.id.quicknoteEdit);
        remindMeLaterCheckbox = findViewById(R.id.remindMeLaterCheckbox);

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

                tempNote = new NoteDataModel(
                    "Quicknote: " + textSnip,
                    quicknoteText,
                    location.latitude, location.longitude);

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
        createNoteButton = findViewById(R.id.createNewNoteButton);
        createNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newNoteIntent = new Intent(MainActivity.this, CreateNoteActivity.class);
                startActivity(newNoteIntent);
            }
        });

        editNoteButton = findViewById(R.id.EditNote);
        editNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editNoteIntent = new Intent(MainActivity.this, EditNotesActivity.class);
                startActivity(editNoteIntent);
            }
        });
    }

    public void checkForPermissions() {
        if(ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                + ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CAMERA)
                + ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                + ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST);
        } else {
            locationPermission = true;
            cameraPermission = true;
            storageReadPermission = true;
            storageWritePermission = true;
            return;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                if(grantResults.length > 0) {
                    locationPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    cameraPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    storageReadPermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    storageWritePermission = grantResults[3] == PackageManager.PERMISSION_GRANTED;

                    if(locationPermission) {
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.LocationPermissionsSuccess), Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.LocationPermissionsFailed), Toast.LENGTH_SHORT).show();
                    }

                    if(cameraPermission) {
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.CameraPermissionsSuccess), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.CameraPermissionsFailed), Toast.LENGTH_SHORT).show();
                    }

                    if(storageReadPermission && storageWritePermission) {
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.StoragePermissionsSuccess), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.StoragePermissionsSuccess), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
        }
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

        /*SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("myCityPersisted", cityView.getText().toString());
        editor.commit();*/ //HVIS VI SKAL GEMME NOGET SHIT
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
