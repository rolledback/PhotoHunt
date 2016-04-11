package mrayer.photohunt;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
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

public class LocationService extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    static final int GEOFENCE_RADIUS_IN_METERS = 150;
    static final String TAG = "LocationService";

    GoogleApiClient googleAPI;
    List<LatLng> loc;

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

        super.onCreate();
    }


    public int onStartCommand(Intent intent, int flags, int startId) {
        googleAPI.connect();
        return super.onStartCommand(intent,flags,startId);
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

            // Need to know what to compare to, get geofence locations
            // Get the geofences that were triggered
            List<Geofence> geofences = geofencingEvent.getTriggeringGeofences();
            loc = new ArrayList<LatLng>();
            for(Geofence g : geofences)
            {
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

            // Set up location monitoring, now have global list to compare to


        }
        else if(transition == Geofence.GEOFENCE_TRANSITION_EXIT)
        {
            // Check if any geofences entered... if none, stop location monitoring
        }


    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, " location services connected.");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, " Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}