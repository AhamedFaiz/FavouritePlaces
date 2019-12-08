package com.example.memorableplace;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if(ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 1, locationListener);
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                locationUpdate(lastLocation, "You are Here");
            }
        }
    }

    public  void  locationUpdate(Location location, String title) {
            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(userLocation).title(title));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Intent intent = getIntent();
        if(intent.getIntExtra("placeNumber",0) == 0){
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    locationUpdate(location,"You are Here");
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,1,locationListener);
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                locationUpdate(lastLocation,"You are Here");
            }else{
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
        }else {
            Location location = new Location(LocationManager.GPS_PROVIDER);
            location.setLatitude(MainActivity.locations.get(intent.getIntExtra("placeNumber",0)).latitude);
            location.setLongitude(MainActivity.locations.get(intent.getIntExtra("placeNumber",0)).longitude);
            locationUpdate(location,MainActivity.places.get(intent.getIntExtra("placeNumber",0)));
        }
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onMapLongClick(LatLng latLng) {
                String address = "";
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                try {
                    List<Address> addressList = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
                    if(addressList != null && addressList.size() >0 ) {
                        if (addressList.get(0).getThoroughfare() != null) {
                            if (addressList.get(0).getSubThoroughfare() != null) {
                                address += addressList.get(0).getSubThoroughfare() + ",";
                            }
                            address += addressList.get(0).getThoroughfare();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(address == ""){
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd:MM:yyyy");
                    address = sdf.format(new Date());
                }
                mMap.addMarker(new MarkerOptions().position(latLng).title(address));
                MainActivity.places.add(address);
                MainActivity.locations.add(latLng);
                MainActivity.arrayAdapter.notifyDataSetChanged();

                SharedPreferences sharedPreferences =MapsActivity.this.getSharedPreferences("com.example.memorableplace",Context.MODE_PRIVATE);
                try {
                    ArrayList<String> latitude = new ArrayList<>();
                    ArrayList<String> longitude = new ArrayList<>();
                    for (LatLng cordinates : MainActivity.locations){
                        latitude.add(Double.toString(cordinates.latitude));
                        longitude.add(Double.toString(cordinates.latitude));
                    }
                    sharedPreferences.edit().putString("places",ObjectSerializer.serialize(MainActivity.places)).apply();
                    sharedPreferences.edit().putString("latitudes",ObjectSerializer.serialize(latitude)).apply();
                    sharedPreferences.edit().putString("longitudes",ObjectSerializer.serialize(longitude)).apply();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
