package com.itsmap.memoryapp.appprojektmemoryapp.Activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itsmap.memoryapp.appprojektmemoryapp.MemoryAppService;
import com.itsmap.memoryapp.appprojektmemoryapp.Models.NoteDataModel;
import com.itsmap.memoryapp.appprojektmemoryapp.R;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class EditNotesActivity extends AppCompatActivity
implements OnMapReadyCallback {

    final static int SET_MARKER_REQUEST = 234;
    final static int CAMERA_REQUEST = 167;
    NoteDataModel noteData;
    Button editPictureBtn, OkBtn, CancelBtn, ExpandMapBtn;
    EditText NoteDescriptionText, NoteNameText;
    ImageView NotePictureImageView;

    MemoryAppService.LocalBinder binder;
    Intent serviceIntent;
    MemoryAppService memoryAppService;
    Boolean mBound = false;

    LatLng location;
    String oldNoteName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_notes_screen);

        serviceIntent = new Intent(this, MemoryAppService.class);
        bindService(serviceIntent, memoryAppServiceConnection, Context.BIND_AUTO_CREATE);

        Intent intent = getIntent();
        noteData = (NoteDataModel) intent.getSerializableExtra("noteData");
        noteData.setLocation(new LatLng(intent.getDoubleExtra("LocationLat", 0), intent.getDoubleExtra("LocationLong", 0)));
        //For test
        if(noteData == null) {
            noteData = new NoteDataModel("Invalid Note", "", 0, 0,"");
        }

        oldNoteName = noteData.getName();
        location = noteData.getLocation();

        NoteDescriptionText = findViewById(R.id.NoteDescriptionText);
        NoteDescriptionText.setText(noteData.getDescription());

        NoteNameText = findViewById(R.id.NoteNameText);
        NoteNameText.setText(noteData.getName());

        NotePictureImageView = findViewById(R.id.NotePictureImageView);
        String imageBitmap = noteData.getImageBitmap();
        if(imageBitmap != null) {
            if(!imageBitmap.isEmpty()) {
                byte[] decodedString = Base64.decode(noteData.getImageBitmap().getBytes(), Base64.DEFAULT);
                Bitmap decodedImg = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                NotePictureImageView.setImageBitmap(decodedImg);
            }
        }

        editPictureBtn = findViewById(R.id.ChangePictureBtn);
        OkBtn = findViewById(R.id.OkBtn);
        CancelBtn = findViewById(R.id.CancelBtn);
        ExpandMapBtn = findViewById(R.id.ExpandMapBtn);

        editPictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(pictureIntent,CAMERA_REQUEST);
            }
        });

        OkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = NoteNameText.getText().toString();
                if(name != "") {
                    noteData.setName(name);
                }
                else {
                    Toast.makeText(EditNotesActivity.this, "You need to set the Name", Toast.LENGTH_SHORT).show();
                }

                String noteDescription = NoteDescriptionText.getText().toString();
                noteData.setDescription(noteDescription);

                memoryAppService.UpdateNote(noteData, oldNoteName);

                Intent mainActivityIntent = new Intent(EditNotesActivity.this, MainActivity.class);
                EditNotesActivity.this.startActivity(mainActivityIntent);
            }
        });

        CancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ExpandMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapActivityIntent = new Intent(EditNotesActivity.this, MapActivity.class);
                if(location != null) {
                    mapActivityIntent.putExtra("LocationLat", location.latitude);
                    mapActivityIntent.putExtra("LocationLong", location.longitude);
                }
                startActivityForResult(mapActivityIntent, SET_MARKER_REQUEST);
            }
        });
    }

    ServiceConnection memoryAppServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (MemoryAppService.LocalBinder) service;
            memoryAppService = binder.getService();
            mBound = true;

            try{
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                if(location != null) {
                    mapFragment.getMapAsync(EditNotesActivity.this);
                } else {
                    noteData.setLocation(new LatLng(0,0));
                    mapFragment.getMapAsync(EditNotesActivity.this);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(memoryAppServiceConnection);
        mBound = false;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        LatLng markerLocation = location;
        googleMap.addMarker(new MarkerOptions().position(markerLocation)
                .title("New Note"));
        googleMap.setMinZoomPreference(15);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(markerLocation));
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            NotePictureImageView.setImageBitmap(photo);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
            noteData.setImageBitmap(encoded);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("noteDataModel", noteData);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
        noteData = (NoteDataModel) savedInstanceState.getSerializable("noteDataModel");
    }
}
