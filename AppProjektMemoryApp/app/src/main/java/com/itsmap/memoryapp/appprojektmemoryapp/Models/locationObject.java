package com.itsmap.memoryapp.appprojektmemoryapp.Models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class locationObject{
    @JsonCreator
    public locationObject (@JsonProperty("latitude") double _latitude, @JsonProperty("longitude") double _longitude) {
        latitude = _latitude;
        longitude = _longitude;
    }

    private double latitude;
    public void setLatitude(double _latitude){
        latitude = _latitude;
    }
    public double getLatitude(){
        return latitude;
    }

    private double longitude;
    public void setLongitude(double _longitude){
        longitude = _longitude;
    }
    public double getLongitude(){
        return longitude;
    }
}
