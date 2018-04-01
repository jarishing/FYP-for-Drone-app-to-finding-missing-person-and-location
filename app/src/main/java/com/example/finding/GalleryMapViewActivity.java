package com.example.finding;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by jarishing on 6/11/2017.
 */

public class GalleryMapViewActivity extends AppCompatActivity implements GoogleMap.OnMapClickListener, OnMapReadyCallback {
    private static final String TAG = "MapView";
    String Data_Lat,Data_Lng;
    double Drone_Lat,Drone_Lng;

    private GoogleMap Map_View_Map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_map_view);

        Data_Lat = getIntent().getStringExtra("Drone_Location_Lat");
        Data_Lng = getIntent().getStringExtra("Drone_Location_Lng");

        try {
            Drone_Lat = Double.parseDouble(Data_Lat);
            Drone_Lng = Double.parseDouble(Data_Lng);
        } catch (NumberFormatException e) {
        }

        Log.d(TAG,"Lat ="+Drone_Lat+", Lng = "+Drone_Lng);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.Drone_location);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // TODO Auto-generated method stub
        // Initializing Amap object
        if (Map_View_Map == null) {
            Map_View_Map = googleMap;
            setUpMap();
        }
        LatLng cityu = new LatLng(Drone_Lat, Drone_Lng);
        float zoomlevel = (float) 17.0;
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(cityu, zoomlevel);
        Map_View_Map.addMarker(new MarkerOptions().position(cityu));
        Map_View_Map.moveCamera(cu);
    }

    private void setUpMap() {
        Map_View_Map.setOnMapClickListener(this);// add the listener for click for amap object
    }
}
