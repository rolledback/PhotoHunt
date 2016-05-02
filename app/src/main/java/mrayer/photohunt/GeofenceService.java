package mrayer.photohunt;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseUser;

import java.util.List;
import java.util.Random;

/**
 * Created by ailae on 4/24/16.
 */

// This class sends the geofence entered notification and stops
public class GeofenceService extends IntentService {

    private String TAG = "GeofenceService";
    private SharedPreferences photoHuntPrefs;

    public GeofenceService()
    {
        super("GeofenceService");
    }

    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns, IntentService
     * stops the service, as appropriate.
     */
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


            photoHuntPrefs = this.getSharedPreferences(getString(R.string.current_album_pref) + "-" + ParseUser.getCurrentUser().getObjectId(),
                    Context.MODE_PRIVATE);

            if(!photoHuntPrefs.getBoolean("mute_notifications", true)) {
                notificationBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
            }

            int rand = new Random().nextInt();
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // Notification ID allows you to update the notification later on
            nm.notify(rand, notificationBuilder.build());

        }

    }

}
