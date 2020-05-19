package com.ksb.covid_19admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import dmax.dialog.SpotsDialog;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.location.Location;
import android.os.Looper;
import android.provider.Settings;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    EditText etUsername, etPassword;
    Button btSignin;
   // TextView latt,longg;
    static FirebaseAuth mAuth;
    String username, password;
    View contextView;
    AlertDialog alertDialog;
    FirebaseAuth.AuthStateListener mAuthStateListener;
    static FirebaseUser currentUser;
    LocationManager locationManager;
    LocationListener locationListener;
    int PERMISSION_ID=44;
    FusedLocationProviderClient mFusedLocationClient;
    private boolean onTimeToast=false;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        //locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        //TODO Add Shared Preferences
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        sharedPreferences = this.getSharedPreferences("com.ksb.covid_19admin",MODE_PRIVATE);

        if(sharedPreferences.getString("postalCode",null)==null) {
            getLastLocation();
        }



        contextView = findViewById(android.R.id.content);
       // latt = findViewById(R.id.lat);
       // longg = findViewById(R.id.longg);
        alertDialog= new SpotsDialog.Builder().setContext(this).build();
        mAuth = FirebaseAuth.getInstance();

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btSignin = findViewById(R.id.bt_signin);

        btSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validate();
            }
        });


        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                /////////
                currentUser = mAuth.getCurrentUser();

                if(currentUser!=null)
                {
                    // TODO: 18-05-2020  
                   // java.text.DateFormat dateFormat = java.text.DateFormat.getDateInstance();
                   // String formatedDate = dateFormat.format(new Date(Long.valueOf("1589735313424")).getTime());
                    // Sign In
                    Toast.makeText(MainActivity.this, "Signed In", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    // Sign Out
                    Toast.makeText(MainActivity.this, "Not Signed In", Toast.LENGTH_SHORT).show();
                }
            }
        };

    }


    private void validate(){
        username=etUsername.getText().toString().trim();
        password=etPassword.getText().toString().trim();

        if(!username.equals("") && !password.equals("") && username.length()>0 && password.length()>=6){
            goToLogin();
        }
        else{

            if(password.length()<6)
            {
                Toast.makeText(this, "Password Length is too Short", Toast.LENGTH_SHORT).show();
            }

            Snackbar.make(contextView, "Invalid inputs", Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    private void goToLogin() {
        alertDialog.setMessage("Signing in..");
        alertDialog.show();
        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            alertDialog.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                            Snackbar.make(contextView, "Welcome Admin", Snackbar.LENGTH_SHORT)
                                    .show();
                        } else {
                            alertDialog.dismiss();
                            Snackbar.make(contextView, "Wrong Password, please try again", Snackbar.LENGTH_SHORT)
                                    .show();
                        }

                    }
                });

    }



    @Override
    public void onStart() {
        super.onStart();

        currentUser = mAuth.getCurrentUser();
        mAuth.addAuthStateListener(mAuthStateListener);

        if(currentUser!=null){
            Log.i("CURRENT USER",currentUser.getEmail());
            updateUI(currentUser);

        }
    }

    @Override
    protected void onStop() {
            super.onStop();
            if(mAuthStateListener!= null)
            {
              mAuth.removeAuthStateListener(mAuthStateListener);
            }
    }

    private void updateUI(FirebaseUser currentUser) {
        Intent intent = new Intent(MainActivity.this,AdminActivity.class);
        intent.putExtra("currentUser",currentUser);
        startActivity(intent);
        finish();
    }


    ///////////////////   LOCATION !!!

    private boolean checkPermissions(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        return false;
    }
    private void requestPermissions(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_ID);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // Granted. Start getting the location information
                getLastLocation();
            }
            else{
                if(!onTimeToast)
                {
                    Toast.makeText(this, "Plz Give Permission", Toast.LENGTH_SHORT).show();
                    onTimeToast = true;
                }
                getLastLocation();
            }
        }
    }
    private boolean isLocationEnabled(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation(){
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                } else {
                                    //latt.setText(location.getLatitude()+"");
                                   // longg.setText(location.getLongitude()+"");

                                    getInfoFromLatLong(location);
                                }
                            }
                        }
                );
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location mLastLocation = locationResult.getLastLocation();
                //latt.setText(mLastLocation.getLatitude()+"");
               // longg.setText(mLastLocation.getLongitude()+"");

                getInfoFromLatLong(mLastLocation);

            }
        };

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );
    }

    public void getInfoFromLatLong(Location location)
    {
        try {
            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());

            List<Address> addresses = geocoder.getFromLocation(location.getLatitude()
                    ,location.getLongitude(),1);

            if(addresses.get(0).getPostalCode() != null && addresses.get(0).getPostalCode().length()>0 && sharedPreferences.getString("postalCode",null)==null)
            {
                sharedPreferences.edit().putString("postalCode",addresses.get(0).getPostalCode()).apply();
                Log.i("Postal Code Saved :: ",addresses.get(0).getPostalCode());
                Toast.makeText(this, addresses.get(0).getPostalCode(), Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermissions() && sharedPreferences.getString("postalCode",null)==null) {
            getLastLocation();
        }
    }
}
