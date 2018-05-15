package com.itsmap.memoryapp.appprojektmemoryapp.LogIn;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.itsmap.memoryapp.appprojektmemoryapp.Activities.MainActivity;
import com.itsmap.memoryapp.appprojektmemoryapp.R;

public class LogInScreen extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private FirebaseAuth auth;
    private ProgressBar progressBar;
    private Button btnSignup, btnLogin, btnReset;

    final static int PERMISSIONS_REQUEST = 154;
    boolean locationPermission = false;
    boolean cameraPermission = false;
    boolean storageReadPermission = false;
    boolean storageWritePermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        checkForPermissions();

        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(LogInScreen.this, MainActivity.class));
            finish();
        }

        // set the view now
        setContentView(R.layout.activity_log_in_screen);

        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);
        btnSignup = findViewById(R.id.btn_signup);
        btnLogin = findViewById(R.id.btn_login);
        btnReset = findViewById(R.id.btn_reset_password);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LogInScreen.this, SignUpScreen.class));
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LogInScreen.this, ResetPasswordScreen.class));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = inputEmail.getText().toString();
                final String password = inputPassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //authenticate user
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LogInScreen.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                progressBar.setVisibility(View.GONE);
                                if (!task.isSuccessful()) {
                                    // there was an error
                                    if (password.length() < 6) {
                                        inputPassword.setError(getString(R.string.minimum_password));
                                    } else {
                                        Toast.makeText(LogInScreen.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Intent intent = new Intent(LogInScreen.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
            }
        });
    }

    public void checkForPermissions() {
        if(ContextCompat.checkSelfPermission(LogInScreen.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                + ContextCompat.checkSelfPermission(LogInScreen.this, android.Manifest.permission.CAMERA)
                + ContextCompat.checkSelfPermission(LogInScreen.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                + ContextCompat.checkSelfPermission(LogInScreen.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LogInScreen.this, new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSIONS_REQUEST);
        } else {
            locationPermission = true;
            cameraPermission = true;
            storageReadPermission = true;
            storageWritePermission = true;
            return;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                if(grantResults.length > 0) {
                    locationPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    cameraPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    storageReadPermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    storageWritePermission = grantResults[3] == PackageManager.PERMISSION_GRANTED;

                    if(locationPermission) {
                        Toast.makeText(LogInScreen.this, getResources().getString(R.string.LocationPermissionsSuccess), Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(LogInScreen.this, getResources().getString(R.string.LocationPermissionsFailed), Toast.LENGTH_SHORT).show();
                    }

                    if(cameraPermission) {
                        Toast.makeText(LogInScreen.this, getResources().getString(R.string.CameraPermissionsSuccess), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LogInScreen.this, getResources().getString(R.string.CameraPermissionsFailed), Toast.LENGTH_SHORT).show();
                    }

                    if(storageReadPermission && storageWritePermission) {
                        Toast.makeText(LogInScreen.this, getResources().getString(R.string.StoragePermissionsSuccess), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LogInScreen.this, getResources().getString(R.string.StoragePermissionsSuccess), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
        }
    }
}