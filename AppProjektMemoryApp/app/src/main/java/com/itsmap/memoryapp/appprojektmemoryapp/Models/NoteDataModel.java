package com.itsmap.memoryapp.appprojektmemoryapp.Models;

import java.io.Serializable;

public class NoteDataModel implements Serializable {

    public NoteDataModel(String _name, String _noteDescription) {
        TimeStamp = String.valueOf(System.currentTimeMillis());
        Name = _name;
        NoteDescription = _noteDescription;
    }

    private String Name;
    public void setName(String name) { Name = name; }
    public String getName() { return Name; }

    private String TimeStamp;
    public void setTimeStamp(String timeStamp) { this.TimeStamp = timeStamp; }
    public String getTimeStamp() { return TimeStamp; }

    private String NoteDescription;
    public void setNoteDescription(String noteDescription) { this.NoteDescription = noteDescription; }
    public String getNoteDescription() { return NoteDescription; }
}
