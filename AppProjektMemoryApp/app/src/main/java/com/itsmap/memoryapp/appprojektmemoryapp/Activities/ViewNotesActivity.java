package com.itsmap.memoryapp.appprojektmemoryapp.Activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.itsmap.memoryapp.appprojektmemoryapp.BaseActivity;
import com.itsmap.memoryapp.appprojektmemoryapp.MemoryAppService;
import com.itsmap.memoryapp.appprojektmemoryapp.Models.NoteDataModel;
import com.itsmap.memoryapp.appprojektmemoryapp.NotesListAdapter;
import com.itsmap.memoryapp.appprojektmemoryapp.R;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;

import java.util.ArrayList;
import java.util.List;

public class ViewNotesActivity extends BaseActivity{

    SwipeMenuListView myNotesListView;
    NotesListAdapter myNotesListAdapter;
    List<NoteDataModel> myNotesList;

    boolean amIBound = false;
    MemoryAppService service;
    MemoryAppService.LocalBinder binder;
    Intent serviceIntent;
    Button createNoteButton;
    String myNotesReady;
    SwipyRefreshLayout refreshLayout;

    private static final int CreateNoteReqCode = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_notes_screen);

        createNoteButton = findViewById(R.id.myNotesCreateButton);
        createNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createNoteIntent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                startActivityForResult(createNoteIntent, CreateNoteReqCode);
            }
        });

        refreshLayout = findViewById(R.id.myNotesSwipeRefreshLayout);
        refreshLayout.setEnabled(false);

        myNotesReady = getResources().getString(R.string.myNotesReady);

        myNotesListView = findViewById(R.id.viewNotesNotesList);
        myNotesList = new ArrayList<NoteDataModel>();
        myNotesListAdapter = new NotesListAdapter(this, myNotesList);

        myNotesListView.setAdapter(myNotesListAdapter);
        myNotesListView.setOnItemClickListener(myNotesListAdapter);
        myNotesListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch(index){
                    case 0:
                        service.deleteNote(myNotesList.get(position));
                        myNotesList.remove(position);
                        myNotesListAdapter.notifyDataSetChanged();
                }
                return false; //Hides the swipe menu
            }
        });

        //https://stackoverflow.com/questions/28200309/how-to-implement-refresh-listview-and-bring-new-items-in-list-if-it-reach-end-of
        myNotesListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            int lastVisibleItem;
            int totalItemCount;
            boolean isEndOfList;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            //do nothing
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                this.totalItemCount = totalItemCount;
                this.lastVisibleItem = firstVisibleItem + visibleItemCount - 1;
                // prevent checking on short lists
                if (totalItemCount > visibleItemCount)
                    checkEndOfList();
            }

            private synchronized void checkEndOfList() {
                // trigger after 2nd to last item
                if (lastVisibleItem >= (totalItemCount - 2)) {
                    if (!isEndOfList) {
                        service.updateMyNotes(10);
                        refreshLayout.setEnabled(true);
                        refreshLayout.setRefreshing(true);
                    }
                    isEndOfList = true;
                } else {
                    isEndOfList = false;
                }
            }
        });

        setupSwipeMenu();


        //Register receiver for myNotesReady event
        IntentFilter filter = new IntentFilter();
        filter.addAction(myNotesReady);
        this.registerReceiver(notesBR, filter);

        serviceIntent = new Intent(this, MemoryAppService.class);
        bindService(serviceIntent, myServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CreateNoteReqCode)
        {
            if (resultCode == RESULT_OK)
            {
                if (data.getExtras() != null)
                {
                    NoteDataModel tempModel = (NoteDataModel) data.getSerializableExtra("noteDataModel");
                    tempModel.setLocation(data.getParcelableExtra("location"));

                    myNotesList.add(tempModel);

                    myNotesListAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private void setupSwipeMenu(){

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                deleteItem.setBackground(new ColorDrawable(Color.rgb(255,
                        0, 0)));
                deleteItem.setWidth(180);
                deleteItem.setIcon(R.drawable.ic_delete_black);
                menu.addMenuItem(deleteItem);
            }
        };
        myNotesListView.setMenuCreator(creator);
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
            refreshLayout.setEnabled(false);
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
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        binder = (MemoryAppService.LocalBinder) savedInstanceState.getBinder("Binder");
        service = binder.getService();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBinder("Binder", binder);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbindService(myServiceConnection);
        unregisterReceiver(notesBR);
    }
}
