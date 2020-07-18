package com.devcom.goretstaxi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private LocationRequest locationRequest;

    private Marker pickUpMarker;

    private Button logoutDriverBtn, settingsDriverBtn;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase mDatabase;

    private String driverID, passengerID = "";

    private DatabaseReference assignedPassengerRef, assignedPassengerPositionRef;

    private Boolean currentLogoutStatus = false;

    private ValueEventListener assignedPassengerPositionListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        logoutDriverBtn = findViewById(R.id.driverLogOutBtn);
        settingsDriverBtn = findViewById(R.id.driverSettingsBtn);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        driverID = currentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance();

        settingsDriverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingsIntent = new Intent(DriverMapActivity.this, SettingsActivity.class);
                settingsIntent.putExtra("type", "Drivers");
                startActivity(settingsIntent);
            }
        });

        logoutDriverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentLogoutStatus = true;
                mAuth.signOut();

                logoutDriver();
                disconnectDriver();
            }
        });

        getAssignedPassengerRequest();
    }

    private void getAssignedPassengerRequest() {
        assignedPassengerRef = mDatabase.getReference().child("Users")
                .child("Drivers").child(driverID).child("Passenger Ride ID");

        assignedPassengerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    passengerID = snapshot.getValue().toString();

                    getAssignedPassengerPosition();
                } else {
                    passengerID = "";

                    if (pickUpMarker != null) {
                        pickUpMarker.remove();

                    }

                    if (assignedPassengerPositionListener != null) {
                        assignedPassengerPositionRef.removeEventListener(assignedPassengerPositionListener);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getAssignedPassengerPosition() {
        assignedPassengerPositionRef = mDatabase.getReference().child("Passenger Request")
                .child(passengerID).child("l");

        assignedPassengerPositionListener = assignedPassengerPositionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<Object> passengerLocationList = (List<Object>) snapshot.getValue();
                    double locLat = 0;
                    double locLng = 0;

                    if (passengerLocationList.get(0) != null) {
                        locLat = Double.parseDouble(passengerLocationList.get(0).toString());
                    }
                    if (passengerLocationList.get(1) != null) {
                        locLng = Double.parseDouble(passengerLocationList.get(1).toString());
                    }

                    LatLng driverLatLng = new LatLng(locLat, locLng);
                    pickUpMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Забрать пассажира отсюда").icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        buildGoogleApiClient();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (getApplicationContext() != null) {
            lastLocation = location;

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(17));

            String userId = currentUser.getUid();
            DatabaseReference driverAvailableRef = mDatabase.getReference().child("Driver Available");
            DatabaseReference driverWorkingRef = mDatabase.getReference().child("Driver Working");

            GeoFire geoFireAvailable = new GeoFire(driverAvailableRef);
            GeoFire geoFireWorking = new GeoFire(driverWorkingRef);


            switch (passengerID) {
                case "":
                    geoFireWorking.removeLocation(userId);
                    geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
                default:
                    geoFireAvailable.removeLocation(userId);
                    geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;

            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!currentLogoutStatus) {

            disconnectDriver();


        }


    }

    private void disconnectDriver() {
        String userId = currentUser.getUid();
        DatabaseReference driverAvailableRef = mDatabase.getReference().child("Driver Available");

        GeoFire geoFire = new GeoFire(driverAvailableRef);
        geoFire.removeLocation(userId);
    }

    private void logoutDriver() {
        Intent logoutIntent = new Intent(this, WelcomeActivity.class);
        startActivity(logoutIntent);
        finish();
    }
}