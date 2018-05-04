package com.itsmap.memoryapp.appprojektmemoryapp.Notes;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.itsmap.memoryapp.appprojektmemoryapp.MainActivity;
import com.itsmap.memoryapp.appprojektmemoryapp.Models.NoteDataModel;
import com.itsmap.memoryapp.appprojektmemoryapp.R;

import java.io.Serializable;

public class EditNotesActivity extends AppCompatActivity {

NoteDataModel noteData;
Button editPictureBtn, OkBtn, CancelBtn;
EditText NoteDescriptionText, LocationEditText, TimeStampEditText;
ImageView NotePictureImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_notes_screen);

        noteData = new NoteDataModel("Model To Edit", ""); //Skal sættes til den note der ønskes redigeret

        NoteDescriptionText = findViewById(R.id.NoteDescriptionText);
        NoteDescriptionText.setText(noteData.getNoteDescription());

        LocationEditText = findViewById(R.id.LocationEditText);
        LocationEditText.setText(noteData.getLocation());

        TimeStampEditText = findViewById(R.id.TimeStampEditText);
        TimeStampEditText.setText(noteData.getTimeStamp());

        NotePictureImageView = findViewById(R.id.NotePictureImageView);
        NotePictureImageView.setId(noteData.getPictureId());

        editPictureBtn = findViewById(R.id.ChangePictureBtn);
        editPictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    noteData.setLocation(location);
                } else {
                    Toast.makeText(EditNotesActivity.this, "You need to set the Location", Toast.LENGTH_SHORT).show();
                }

                String noteDescription = NoteDescriptionText.getText().toString();
                noteData.setNoteDescription(noteDescription);

                int pictureId = NotePictureImageView.getId();
                noteData.setPictureId(pictureId);

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
