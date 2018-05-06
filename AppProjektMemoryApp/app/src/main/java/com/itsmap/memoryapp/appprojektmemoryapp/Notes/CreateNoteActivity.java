package com.itsmap.memoryapp.appprojektmemoryapp.Notes;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itsmap.memoryapp.appprojektmemoryapp.MainActivity;
import com.itsmap.memoryapp.appprojektmemoryapp.MemoryAppService;
import com.itsmap.memoryapp.appprojektmemoryapp.Models.NoteDataModel;
import com.itsmap.memoryapp.appprojektmemoryapp.R;

import java.io.Serializable;

public class CreateNoteActivity extends AppCompatActivity {

final static int LOCATION_PERMISSIONS_REQUEST = 123;
final static int CAMERA_PERMISSIONS_REQUEST = 124;
private Boolean locationPermissionGranted;
private Boolean cameraPermissionGranted;
private String location;

MemoryAppService.LocalBinder binder;
Intent bindingIntent;
MemoryAppService memoryAppService;
Boolean mBound = false;

ServiceConnection memoryAppServiceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        binder = (MemoryAppService.LocalBinder) service;
        memoryAppService =  binder.getService();
        mBound = true;

        location = memoryAppService.getLocation();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mBound = false;
    }
};

Button OkBtn, CancelBtn, TakePictureBtn;
TextView LocationTextView, TimeStampTextView;
EditText NoteDescriptionText;
ImageView NotePictureImageView;

NoteDataModel noteData;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);
        bindingIntent = new Intent(this, MemoryAppService.class);
        bindService(bindingIntent, memoryAppServiceConnection, Context.BIND_AUTO_CREATE);
        startService(bindingIntent);

        LocationTextView = findViewById(R.id.LocationTextView);
        if(location != "Permission Error") {
            LocationTextView.setText(location);
        } else {
            this.requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},LOCATION_PERMISSIONS_REQUEST);
        }

        TimeStampTextView = findViewById(R.id.TimeStampTextView);
        TimeStampTextView.setText(noteData.getTimeStamp().toString());

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

        OkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteData.setNoteDescription(NoteDescriptionText.getText().toString());

                FirebaseFirestore.getInstance().collection("Notes")
                        .add(noteData);
                //Skal Desuden laves noget Database persistering
                Intent mainActivityIntent = new Intent(CreateNoteActivity.this, MainActivity.class);
                CreateNoteActivity.this.startActivity(mainActivityIntent);

            }
        });

        TakePictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkCallingOrSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSIONS_REQUEST);
                } else {
                    Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(pictureIntent, CAMERA_PERMISSIONS_REQUEST);
                }
                //Skal gøre det muligt at tage et billede, som efterfølgende vises på aktiviteten i ImageView'et
            }
        });
    }

    @Override
    protected void onDestroy() {
        //save shared preferences
        unbindService(memoryAppServiceConnection);
        mBound = false;
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        locationPermissionGranted = true;
                    }

                } else {
                    locationPermissionGranted = false;
                }
                break;
            }
            case CAMERA_PERMISSIONS_REQUEST: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cameraPermissionGranted = true;
                    Intent pictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(pictureIntent, CAMERA_PERMISSIONS_REQUEST);
                } else {
                    cameraPermissionGranted = false;
                }

                break;
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_PERMISSIONS_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            NotePictureImageView.setImageBitmap(photo);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("binder", (Serializable) binder);
        outState.putSerializable("noteDataModel", (Serializable) noteData);
        outState.putString("location", location);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);

        binder = (MemoryAppService.LocalBinder) savedInstanceState.getSerializable("binder");
        noteData =(NoteDataModel) savedInstanceState.getSerializable("noteDataModel");
        location = savedInstanceState.getString("location");
    }
}
