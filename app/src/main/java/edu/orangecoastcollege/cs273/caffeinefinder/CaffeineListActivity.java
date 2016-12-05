package edu.orangecoastcollege.cs273.caffeinefinder;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
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
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class CaffeineListActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int FINE_LOCATION_REQUEST_CODE = 22;
    private DBHelper db;
    private List<CaffeineLocation> mAllLocationsList;
    private ListView locationsListView;
    private LocationListAdapter locationListAdapter;
    private GoogleMap mMap;

    // Member variable to access the Google Play services (LOCATION SERVICES)
    private GoogleApiClient mGoogleApiClient;

    //Member variable to store location requests (how often to update)
    private LocationRequest mLocationRequest;

    //Member variable to store our current location
    private Location myLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caffeine_list);

        deleteDatabase(DBHelper.DATABASE_NAME);
        db = new DBHelper(this);
        db.importLocationsFromCSV("locations.csv");

        mAllLocationsList = db.getAllCaffeineLocations();
        locationsListView = (ListView) findViewById(R.id.locationsListView);
        locationListAdapter = new LocationListAdapter(this, R.layout.location_list_item, mAllLocationsList);
        locationsListView.setAdapter(locationListAdapter);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.caffeineMapFragment);
        mapFragment.getMapAsync(this);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }

        // Define the interval
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(1000);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    private void handleNewLocation(Location newLocation)
    {
        //Clear the map first
        mMap.clear();
        // Let's change the value of myLocation to newLocation
        myLocation = newLocation;
        // Add special marker (blue) for "my" location
        //MBCC Building Lat/Lng (MBCC 135)  33.671028, -117.911305
        LatLng myCoordinate = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        mMap.addMarker(new MarkerOptions()
                .position(myCoordinate)
                .title("Current Location")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.my_marker)));
        CameraPosition cameraPosition = new CameraPosition.Builder().target(myCoordinate).zoom(14.0f).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        mMap.moveCamera(cameraUpdate);

        // Add normal markers for all caffeine locations
        for (CaffeineLocation caffeineLocation : mAllLocationsList) {
            LatLng coordinate = new LatLng(caffeineLocation.getLatitude(), caffeineLocation.getLongitude());
            mMap.addMarker(new MarkerOptions().position(coordinate).title(caffeineLocation.getName()));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("Caffeine Finder", "Suspended connection from Google Play Services.");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
           ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_REQUEST_CODE);
        }
        myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest, this);

        handleNewLocation(myLocation);
    }

    public void findClosestCaffeine(View view)
    {
        double minDistance = Double.MAX_VALUE;
        CaffeineLocation closestCaffeineLocation = null;
        for(CaffeineLocation caffeineLocation : mAllLocationsList)
        {
            Location location = new Location("");
            location.setLatitude(caffeineLocation.getLatitude());
            location.setLongitude(caffeineLocation.getLongitude());

            double distance = myLocation.distanceTo(location);
            if(distance < minDistance)
            {
                minDistance = distance;
                closestCaffeineLocation = caffeineLocation;
            }
        }

        // Lauch new intent to the Details activity
        Intent detailsIntent = new Intent(this, CaffeineDetailsActivity.class);
        detailsIntent.putExtra("ClosestCaffeineLocation", closestCaffeineLocation);
        detailsIntent.putExtra("MyLatitude", myLocation.getLatitude());
        detailsIntent.putExtra("MyLongitude", myLocation.getLongitude());
        startActivity(detailsIntent);
    }

    public void viewLocationDetails(View view)
    {
        if(view instanceof LinearLayout)
        {
            LinearLayout selectedLinearLayout = (LinearLayout) view;
            CaffeineLocation selectedCaffeine = (CaffeineLocation) selectedLinearLayout.getTag();

            Intent detailsIntent = new Intent(this, CaffeineDetailsActivity.class);
            detailsIntent.putExtra("selectedCaffeineLocation", selectedCaffeine);
            detailsIntent.putExtra("MyLatitude", myLocation.getLatitude());
            detailsIntent.putExtra("MyLongitude", myLocation.getLongitude());
            startActivity(detailsIntent);
        }
    }

}
