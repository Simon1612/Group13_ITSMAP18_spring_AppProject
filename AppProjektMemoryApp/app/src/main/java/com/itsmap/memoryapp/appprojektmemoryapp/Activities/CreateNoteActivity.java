package com.itsmap.memoryapp.appprojektmemoryapp.Activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.itsmap.memoryapp.appprojektmemoryapp.MemoryAppService;
import com.itsmap.memoryapp.appprojektmemoryapp.Models.NoteDataModel;
import com.itsmap.memoryapp.appprojektmemoryapp.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CreateNoteActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    final static int SET_MARKER_REQUEST = 234;
    final static int CAMERA_REQUEST = 167;
    LatLng location;
    String currentLocationReady;

    MemoryAppService.LocalBinder binder;
    Intent serviceIntent;
    MemoryAppService memoryAppService;
    Button OkBtn, CancelBtn, TakePictureBtn, ExpandMapBtn;
    TextView TimeStampTextView;
    EditText NoteDescriptionText, NameText;
    ImageView NotePictureImageView;
    NoteDataModel noteData;

    Boolean mBound = false;

    static String encoded;

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
        noteData = new NoteDataModel("", "", 0, 0, "");
        if(location != null) {
           noteData.setLocation(location);
        }
        if(encoded != null) {
            noteData.setImageBitmap(encoded);
        }

        TimeStampTextView = findViewById(R.id.TimeStampTextView);
        TimeStampTextView.setText(noteData.getTimeStamp().toString());


        NameText = findViewById(R.id.NoteNameText);
        NoteDescriptionText = findViewById(R.id.NoteDescriptionText);
        NotePictureImageView = findViewById(R.id.NotePictureImageView);

        String imgBitmap = noteData.getImageBitmap();
        if (imgBitmap != null) {
            if (!imgBitmap.isEmpty()) {
                byte[] decodedString = Base64.decode(imgBitmap.getBytes(), Base64.DEFAULT);
                Bitmap decodedImg = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                NotePictureImageView.setImageBitmap(decodedImg);
            }
        }

        OkBtn = findViewById(R.id.OkBtn);
        CancelBtn = findViewById(R.id.CancelBtn);
        TakePictureBtn = findViewById(R.id.TakePictureBtn);
        ExpandMapBtn = findViewById(R.id.ExpandMapBtn);

        CancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        OkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = NameText.getText().toString();
                if(!name.matches("")) {
                    noteData.setName(name);
                } else {
                    Toast.makeText(CreateNoteActivity.this, getResources().getString(R.string.NoteNameErrorTxt), Toast.LENGTH_SHORT).show();
                    return;
                }

                if(location != null) {
                    noteData.setLocation(location);
                } else {
                    noteData.setLocation(new LatLng(123, 123)); //random default-værdi
                }

                String description =  NoteDescriptionText.getText().toString();
                if(!description.matches("")) {
                    noteData.setDescription(description);
                } else {
                    Toast.makeText(CreateNoteActivity.this, getResources().getString(R.string.NoteDescriptionErrorTxt), Toast.LENGTH_SHORT).show();
                    noteData.setDescription(" ");
                }

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

        ExpandMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapActivityIntent = new Intent(CreateNoteActivity.this, MapActivity.class);
                        if(location != null) {
                            mapActivityIntent.putExtra("LocationLat", location.latitude);
                            mapActivityIntent.putExtra("LocationLong", location.longitude);
                        }
                startActivityForResult(mapActivityIntent, SET_MARKER_REQUEST);
            }
        });
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        LatLng markerLocation = new LatLng(location.latitude, location.longitude);
        googleMap.addMarker(new MarkerOptions().position(markerLocation)
                .title("New Note"));

        googleMap.setMinZoomPreference(15);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(markerLocation));

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

            memoryAppService.startService(serviceIntent);
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
        unbindService(memoryAppServiceConnection);
        unregisterReceiver(br);
        mBound = false;
        super.onDestroy();
    }

    //Baseret på stackoverflow: https://stackoverflow.com/questions/36117882/is-it-possible-to-store-image-to-firebase-in-android
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = data.getParcelableExtra("data");
            NotePictureImageView.setImageBitmap(photo);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
            noteData.setImageBitmap(encoded);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBinder("binder", binder);
        outState.putParcelable("Location", location);
        outState.putSerializable("noteDataModel", noteData);
        outState.putString("imageEncoded", encoded);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);

        binder = (MemoryAppService.LocalBinder) savedInstanceState.getBinder("binder");
        noteData = (NoteDataModel) savedInstanceState.getSerializable("noteDataModel");
        noteData.setLocation((LatLng) savedInstanceState.getParcelable("Location"));
        encoded = savedInstanceState.getString("imageEncoded");
    }
}
