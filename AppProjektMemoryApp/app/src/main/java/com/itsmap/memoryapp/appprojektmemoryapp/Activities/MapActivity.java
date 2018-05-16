package com.itsmap.memoryapp.appprojektmemoryapp.Activities;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.itsmap.memoryapp.appprojektmemoryapp.R;

//Inspireret af https://developers.google.com/maps/documentation/android-sdk/map-with-marker
public class MapActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    Button returnBtn;
    private LatLng location = null;

    FirebaseFirestore firebaseDb = FirebaseFirestore.getInstance();
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_map_screen);
            // Get the SupportMapFragment and request notification
            // when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);

            mapFragment.getMapAsync(this);

            returnBtn = findViewById(R.id.returnBtn);
            returnBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        /**
         * Manipulates the map when it's available.
         * The API invokes this callback when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera. In this case,
         * we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user receives a prompt to install
         * Play services inside the SupportMapFragment. The API invokes this method after the user has
         * installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(final GoogleMap googleMap) {

            googleMap.setMinZoomPreference(10);
            if(getIntent().getExtras() != null) {
                location = new LatLng((Double) getIntent().getExtras().get("LocationLat"), (Double) getIntent().getExtras().get("LocationLong"));
            }
            if(location != null){
                googleMap.addMarker(new MarkerOptions().position(location));
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(location));
            }
            firebaseDb.collection("Notes")
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()) {
                        for (DocumentSnapshot doc : task.getResult()){
                            location = new LatLng((Double) doc.getData().get("Latitude"), (Double) doc.getData().get("Longtitude"));

                            String Name = doc.getData().get("Name").toString();
                            BitmapDescriptor markerImg = BitmapDescriptorFactory.fromResource(R.drawable.note_image);
                            googleMap.addMarker(new MarkerOptions().position(location).title(Name).icon(markerImg));
                        }
                    }
                    else {
                        Log.w("DbRead", "Error reading notes from db");
                    }
                }
            });
        }
}
