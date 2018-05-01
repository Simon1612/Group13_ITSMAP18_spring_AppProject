package com.itsmap.memoryapp.appprojektmemoryapp.Models;

import android.location.Location;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class NoteDataModel {

    public NoteDataModel(String location, String noteDescription) {
        Location = location;
        TimeStamp = String.valueOf(System.currentTimeMillis());
        NoteDescription = noteDescription;
        //PictureId = pictureId; Skal nok have et unikt ID til billedet i Db går jeg ud fra
        NoteId = 1; //Finde en måde at Auto-generes således at note får et unikt ID.
        Creator = FirebaseAuth.getInstance().getCurrentUser(); //Ingen Anelse om det her fungerer, men kunne være Ez
    }

    private int NoteId; //Skal ikke være muligt at tilgå NoteId, da det kun skal bruges som Primary ID i Db.

    public String Location; //Hvilken type er Location??
    public void setLocation(String location) {
        Location = location;
    }

    public String getLocation() {
        return Location;
    }

    public String TimeStamp;
    public void setTimeStamp(String timeStamp) {
        TimeStamp = timeStamp; //Nok unødvendig, kan også gøres som nedenstående ellers
        //TimeStamp = System.currentTimeMillis();
    }

    public String getTimeStamp() {
        return TimeStamp;
    }

    public String NoteDescription;
    public void setNoteDescription(String noteDescription) {
        NoteDescription = noteDescription;
    }

    public String getNoteDescription() {
        return NoteDescription;
    }

    public int PictureId;

    public void setPictureId(int pictureId) {
        PictureId = pictureId;
    }

    public int getPictureId() {
        return PictureId;
    }

    public FirebaseUser Creator;
    public void setCreator(FirebaseUser creator) {
        Creator = creator; //Højst sandsynligt også unødvendigt, hvorfor skulle man ændre creator af en note?
    }

    public FirebaseUser getCreator() {
        return Creator;
    }
}
