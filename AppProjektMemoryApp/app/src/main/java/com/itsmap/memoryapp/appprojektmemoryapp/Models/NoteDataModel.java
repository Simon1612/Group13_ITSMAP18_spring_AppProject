package com.itsmap.memoryapp.appprojektmemoryapp.Models;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.text.DateFormat;

public class NoteDataModel implements Serializable {

    public NoteDataModel(String _name, String _noteDescription, double _latitude, double _longtitude) {
        TimeStamp = DateFormat.getInstance().format(System.currentTimeMillis());
        Name = _name;
        Description = _noteDescription;

        Location = new LatLng(_latitude, _longtitude);
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

    /*private Location Location;
    public void setLocation(Location location) { Location = location; }
    public Location getLocation() { return Location; }*/

    private LatLng Location;
    public void setLocation(LatLng location) { Location = location; }
    public LatLng getLocation() { return Location; }
}
