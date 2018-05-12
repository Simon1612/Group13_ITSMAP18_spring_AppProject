package com.itsmap.memoryapp.appprojektmemoryapp.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itsmap.memoryapp.appprojektmemoryapp.Models.NoteDataModel;
import com.itsmap.memoryapp.appprojektmemoryapp.R;

import java.util.HashMap;
import java.util.Map;

public class EditNotesActivity extends AppCompatActivity
implements OnMapReadyCallback {

    final static int CAMERA_REQUEST = 167;
    NoteDataModel noteData;
    Button editPictureBtn, OkBtn, CancelBtn;
    EditText NoteDescriptionText, NoteNameText;
    ImageView NotePictureImageView;

    FirebaseFirestore firebaseDb = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_notes_screen);

        Intent intent = getIntent();
        noteData = (NoteDataModel) intent.getSerializableExtra("noteData");
        //For test
        if(noteData == null) {
            noteData = new NoteDataModel("Invalid Note", "", 0, 0);
        }
        try{
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            if(noteData.getLocation() != null) {
                mapFragment.getMapAsync(EditNotesActivity.this);
            } else {
                noteData.setLocation(new LatLng(0,0));
                mapFragment.getMapAsync(EditNotesActivity.this);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }

        NoteDescriptionText = findViewById(R.id.NoteDescriptionText);
        NoteDescriptionText.setText(noteData.getDescription());

        NoteNameText = findViewById(R.id.NoteNameText);
        NoteNameText.setText(noteData.getName());

        NotePictureImageView = findViewById(R.id.NotePictureImageView);

        editPictureBtn = findViewById(R.id.ChangePictureBtn);
        editPictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(pictureIntent,CAMERA_REQUEST);
            }
        });


        OkBtn = findViewById(R.id.OkBtn);
        CancelBtn = findViewById(R.id.CancelBtn);

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

                Map<String, Object> note = new HashMap<String, Object>();
                note.put("Id", 123123); //Skal sættes til det samme som den Note der ønskes Redigeret
                note.put("Name", noteData.getName());
                note.put("Timestamp", noteData.getTimeStamp());
                note.put("Description", noteData.getDescription());
                note.put("Latitude", noteData.getLocation().latitude);
                note.put("Longtitude", noteData.getLocation().longitude);
                note.put("Creator", FirebaseAuth.getInstance().getCurrentUser());

                firebaseDb.collection("Notes")
                        .add(note)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d("DbUpdate", "New Note Added to Db");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("DbUpdate", "Error in adding new note to Db");
                    }
                });

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
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        LatLng currentLocation = new LatLng(noteData.getLocation().latitude, noteData.getLocation().longitude);
        googleMap.addMarker(new MarkerOptions().position(currentLocation)
                .title("New Note"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            NotePictureImageView.setImageBitmap(photo);
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
