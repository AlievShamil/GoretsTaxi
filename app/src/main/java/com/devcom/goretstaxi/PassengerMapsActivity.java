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
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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

import java.util.HashMap;
import java.util.List;

public class PassengerMapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private LocationRequest locationRequest;
    private LatLng passengerPosition;
    private Marker driverMarker, pickUpMarker;

    private Button logoutBtn, orderTaxiBtn, settingsBtn;

    private GeoFire geoFire;
    private GeoQuery geoQuery;
    private String passengerID, driverId;

    private double radius = 1;
    private Boolean isDriverFound = false, requestType = false;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase mDatabase;
    private DatabaseReference passengerLocationRef, driverLocationRef, driverAvailableRef, driverRef;

    private ValueEventListener driversLocationListener;

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
        driverAvailableRef = mDatabase.getReference().child("Driver Available");
        driverLocationRef = mDatabase.getReference().child("Driver Working");

        logoutBtn = findViewById(R.id.passengerLogOutBtn);
        orderTaxiBtn = findViewById(R.id.orderBtn);
        settingsBtn = findViewById(R.id.settingsBtn);

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingsIntent = new Intent(PassengerMapsActivity.this, SettingsActivity.class);
                settingsIntent.putExtra("type", "Passengers");
                startActivity(settingsIntent);
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutPassenger();
            }
        });

        orderTaxiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requestType) {
                    requestType = false;
                    geoQuery.removeAllListeners();
                    driverLocationRef.removeEventListener(driversLocationListener);

                    if (isDriverFound != null) {
                        driverRef = mDatabase.getReference().child("Users").child("Drivers")
                                .child(driverId).child("Passenger Ride ID");
                        driverRef.removeValue();
                        driverId = null;
                    }

                    isDriverFound = false;
                    radius = 1;

                    geoFire = new GeoFire(passengerLocationRef);
                    geoFire.removeLocation(passengerID);

                    if (pickUpMarker != null) {
                        pickUpMarker.remove();
                    }

                    if (driverMarker != null) {
                        driverMarker.remove();
                    }
                    orderTaxiBtn.setText("Вызвать такси");

                } else {
                    requestType = true;
                    geoFire = new GeoFire(passengerLocationRef);
                    geoFire.setLocation(passengerID, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()));

                    passengerPosition = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    pickUpMarker = mMap.addMarker(new MarkerOptions().position(passengerPosition).title("Я здесь").icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));

                    orderTaxiBtn.setText("Поиск водителя...");
                    getNearbyDriver();
                }
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

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
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
        Intent logoutIntent = new Intent(this, WelcomeActivity.class);
        startActivity(logoutIntent);
        finish();
    }

    private void getNearbyDriver() {
        GeoFire driversGeoFire = new GeoFire(driverAvailableRef);
        GeoQuery geoQuery = driversGeoFire.queryAtLocation(new GeoLocation(passengerPosition.latitude, passengerPosition.longitude), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!isDriverFound && requestType) {
                    isDriverFound = true;
                    driverId = key;

                    driverRef = mDatabase.getReference()
                            .child("Users").child("Drivers").child(key);
                    HashMap driverMap = new HashMap();
                    driverMap.put("Passenger Ride ID", passengerID);
                    driverRef.updateChildren(driverMap);

                    getDriverLocation();
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
                if (!isDriverFound) {
                    radius = radius + 1;
                    getNearbyDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void getDriverLocation() {
        driversLocationListener = driverLocationRef.child(driverId).child("l").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && requestType) {
                    List<Object> driverLocationList = (List<Object>) snapshot.getValue();
                    double locLat = 0;
                    double locLng = 0;

                    orderTaxiBtn.setText("Водитель уже в пути");

                    if (driverLocationList.get(0) != null) {
                        locLat = Double.parseDouble(driverLocationList.get(0).toString());
                    }
                    if (driverLocationList.get(1) != null) {
                        locLng = Double.parseDouble(driverLocationList.get(1).toString());
                    }

                    LatLng driverLatLng = new LatLng(locLat, locLng);

                    if (driverMarker != null) {
                        driverMarker.remove();
                    }

                    Location location1 = new Location("1");
                    location1.setLatitude(driverLatLng.latitude);
                    location1.setLongitude(driverLatLng.longitude);

                    Location location2 = new Location("2");
                    location1.setLatitude(passengerPosition.latitude);
                    location1.setLongitude(passengerPosition.longitude);

                    float distance = location1.distanceTo(location2);
                    if(distance<100) {
                        orderTaxiBtn.setText("Ваще такси подъезжает " + distance / 1000);
                    } else {
                        orderTaxiBtn.setText("Расстояние до такси " + distance / 1000);
                    }

                    driverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Ваше такси находится здесь").icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}