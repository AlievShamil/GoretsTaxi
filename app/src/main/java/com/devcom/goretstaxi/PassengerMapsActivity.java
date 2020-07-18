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
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PassengerMapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener{

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private LocationRequest locationRequest;
    private LatLng passengerPosition;

    private Button logoutBtn, orderTaxiBtn;

    private GeoFire geoFire;
    private String passengerID;

    private double radius = 1;
    private boolean isDriverFound = false;
    private String driverId;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase mDatabase;
    private DatabaseReference passengerLocationRef;
    private DatabaseReference driverLocationRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        passengerID = currentUser.getUid();

        passengerLocationRef = mDatabase.getReference().child("Passengers Request");
        driverLocationRef = mDatabase.getReference().child("Driver Available");

        logoutBtn = findViewById(R.id.passengerLogOutBtn);
        orderTaxiBtn = findViewById(R.id.orderBtn);

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutPassenger();
            }
        });

        orderTaxiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                geoFire = new GeoFire(passengerLocationRef);
                geoFire.setLocation(passengerID, new GeoLocation(lastLocation.getLatitude(),lastLocation.getLongitude()));

                passengerPosition = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                mMap.addMarker(new MarkerOptions().position(passengerPosition).title("Посмотрите где находится пассажир"));

                orderTaxiBtn.setText("Поиск водителя...");
                getNearbyDriver();
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
        lastLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
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

    }

    private void logoutPassenger() {
        Intent logoutIntent = new Intent(this,WelcomeActivity.class);
        startActivity(logoutIntent);
        finish();
    }

    private void getNearbyDriver() {
        GeoFire driversGeoFire = new GeoFire(driverLocationRef);
        GeoQuery geoQuery = driversGeoFire.queryAtLocation(new GeoLocation(passengerPosition.latitude,passengerPosition.longitude),radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!isDriverFound) {
                    isDriverFound = true;
                    driverId = key;
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!isDriverFound) {
                    radius++;
                    getNearbyDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
}