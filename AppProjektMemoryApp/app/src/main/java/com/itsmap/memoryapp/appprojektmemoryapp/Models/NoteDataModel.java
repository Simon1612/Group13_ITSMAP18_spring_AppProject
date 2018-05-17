package com.itsmap.memoryapp.appprojektmemoryapp.Models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.text.DateFormat;

public class NoteDataModel implements Serializable{


    public NoteDataModel(String _name, String _noteDescription, double _latitude, double _longitude, String _imgBitmap) {
        TimeStamp = DateFormat.getInstance().format(System.currentTimeMillis());
        Name = _name;
        Description = _noteDescription;
        ImageBitmap = _imgBitmap;
        Location = new LatLng(_latitude, _longitude);
    }

    @JsonCreator
    public NoteDataModel(@JsonProperty("Name") String _name,
                         @JsonProperty("Description") String _noteDescription,
                         @JsonProperty("Latitude") double _latitude,
                         @JsonProperty("Longitude") double _longitude,
                         @JsonProperty("Timestamp") String _timeStamp,
                         @JsonProperty("ImageBitmap") String _imgBitmap){

        TimeStamp = _timeStamp;
        Name = _name;
        Description = _noteDescription;
        ImageBitmap = _imgBitmap;
        Location = new LatLng(_latitude, _longitude);
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

    private String ImageBitmap;
    public void setImageBitmap(String imageBitmap) {this.ImageBitmap = imageBitmap;}
    public String getImageBitmap() { return ImageBitmap; }

    private transient LatLng Location;
    public void setLocation(LatLng location) { Location = location; }
    public LatLng getLocation() { return Location; }
}