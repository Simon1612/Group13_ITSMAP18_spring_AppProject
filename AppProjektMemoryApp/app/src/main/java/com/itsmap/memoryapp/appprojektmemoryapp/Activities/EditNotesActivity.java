package com.itsmap.memoryapp.appprojektmemoryapp.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.itsmap.memoryapp.appprojektmemoryapp.Models.NoteDataModel;
import com.itsmap.memoryapp.appprojektmemoryapp.R;

import java.io.Serializable;

import static com.itsmap.memoryapp.appprojektmemoryapp.Notes.CreateNoteActivity.CAMERA_PERMISSIONS_REQUEST;

public class EditNotesActivity extends AppCompatActivity {

NoteDataModel noteData;
Button editPictureBtn, OkBtn, CancelBtn;
EditText NoteDescriptionText, LocationEditText, TimeStampEditText;
ImageView NotePictureImageView;

Boolean cameraPermissionGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_notes_screen);

        Intent intent = getIntent();
        noteData = (NoteDataModel) intent.getSerializableExtra("noteData");

        NoteDescriptionText = findViewById(R.id.NoteDescriptionText);
        NoteDescriptionText.setText(noteData.getDescription());

        NotePictureImageView = findViewById(R.id.NotePictureImageView);

        editPictureBtn = findViewById(R.id.ChangePictureBtn);
        editPictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkCallingOrSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSIONS_REQUEST);
                } else {
                    Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(pictureIntent, CAMERA_PERMISSIONS_REQUEST);
                }

                //Billede skal kunne opdateres og vises på aktiviteten i ImageView'et
            }
        });


        OkBtn = findViewById(R.id.OkBtn);
        CancelBtn = findViewById(R.id.CancelBtn);

        OkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String timeStamp = TimeStampEditText.getText().toString();
                if(timeStamp != "") {
                    noteData.setTimeStamp(timeStamp);
                }
                else {
                    Toast.makeText(EditNotesActivity.this, "You need to set the Timestamp", Toast.LENGTH_SHORT).show();
                }

                String location = LocationEditText.getText().toString();
                if(location != "") {
                   // noteData.setLocation(location);
                } else {
                    Toast.makeText(EditNotesActivity.this, "You need to set the Location", Toast.LENGTH_SHORT).show();
                }

                String noteDescription = NoteDescriptionText.getText().toString();
                noteData.setDescription(noteDescription);

                int pictureId = NotePictureImageView.getId();
               // noteData.setPictureId(pictureId);

                //Skal laves noget Db-operation til at gemme noteModellen ned

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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
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
        outState.putSerializable("noteDataModel", (Serializable) noteData);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
        noteData = (NoteDataModel) savedInstanceState.getSerializable("noteDataModel");
    }
}
