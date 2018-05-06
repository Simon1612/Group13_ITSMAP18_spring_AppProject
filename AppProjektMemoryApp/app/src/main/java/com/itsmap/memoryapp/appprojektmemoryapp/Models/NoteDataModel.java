package com.itsmap.memoryapp.appprojektmemoryapp.Models;

import java.io.Serializable;
import java.text.DateFormat;

public class NoteDataModel implements Serializable {

    public NoteDataModel(String _name, String _noteDescription, String _location) {
        TimeStamp = DateFormat.getInstance().format(System.currentTimeMillis());
        Name = _name;
        Description = _noteDescription;
        Location = _location;
    }

    private String Name;
    public void setName(String name) { Name = name; }
    public String getName() { return Name; }

    private String TimeStamp;
    public void setTimeStamp(String timeStamp) { this.TimeStamp = timeStamp; }
    public String getTimeStamp() { return TimeStamp; }

    private String Description;
    public void setDescription(String description) { this.Description = description; }
    public String getDescription() { return Description; }

    private String Location;
    public void setLocation(String location) { Location = location; }
    public String getLocation() { return Location; }
}
