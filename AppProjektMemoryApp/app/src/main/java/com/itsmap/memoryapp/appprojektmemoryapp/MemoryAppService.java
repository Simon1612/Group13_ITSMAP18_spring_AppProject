package com.itsmap.memoryapp.appprojektmemoryapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.itsmap.memoryapp.appprojektmemoryapp.Activities.MainActivity;
import com.itsmap.memoryapp.appprojektmemoryapp.Models.NoteDataModel;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class MemoryAppService extends Service {
    private final IBinder myBinder = new MemoryAppService.LocalBinder();
    NotificationCompat.Builder notification;
    NotificationManager nManager;
    NotificationChannel nChannel;
    Intent notificationIntent, notesReadyIntent, locationReadyIntent;
    FirebaseUser currentUser;
    FirebaseFirestore database;
    DocumentReference userRef;
    List<NoteDataModel> lastFourNotes;

    String currentLocationReady;
    private FusedLocationProviderClient mFusedLocationClient;
    String notesReady;
    LatLng currentLocation;

    int ONGOING_NOTIFICATION_ID = 1337;

    public class LocalBinder extends Binder {
        public MemoryAppService getService() {
            return MemoryAppService.this;
        }
    }

    public MemoryAppService() {
    }

    @Override
    public void onCreate() {
        currentLocationReady = getResources().getString(R.string.currentLocationReady);
        notesReady = "NOTES_READY";
        nManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        lastFourNotes = new ArrayList<NoteDataModel>();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //Set broadcast receiver
    /*    IntentFilter filter = new IntentFilter();
        filter.addAction(currentLocationReady);
        this.registerReceiver(br, filter);*/
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    //Modified OnStartCommand example from: https://developer.android.com/reference/android/app/Service.html
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        database = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = database.collection("Users").document(currentUser.getEmail());
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (!document.exists()) {
                        CollectionReference colRef = database.collection("Users");

                        colRef.document(currentUser.getEmail())
                                .collection("Notes")
                                .document(getResources().getString(R.string.firstNoteName))
                                .set(new NoteDataModel(getResources().getString(R.string.firstNoteName), getResources().getString(R.string.firstNoteDescription), 0, 0));
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
        notificationIntent = new Intent(this, MemoryAppService.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        //Create notification
        notification =
                new NotificationCompat.Builder(this, getResources().getString(R.string.app_name))
                        .setContentTitle(getResources().getString(R.string.notificationTitle))
                        .setContentText(getResources().getString(R.string.notificationText) + "\n" + DateFormat.getInstance().format(System.currentTimeMillis()))
                        //.setSmallIcon(R.drawable.ic_stat_service) VI SKAL HA ET ICON IK
                        .setContentIntent(pendingIntent)
                        .setWhen(System.currentTimeMillis());

        nChannel = new NotificationChannel("1337", getResources().getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW);
        nManager.createNotificationChannel(nChannel);
        notification.setChannelId("1337");

        //Start service as foreground
        startForeground(ONGOING_NOTIFICATION_ID, notification.build());

        notesReadyIntent = new Intent();
        notesReadyIntent.setAction(notesReady);
        locationReadyIntent = new Intent();
        locationReadyIntent.setAction(currentLocationReady);

        getLocation();

        //updateLastFourNotes();
        sendBroadcast(notesReadyIntent);

        return Service.START_NOT_STICKY;
    }

    public void SaveNote(NoteDataModel note){
        userRef.collection("Notes")
                .document(note.getName())
                .set(note);
    }

    public LatLng getCurrentLocation(){ return currentLocation; }

    public List<NoteDataModel> getLastFourNotes() {
        return lastFourNotes;
    }

   /* public void updateLastFourNotes() {
        Task t = userRef.collection("Notes")
                .orderBy("timeStamp", Query.Direction.DESCENDING)
                .limit(4)
                .get();

        synchronized (t){
            if(t.isSuccessful())
            {
                lastFourNotes.addAll((List<NoteDataModel>) t.getResult());
                sendBroadcast(notesReadyIntent);
            } else {
                Log.d(TAG, "get failed with ", t.getException());
            }
        }
    }*/

    public void getLocation() {
        int hasPermission = this.checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION");
        if(hasPermission == 0) {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {

                                Location location = task.getResult();
                                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                                sendBroadcast(locationReadyIntent);
                            }
                        }
                    });
        }
        else {
            currentLocation = null;
            Toast.makeText(this, "You need to allow location-services", Toast.LENGTH_SHORT).show();
        }
    }

    //Inspiration from: https://developer.android.com/guide/components/broadcasts.html
    private BroadcastReceiver br = new BroadcastReceiver(){ //MÅSKE IKKE NØDVENDIG
        @Override
        public void onReceive(Context context, Intent intent) {

            //Update Notification
            synchronized (notification){
                notification.setContentText(getResources().getString(R.string.notificationText) + "\n" + DateFormat.getInstance().format(System.currentTimeMillis()))
                        .setWhen(System.currentTimeMillis());
                nManager.notify(1337, notification.build());
            }
        }
    };

}
