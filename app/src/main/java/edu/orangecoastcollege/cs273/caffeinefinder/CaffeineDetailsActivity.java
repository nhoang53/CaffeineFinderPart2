package edu.orangecoastcollege.cs273.caffeinefinder;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location; // Dont use import com.google.android.gms.identity.intents.Address;
import android.media.MediaPlayer;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.Manifest;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;



public class CaffeineDetailsActivity extends AppCompatActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    private TextView locationListNameTextView;
    private TextView locationListAddressTextView;
    private TextView locationListPhoneTextView;

    private CaffeineLocation caffeineLocation;
    private Location currentLocation;
    private double myLatitude;
    private double myLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caffeine_details);
        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

        // get Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.detailMapFragment);
        mapFragment.getMapAsync(this);

        locationListNameTextView = (TextView) findViewById(R.id.locationListNameTextView);
        locationListAddressTextView = (TextView) findViewById(R.id.locationListAddressTextView);
        locationListPhoneTextView = (TextView) findViewById(R.id.locationListPhoneTextView);

        // Get parcel from Intent
        if(getIntent().getExtras().getParcelable("ClosestCaffeineLocation") != null)
        {
            caffeineLocation = getIntent().getExtras().getParcelable("ClosestCaffeineLocation");
        }
        else
        {
            caffeineLocation = getIntent().getExtras().getParcelable("selectedCaffeineLocation");
        }
        /*currentLocation.setLatitude(getIntent().getDoubleExtra("MyLatitude", 0.0));
        currentLocation.setLongitude(getIntent().getDoubleExtra("MyLongitude", 0.0));*/
        myLatitude = getIntent().getDoubleExtra("MyLatitude", 0);
        myLongitude = getIntent().getDoubleExtra("MyLongitude", 0);

        locationListNameTextView.setText(caffeineLocation.getName());
        locationListAddressTextView.setText(caffeineLocation.getAddress()
                            + ", " + caffeineLocation.getCity() + " "
                            + caffeineLocation.getState() + " "
                            + caffeineLocation.getZipCode());

        locationListPhoneTextView.setText(caffeineLocation.getPhone());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng caffeineStore = new LatLng(caffeineLocation.getLatitude(), caffeineLocation.getLongitude());
        mMap.addMarker(new MarkerOptions().position(caffeineStore).title(caffeineLocation.getName()));
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(caffeineStore, 14));

        LatLng myLocation = new LatLng(myLatitude, myLongitude);
        mMap.addMarker(new MarkerOptions().position(myLocation).title("My Location")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.my_marker)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 14), 2000, null);

    }

}
