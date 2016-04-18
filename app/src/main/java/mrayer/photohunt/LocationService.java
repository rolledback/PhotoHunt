package mrayer.photohunt;

import android.Manifest;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
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
import java.util.List;
import java.util.Random;

/**
 * Created by ailae on 4/7/16.
 */

// Geofencing code from http://developer.android.com/training/location/geofencing.html

// Location code from http://developer.android.com/training/location/receive-location-updates.html

public class LocationService extends IntentService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    static final int DIST = 5;
    static final String TAG = "LocationService";

    int photosFound;

    GoogleApiClient googleAPI;
    List<LatLng> loc;
    int numGeofences;
    SharedPreferences currentAlbumPref;

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
        photosFound = 0;

        Context context = getApplicationContext();
        currentAlbumPref = context.getSharedPreferences(getString(R.string.current_album_pref),
                Context.MODE_PRIVATE);

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

            // TODO: Need to make this open a new Current PhotoHunt Activity - once I make it

            Intent notifyIntent = new Intent(this, CurrentPhotoHuntActivity.class);

            // Because clicking the notification opens a new ("special") activity, there's
            // no need to create an artificial back stack.
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    notifyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentTitle("PhotoHunt")
                    .setContentText("Geofence entered - you are close to a photo")
                    .setContentIntent(pendingIntent);

            int rand = new Random().nextInt();
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // Notification ID allows you to update the notification later on
            nm.notify(rand, notificationBuilder.build());

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
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle("PhotoHunt")
                        .setContentText("You are at a photo location!");

                int rand = new Random().nextInt();
                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                // Notification ID allows you to update the notification later on
                nm.notify(rand, notificationBuilder.build());

                // Remove l from loc
                loc.remove(temp);

                SharedPreferences.Editor editor = currentAlbumPref.edit();
                // Get the current photos found and increment it by 1
                // Keeping track of the photos found in this class would not reset it properly
                editor.putInt(getString(R.string.photos_found), currentAlbumPref.getInt(getString(R.string.photos_found), 0) + 1);
                editor.commit();

                // Check to see if they completed the album
                // TODO: What if they exit a geofence and re-enter? Could count photo more than once
                if(currentAlbumPref.getInt(getString(R.string.photos_found), -1) == currentAlbumPref.getInt(getString(R.string.total_photos), -2))
                {

                    Intent notifyIntent = new Intent(this, CurrentPhotoHuntActivity.class);

                    PendingIntent pendingIntent = PendingIntent.getActivity(
                            this,
                            0,
                            notifyIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

                    NotificationCompat.Builder notification = new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.notification_icon)
                            .setContentTitle("PhotoHunt")
                            .setContentText("You have completed your current album!")
                            .setContentIntent(pendingIntent);

                    // TODO: Open the CurrentPhotoHunt upon clicking notification

                    rand = new Random().nextInt();
                    // Notification ID allows you to update the notification later on
                    nm.notify(rand, notification.build());

                    // TODO: Add this album to user's completed album list/count
                }

                Log.d(TAG, "Photos found: " + currentAlbumPref.getInt(getString(R.string.photos_found), -1));

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
        locationReq.setInterval(5000); // 5 sec
        locationReq.setFastestInterval(3000); // 3 sec
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