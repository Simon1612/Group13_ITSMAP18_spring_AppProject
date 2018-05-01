package com.itsmap.memoryapp.appprojektmemoryapp.Notes;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
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
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.itsmap.memoryapp.appprojektmemoryapp.Models.NoteDataModel;
import com.itsmap.memoryapp.appprojektmemoryapp.R;

import java.io.Serializable;
import java.security.Provider;

public class CreateNoteActivity extends AppCompatActivity {

final static int PERMISSIONS_REQUEST = 123;

private Boolean locationPermissionGranted;

Button OkBtn, CancelBtn, TakePictureBtn;
TextView LocationTextView, TimeStampTextView;
EditText NoteDescriptionText;
ImageView NotePictureImageView;

NoteDataModel noteData;

private FusedLocationProviderClient mFusedLocationClient;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        noteData = new NoteDataModel("", "");

        LocationTextView = findViewById(R.id.LocationTextView); //Ved ikke hvordan man får placering, men den skal sættes til brugerens nuværende

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            int hasPermission = this.checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION");
            if(hasPermission == 0) {
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    LocationTextView.setText(location.toString()); //Tjek Konverteringen
                                }
                            }
                        });
            }
            else {
            Toast.makeText(this, "You need to give permission in order for your location to be found", Toast.LENGTH_SHORT).show();
            String[] permissionArray = {"android.permission.ACCESS_COARSE_LOCATION"};
            this.requestPermissions(permissionArray, PERMISSIONS_REQUEST);
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
                noteData.setLocation(LocationTextView.getText().toString()); //ikke rigtigt
                noteData.setTimeStamp(TimeStampTextView.getText().toString());
                noteData.setNoteDescription(NoteDescriptionText.getText().toString());
                noteData.setPictureId(NotePictureImageView.getId());

                //Skal Desuden laves noget Database persistering
            }
        });

        TakePictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Skal gøre det muligt at tage et billede, som efterfølgende vises på aktiviteten i ImageView'et
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
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
                return;
            }

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("noteDataModel", (Serializable) noteData);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);

        noteData =(NoteDataModel) savedInstanceState.getSerializable("noteDataModel");
    }
}
