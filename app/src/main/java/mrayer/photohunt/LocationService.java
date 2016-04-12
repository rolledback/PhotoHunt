package mrayer.photohunt;

import android.Manifest;
import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ailae on 4/7/16.
 */

// Geofencing code from http://developer.android.com/training/location/geofencing.html

// Aila's TODO: Add location monitoring

public class LocationService extends IntentService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    static final int DIST = 5;
    static final String TAG = "LocationService";

    GoogleApiClient googleAPI;
    List<LatLng> loc;
    int numGeofences;

    /**
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */
    public LocationService() {
        super("LocationService");
    }

    public void onCreate() {
        // Make a new GoogleAPIClient
        googleAPI = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleAPI.connect();
        super.onCreate();
    }


    public int onStartCommand(Intent intent, int flags, int startId) {
        googleAPI.connect();
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        googleAPI.disconnect();
        super.onDestroy();
    }

    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns, IntentService
     * stops the service, as appropriate.
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            Log.e(TAG, " error code: " + geofencingEvent.getErrorCode());
            return;
        }

        // Get the transition type
        int transition = geofencingEvent.getGeofenceTransition();

        if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Log.d(TAG, " geofence entered");

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), " geofence entered!!!1 ", Toast.LENGTH_LONG).show();
                }
            });

            // Need to know what to compare location to once location monitoring occurs
            // Need to get geofence locations from geofences that were triggered
            List<Geofence> geofences = geofencingEvent.getTriggeringGeofences();
            numGeofences = geofences.size();

            loc = new ArrayList<LatLng>();
            for (Geofence g : geofences) {
                String[] coord = g.getRequestId().split(",");
                double lat = Double.parseDouble(coord[0]);
                double lon = Double.parseDouble(coord[1]);
                LatLng location = new LatLng(lat, lon);
                loc.add(location);
            }

            handler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "LatLng: " + loc.toString(), Toast.LENGTH_LONG).show();
                }
            });

            // This will run every time a geofence is entered...
            // Need to stop if you change the current album
            // Need to stop if you exit the geofence...

        }

        else if(transition == Geofence.GEOFENCE_TRANSITION_EXIT)
        {
            // Remove that lat/lng from list of locations - no longer in the geofence
            List<Geofence> geofences = geofencingEvent.getTriggeringGeofences();
            numGeofences = numGeofences - geofences.size();

            for (Geofence g : geofences) {
                String[] coord = g.getRequestId().split(",");
                double lat = Double.parseDouble(coord[0]);
                double lon = Double.parseDouble(coord[1]);
                LatLng location = new LatLng(lat, lon);
                loc.remove(location);
            }

            // Could stop location monitoring here if no geofences are entered
            if(numGeofences < 0)
            {
                LocationServices.FusedLocationApi.removeLocationUpdates(googleAPI, this);
            }
        }


    }

    @Override
    public void onLocationChanged(Location location) {
        // Compare to list of locations
        Log.d(TAG, " location changed");

        for(LatLng l: loc)
        {
            Location temp = new Location("");
            temp.setLatitude(l.latitude);
            temp.setLongitude(l.longitude);
            // Dist in meters
            Log.d(TAG, "location: " + location.getLatitude() + " " + location.getLongitude() + " comparing to: "
            + l.latitude + " " + l.longitude + " which is: " + location.distanceTo(temp));
            if(location.distanceTo(temp) < DIST)
            {
                // You are here! Make toast
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "You are at a photo location!!", Toast.LENGTH_LONG).show();
                    }
                });

                // Remove l from loc
                loc.remove(temp);

                // If loc is empty, can stop monitoring locations
                if(loc.size() == 0)
                {
                    LocationServices.FusedLocationApi.removeLocationUpdates(googleAPI, this);
                }
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, " location services connected.");

        LocationRequest locationReq = new LocationRequest();
        locationReq.setInterval(5000);
        locationReq.setFastestInterval(3000);
        locationReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Log.d(Constants.SetChangeLocation_Tag, "Do not have correct permissions");
        }
        else
        {
            Log.d(TAG, " requesting location updates");
            LocationServices.FusedLocationApi.requestLocationUpdates(googleAPI, locationReq, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, " Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}