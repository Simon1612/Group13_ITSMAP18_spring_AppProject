package com.itsmap.memoryapp.appprojektmemoryapp.Activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.itsmap.memoryapp.appprojektmemoryapp.BaseActivity;
import com.itsmap.memoryapp.appprojektmemoryapp.MemoryAppService;
import com.itsmap.memoryapp.appprojektmemoryapp.Models.NoteDataModel;
import com.itsmap.memoryapp.appprojektmemoryapp.NotesListAdapter;
import com.itsmap.memoryapp.appprojektmemoryapp.R;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import java.util.ArrayList;
import java.util.List;

public class ViewNotesActivity extends BaseActivity implements SwipyRefreshLayout.OnRefreshListener {

    ListView myNotesListView;
    NotesListAdapter myNotesListAdapter;
    List<NoteDataModel> myNotesList;

    boolean amIBound = false;
    MemoryAppService service;
    MemoryAppService.LocalBinder binder;
    Intent serviceIntent;
    Button testButton;
    String myNotesReady;
    SwipyRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_notes_screen);

        testButton = findViewById(R.id.testButton);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                service.updateMyNotes(1);
            }
        });

        refreshLayout = findViewById(R.id.myNotesSwipeRefreshLayout);
        refreshLayout.setOnRefreshListener(this);


        myNotesReady = getResources().getString(R.string.myNotesReady);

        myNotesListView = findViewById(R.id.viewNotesNotesList);
        myNotesList = new ArrayList<NoteDataModel>();
        myNotesListAdapter = new NotesListAdapter(this, myNotesList);

        myNotesListView.setAdapter(myNotesListAdapter);
        myNotesListView.setOnItemClickListener(myNotesListAdapter);

        //Register receiver for notesReady event
        IntentFilter filter = new IntentFilter();
        filter.addAction(myNotesReady);
        this.registerReceiver(notesBR, filter);

        serviceIntent = new Intent(this, MemoryAppService.class);
        bindService(serviceIntent, myServiceConnection, Context.BIND_AUTO_CREATE);
    }


    @Override
    public void onRefresh(SwipyRefreshLayoutDirection direction)
    {
        service.updateMyNotes(4); //Send command as intent instead?
    }

    private ServiceConnection myServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder serviceBinder){
            binder = (MemoryAppService.LocalBinder) serviceBinder;
            service = binder.getService();
            amIBound = true;

            service.startService(serviceIntent);
            myNotesList.addAll(service.getMyNotes());
            myNotesListAdapter.notifyDataSetChanged();
            //service.updateMyNotes(4);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            amIBound = false;
            unbindService(myServiceConnection);
        }
    };


    private BroadcastReceiver notesBR = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshLayout.setRefreshing(false);
            try{
                myNotesList.clear();
                myNotesList.addAll(service.getMyNotes());

                myNotesListAdapter.notifyDataSetChanged();
            }
            catch(Exception e){
                e.printStackTrace();
            }

        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbindService(myServiceConnection);
        unregisterReceiver(notesBR);
    }
}
