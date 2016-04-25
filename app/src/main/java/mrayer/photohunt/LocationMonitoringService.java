package mrayer.photohunt;

import android.Manifest;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.service.carrier.CarrierMessagingService;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.parse.Parse;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by ailae on 4/7/16.
 */

// Geofencing code from http://developer.android.com/training/location/geofencing.html

// Location code from http://developer.android.com/training/location/receive-location-updates.html

public class LocationMonitoringService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    static final int DIST = 10;
    static final String TAG = "LM Service";

    int photosFound;

    GoogleApiClient googleAPI;

    // location -> if it has been found
    Map<LatLng, Boolean> locations;
    int totalPhotos;
    SharedPreferences currentAlbumPref;

    public void onCreate() {
        // Make a new GoogleAPIClient
        googleAPI = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        Context context = getApplicationContext();
        currentAlbumPref = context.getSharedPreferences(getString(R.string.current_album_pref),
                Context.MODE_PRIVATE);

        // Build up the list of locations
        locations = new HashMap<LatLng, Boolean>();

        totalPhotos = currentAlbumPref.getInt(getString(R.string.total_photos), -1);

        for(int i = 1; i < totalPhotos + 1; i++)
        {
            String location = currentAlbumPref.getString("photo" + i, "");
            String[] coord = location.split(",");
            double lat = Double.parseDouble(coord[0]);
            double lon = Double.parseDouble(coord[1]);
            LatLng latLngCoord = new LatLng(lat, lon);
            Log.d(TAG, " adding: " + latLngCoord.toString());
            locations.put(latLngCoord, false);
        }

        googleAPI.connect();
        photosFound = 0;

        super.onCreate();
    }


    public int onStartCommand(Intent intent, int flags, int startId) {
        googleAPI.connect();
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        googleAPI.disconnect();
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public synchronized void onLocationChanged(Location location) {
        // Compare to list of locations
        Log.d(TAG, " location changed");

        for(LatLng l: locations.keySet())
        {
            if(locations.get(l)) {
                // if already found skip it
                continue;
            }

            Location temp = new Location("");
            temp.setLatitude(l.latitude);
            temp.setLongitude(l.longitude);
            // Dist in meters
            Log.d(TAG, "location: " + location.getLatitude() + " " + location.getLongitude() + " comparing to: "
            + l.latitude + " " + l.longitude + " which is: " + location.distanceTo(temp));

            if(location.distanceTo(temp) < DIST)
            {
                // Remove l from loc
                locations.put(l, true);

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
                        .setContentText("You are at a photo location!")
                        .setContentIntent(pendingIntent);

                int rand = new Random().nextInt();
                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                // Notification ID allows you to update the notification later on
                nm.notify(rand, notificationBuilder.build());

                Log.d(TAG, "Photos found: " + currentAlbumPref.getInt(getString(R.string.photos_found), -1));

                SharedPreferences.Editor editor = currentAlbumPref.edit();
                // Get the current photos found and increment it by 1
                // Keeping track of the photos found in this class would not reset it properly
                editor.putInt(getString(R.string.photos_found), currentAlbumPref.getInt(getString(R.string.photos_found), 0) + 1);
                editor.commit();

                // Check to see if they completed the album
                // TODO: What if they exit a geofence and re-enter? Could count photo more than once
                int count = 0;
                if(currentAlbumPref.getInt(getString(R.string.photos_found), -1) == currentAlbumPref.getInt(getString(R.string.total_photos), -2))
                {
                    NotificationCompat.Builder notification = new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.notification_icon)
                            .setContentTitle("PhotoHunt")
                            .setContentText("You have completed your current album!")
                            .setContentIntent(pendingIntent);

                    rand = new Random().nextInt();
                    // Notification ID allows you to update the notification later on
                    nm.notify(rand, notification.build());

                    ParseUser user = ParseUser.getCurrentUser();

                    if(user.has("CompletedAlbums") && user.has("CompletedCount")) {
                        List<String> completedAlbums = (ArrayList<String>) user.get("CompletedAlbums");
                        count = (int) user.get("CompletedCount");
                        completedAlbums.add(currentAlbumPref.getString(getString(R.string.album_id), "-1"));
                        count = count + 1;
                        user.put("CompletedAlbums", completedAlbums);
                        user.put("CompletedCount", count);
                    }
                    else {
                        List<String> completedAlbums = new ArrayList<String>();
                        count = 1;
                        completedAlbums.add(currentAlbumPref.getString(getString(R.string.album_id), "-1"));
                        user.put("CompletedAlbums", completedAlbums);
                        user.put("CompletedCount", count);
                    }

                    user.saveInBackground();

                    List<String> completedAlbums = (ArrayList<String>) user.get("CompletedAlbums");
                    count = (int) user.get("CompletedCount");

                    Log.d(TAG, "User's completedAlbums: " + completedAlbums.size() + " number: " + count);
                }

                // If count is same size as number of photos, can stop monitoring locations
                if(count == totalPhotos)
                {
                    LocationServices.FusedLocationApi.removeLocationUpdates(googleAPI, this).setResultCallback(
                            new ResultCallback<Status>() {
                                @Override
                                public void onResult(Status status) {
                                    if (status.isSuccess()) {
                                        Log.i(TAG, " no more photos - removed updates");
                                        stopSelf();
                                    }
                                }
                            }
                    );
                }
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, " location services connected.");

        LocationRequest locationReq = new LocationRequest();
        locationReq.setInterval(7000); // 7 sec
        locationReq.setFastestInterval(7000); // 7 sec
        locationReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Log.d(Constants.SetChangeLocation_Tag, "Do not have correct permissions");
        }
        else
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleAPI, locationReq, this).setResultCallback(
                    new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if (status.isSuccess()) {
                        Log.i(TAG, " requesting location updates");
                    }
                }
            });
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