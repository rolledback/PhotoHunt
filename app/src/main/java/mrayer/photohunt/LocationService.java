package mrayer.photohunt;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ailae on 4/7/16.
 */

// Geofencing code from http://developer.android.com/training/location/geofencing.html

// Aila's TODO: Add GoogleAPIClient in here, add location monitoring

public class LocationService extends IntentService {

    static final int GEOFENCE_RADIUS_IN_METERS = 150;
    static final String TAG = "LocationService";

    GoogleApiClient googleAPI;
    ArrayList<Geofence> geofences;
    ArrayList<Photo> photos;

    /**
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */
    public LocationService() {
        super("LocationService");
    }

//    public void onCreate() {
//        // Make a new GoogleAPIClient
//        googleAPI = new GoogleApiClient.Builder(this)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(LocationServices.API)
//                .build();
//        geofences = new ArrayList<Geofence>();
//        super.onCreate();
//    }
//
//
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        googleAPI.connect();
//        return super.onStartCommand(intent,flags,startId);
//    }
//
//    public void onDestroy() {
//        googleAPI.disconnect();
//        super.onDestroy();
//    }

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
            // If entered, need to make sure location monitoring is set

            // Get the geofences that were triggered
            List<Geofence> geofences = geofencingEvent.getTriggeringGeofences();

            // Could get location here... debatable

        }
        else if(transition == Geofence.GEOFENCE_TRANSITION_EXIT)
        {
            // Check if any geofences entered... if none, stop location monitoring
        }


    }
}