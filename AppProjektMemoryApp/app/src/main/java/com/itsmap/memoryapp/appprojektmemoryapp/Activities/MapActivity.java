package com.itsmap.memoryapp.appprojektmemoryapp.Activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

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

            BitmapDescriptor markerImg = BitmapDescriptorFactory.fromResource(R.drawable.note_image);
            LatLng aarhus = new LatLng(56.162939, 10.203921);
            googleMap.addMarker(new MarkerOptions().position(aarhus)
                    .title("Marker in Aarhus")
                    .icon(markerImg));
            googleMap.setMinZoomPreference(10);
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(aarhus));
           /* firebaseDb.collection("Notes").whereEqualTo("Creator", FirebaseAuth.getInstance().getCurrentUser())
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()) {
                        for (DocumentSnapshot doc : task.getResult()){
                            Double Latitude = Double.valueOf(doc.getData().get("Latitude").toString());
                            Double Longtitude = Double.valueOf(doc.getData().get("Longtitude").toString());
                            String Name = doc.getData().get("Name").toString();

                            LatLng noteLocation = new LatLng(Latitude, Longtitude);
                            googleMap.addMarker(new MarkerOptions().position(noteLocation).title(Name));
                        }
                    }
                    else {
                        Log.w("DbRead", "Error reading notes from db");
                    }
                }
            });*/

            //TODO: Hvis vi ønsker at mappet skal starte met at zoome ind på brugerens nuværende lcoation, skal MapActivity have adgang til service'en.
            //LatLng latestLocation = new LatLng(memoryAppService.getLocation().getLatitude(), memoryAppService.getLocation().getLongtitude());
            //googleMap.moveCamera(CameraUpdateFactory.newLatLng(latestLocation));
        }
}
