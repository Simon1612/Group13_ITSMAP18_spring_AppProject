package com.itsmap.memoryapp.appprojektmemoryapp.Activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
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
    Button createQuicknoteButton;
    ListView homescreenNotesListView;
    EditText quicknoteEdit;
    CheckBox remindMeLaterCheckbox;
    List<NoteDataModel> recentNotesList;
    Intent serviceIntent;
    String currentLocationReady, textSnip, quicknoteText;
    NotesListAdapter notesListAdapter;
    boolean amIBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        currentLocationReady = getResources().getString(R.string.currentLocationReady);

        createQuicknoteButton = findViewById(R.id.createQuicknoteButton);
        homescreenNotesListView = findViewById(R.id.homescreenNotesList);
        quicknoteEdit = findViewById(R.id.quicknoteEdit);
        remindMeLaterCheckbox = findViewById(R.id.remindMeLaterCheckbox);

        recentNotesList = new ArrayList<NoteDataModel>();
        serviceIntent = new Intent(this, MemoryAppService.class);

        bindService(serviceIntent, myServiceConnection, Context.BIND_AUTO_CREATE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(currentLocationReady);
        this.registerReceiver(br, filter);

        //Set adapter for listView
        notesListAdapter = new NotesListAdapter(this, recentNotesList);
        homescreenNotesListView.setAdapter(notesListAdapter);
        homescreenNotesListView.setOnItemClickListener(notesListAdapter);

        createQuicknoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quicknoteText = quicknoteEdit.getText().toString();

                if(quicknoteText.length() > 10)
                    textSnip = String.format("%s...", quicknoteText.substring(0, 7));
                else
                    textSnip = quicknoteText;

                if(recentNotesList.size() >= 4){
                    recentNotesList.remove(0);
                    recentNotesList.add(new NoteDataModel(
                            "Quicknote: " + textSnip,
                            quicknoteText,
                            service.getCurrentLocation()));

                    notesListAdapter.notifyDataSetChanged();
                    quicknoteEdit.getText().clear();
                }
                else{
                    recentNotesList.add(new NoteDataModel(
                            "Quicknote: " + textSnip,
                            quicknoteText,
                            service.getCurrentLocation()));

                    notesListAdapter.notifyDataSetChanged();
                    quicknoteEdit.getText().clear();
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
            recentNotesList.addAll(service.getLastFourNotes());
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
