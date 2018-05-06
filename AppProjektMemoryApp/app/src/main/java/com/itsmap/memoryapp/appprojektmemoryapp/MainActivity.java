package com.itsmap.memoryapp.appprojektmemoryapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;


import com.itsmap.memoryapp.appprojektmemoryapp.Models.NoteDataModel;
import com.itsmap.memoryapp.appprojektmemoryapp.Notes.ViewNotesActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    MemoryAppService service;
    MemoryAppService.LocalBinder binder;
    Button createQuicknoteButton;
    ListView homescreenNotesList;
    EditText quicknoteEdit;
    CheckBox remindMeLaterCheckbox;
    List<NoteDataModel> recentNotesList;
    Intent serviceIntent;
    String currentLocationReady;
    boolean amIBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        currentLocationReady = getResources().getString(R.string.currentLocationReady);

        createQuicknoteButton = findViewById(R.id.createQuicknoteButton);
        homescreenNotesList = findViewById(R.id.homescreenNotesList);
        quicknoteEdit = findViewById(R.id.quicknoteEdit);
        remindMeLaterCheckbox = findViewById(R.id.remindMeLaterCheckbox);

        recentNotesList = new ArrayList<NoteDataModel>();
        serviceIntent = new Intent(this, MemoryAppService.class);

        bindService(serviceIntent, myServiceConnection, Context.BIND_AUTO_CREATE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(currentLocationReady);
        this.registerReceiver(br, filter);

        createQuicknoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Save quicknote here

                if(recentNotesList.size() >= 4){
                    recentNotesList.remove(0);
                    //recentNotesList.add(new NoteDataModel(service.givmylocationtak, quicknoteEdit.getText().toString()));
                }
                else{
                    //recentNotesList.add(new NoteDataModel(service.givmylocationtak, quicknoteEdit.getText().toString()));
                }
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

    //Inspiration from: https://developer.android.com/guide/components/broadcasts.html
    private BroadcastReceiver br = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            //throw new Exception();
        }
    };

    //Inspiration: https://developer.android.com/training/data-storage/shared-preferences.html#java
    @Override
    public void onDestroy() {
        super.onDestroy();

        unbindService(myServiceConnection);
        unregisterReceiver(br);

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
