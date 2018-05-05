package com.itsmap.memoryapp.appprojektmemoryapp;

import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import com.itsmap.memoryapp.appprojektmemoryapp.Models.NoteDataModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button createQuicknoteButton;
    ListView homescreenNotesList;
    EditText quicknoteEdit;
    CheckBox remindMeLaterCheckbox;
    List<NoteDataModel> recentNotesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        createQuicknoteButton = findViewById(R.id.createQuicknoteButton);
        homescreenNotesList = findViewById(R.id.homescreenNotesList);
        quicknoteEdit = findViewById(R.id.quicknoteEdit);
        remindMeLaterCheckbox = findViewById(R.id.remindMeLaterCheckbox);

        recentNotesList = new ArrayList<NoteDataModel>();

        createQuicknoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Save quicknote here

                if(recentNotesList.size() >= 4){
                    recentNotesList.remove(0);
                    //recentNotesList.add(new NoteDataModel(service.givmylocationtak, quicknoteEdit.getText().toString()));
                }
                else{
                    //recentNotesList.add(new NoteDataModel(service.givmylocationtak, quicknoteEdit.getText().toString()));
                }
            }
        });
    }
}
