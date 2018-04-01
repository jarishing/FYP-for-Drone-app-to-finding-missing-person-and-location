package com.example.finding;

import android.net.Uri;

/**
 * Created by jarishing on 6/11/2017.
 */

public class Spacecraft {
    private String name;
    private String droneLocationLat;
    private String droneLocationLng;
    private Uri uri;

    public String getName(){
        return name;
    }

    public void getName(String name) {
        this.name = name;
    }

    public String getdroneLocationLat(){
        return droneLocationLat;
    }

    public void getdroneLocationLat(String droneLocationLat){
        this.droneLocationLat = droneLocationLat;
    }

    public String getdroneLocationLng(){
        return droneLocationLng;
    }

    public void getdroneLocationLng(String droneLocationLng){
        this.droneLocationLng = droneLocationLng;
    }

    public Uri getUri(){
        return uri;
    }

    public void getUri(Uri uri){
        this.uri = uri;
    }
}

