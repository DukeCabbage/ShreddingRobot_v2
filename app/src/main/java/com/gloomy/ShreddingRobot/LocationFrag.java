package com.gloomy.ShreddingRobot;

import android.app.Fragment;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import java.text.DecimalFormat;

public class LocationFrag extends Fragment implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "LocationFrag";
    public static final int UPDATE_INTERVAL_IN_MILLISECONDS = 2000;
    private static final int FASTEST_INTERVAL_IN_MILLISECONDS = 500;
    DecimalFormat dff = new DecimalFormat("#.00");

    private Context _context;
    private TrackingActivity parentActivity;
    private LocationClient mLocationClient;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;

    private boolean firstConnected;
    private boolean hasSpeed;
    private boolean hasAltitude;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = (TrackingActivity) getActivity();
        _context = parentActivity;
//		findView();
        init();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void init(){
        mLocationClient = new LocationClient(_context, this, this);
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL_IN_MILLISECONDS);

        firstConnected = true;
    }

    public void startTracking() {
        mLocationClient.connect();
        Log.e(TAG, "mLocationClient connect");
    }

    public void stopTracking() {
        if (mLocationClient.isConnected()) {
            mLocationClient.removeLocationUpdates(this);
            Log.e(TAG, "removeLocationUpdates");
        }
        mLocationClient.disconnect();
        Log.e(TAG, "mLocationClient disconnect");
    }

    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
//		mCurrentLocation = mLocationClient.getLastLocation();
//		Log.e(TAG, ""+mCurrentLocation.hasSpeed());
//		Log.e(TAG, ""+mCurrentLocation.hasAltitude());
        Toast.makeText(_context, "Connected", Toast.LENGTH_SHORT).show();
        // Send request for location update
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }
    @Override
    public void onDisconnected() {
        Toast.makeText(_context, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(_context, "Location service fails, please check connection.",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        if (firstConnected){
            Toast.makeText(_context, "Got location feedback", Toast.LENGTH_SHORT).show();
            firstConnected = false;
        }

        parentActivity.updateSpeed(location.getSpeed(), location.getAccuracy());
        parentActivity.updateAltimeter(location.getAltitude());
    }
}