package com.gloomy.ShreddingRobot;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class LocationFrag extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "LocationFrag";
    public static final int UPDATE_INTERVAL_IN_MILLISECONDS = 2000;
    private static final int FASTEST_INTERVAL_IN_MILLISECONDS = 500;

    private Context _context;
    private TrackingActivity parentActivity;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private boolean firstConnected;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = (TrackingActivity) getActivity();
        _context = parentActivity;
        init();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void init(){
        mGoogleApiClient = new GoogleApiClient.Builder(_context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS); // Update location every second

        firstConnected = true;
    }

    public void startTracking() {
        mGoogleApiClient.connect();
        Log.e(TAG, "mGoogleApiClient connect");

        // Get Location Manager and check for GPS & Network location services
        LocationManager lm = (LocationManager) _context.getSystemService(Context.LOCATION_SERVICE);
        if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // Build the alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(_context);
            builder.setTitle("Location Services Not Active");
            builder.setMessage("Please enable Location Services and GPS for speed measuring");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Show location settings when the user acknowledges the alert dialog
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
    }

    public void stopTracking() {
        mGoogleApiClient.disconnect();
        Log.e(TAG, "mLocationClient disconnect");
    }

    @Override
    public void onConnected(Bundle dataBundle) {
        Toast.makeText(_context, "Connected", Toast.LENGTH_SHORT).show();
        // Send request for location update
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection has been suspend");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "GoogleApiClient connection has failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        if (firstConnected){
            Toast.makeText(_context, "Got location feedback", Toast.LENGTH_SHORT).show();
            firstConnected = false;
        }

        double speed = location.getSpeed();
        double accuracy = location.getAccuracy();
        double altitude = location.getAltitude();

        Log.e(TAG, "Location received: " + location.toString());
        Log.e(TAG, "Speed: " + speed);
        Log.e(TAG, "Accuracy: " + accuracy);
        Log.e(TAG, "Altitude: " + altitude);

        parentActivity.updateSpeed(location.getSpeed(), location.getAccuracy());
        parentActivity.updateAltimeter(location.getAltitude());
    }
}