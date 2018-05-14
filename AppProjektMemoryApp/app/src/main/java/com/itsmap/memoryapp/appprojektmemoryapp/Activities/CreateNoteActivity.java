package com.itsmap.memoryapp.appprojektmemoryapp.Activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itsmap.memoryapp.appprojektmemoryapp.MemoryAppService;
import com.itsmap.memoryapp.appprojektmemoryapp.Models.NoteDataModel;
import com.itsmap.memoryapp.appprojektmemoryapp.R;

import java.util.HashMap;
import java.util.Map;

public class CreateNoteActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    final static int CAMERA_REQUEST = 167;
    private LatLng location;
    String currentLocationReady;

    MemoryAppService.LocalBinder binder;
    Intent serviceIntent;
    MemoryAppService memoryAppService;
    Button OkBtn, CancelBtn, TakePictureBtn;
    TextView TimeStampTextView;
    EditText NoteDescriptionText, NameText;
    ImageView NotePictureImageView;
    NoteDataModel noteData;

    Boolean mBound = false;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        currentLocationReady = getResources().getString(R.string.currentLocationReady);
        serviceIntent = new Intent(this, MemoryAppService.class);
        bindService(serviceIntent, memoryAppServiceConnection, Context.BIND_AUTO_CREATE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(currentLocationReady);
        this.registerReceiver(br, filter);
        startService(serviceIntent);

        noteData = new NoteDataModel("", "", 0, 0);

        TimeStampTextView = findViewById(R.id.TimeStampTextView);
        TimeStampTextView.setText(noteData.getTimeStamp().toString());

        NameText = findViewById(R.id.NoteNameText);
        NoteDescriptionText = findViewById(R.id.NoteDescriptionText);
        NotePictureImageView = findViewById(R.id.NotePictureImageView);

        OkBtn = findViewById(R.id.OkBtn);
        CancelBtn = findViewById(R.id.CancelBtn);
        TakePictureBtn = findViewById(R.id.TakePictureBtn);

        CancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Baseret på FirebaseFirestore dokumentation
        OkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteData.setName(NameText.getText().toString());
                noteData.setLocation(location);
                noteData.setDescription(NoteDescriptionText.getText().toString());

                memoryAppService.SaveNote(noteData);

                Intent mainActivityIntent = new Intent(CreateNoteActivity.this, MainActivity.class);
                startActivity(mainActivityIntent);

            }
        });

        TakePictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(pictureIntent, CAMERA_REQUEST);
            }
        });
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        BitmapDescriptor markerImg = BitmapDescriptorFactory.fromResource(R.drawable.note_image);
        LatLng currentLocation = new LatLng(location.latitude, location.longitude);
        googleMap.addMarker(new MarkerOptions().position(currentLocation)
                .title("New Note")
                .icon(markerImg));

        googleMap.setMinZoomPreference(15);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));

        }

    ServiceConnection memoryAppServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (MemoryAppService.LocalBinder) service;
            memoryAppService =  binder.getService();
            mBound = true;

            try{
                location = memoryAppService.getCurrentLocation();
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                if(location != null) {
                    mapFragment.getMapAsync(CreateNoteActivity.this);
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            unbindService(memoryAppServiceConnection);
            mBound = false;
        }
    };

    private BroadcastReceiver br = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            try{
                location = memoryAppService.getCurrentLocation();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onDestroy() {
        //save shared preferences
        unregisterReceiver(br);
        mBound = false;
        super.onDestroy();
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = data.getParcelableExtra("data");
            NotePictureImageView.setImageBitmap(photo);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBinder("binder", binder);
        outState.putSerializable("noteDataModel", noteData);
        outState.putParcelable("Location", location);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);

        binder = (MemoryAppService.LocalBinder) savedInstanceState.getBinder("binder");
        noteData =(NoteDataModel) savedInstanceState.getSerializable("noteDataModel");
        noteData.setLocation((LatLng) savedInstanceState.getParcelable("Location"));
    }
}
