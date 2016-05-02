package mrayer.photohunt;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.parse.ParseUser;

import java.util.Arrays;

/**
 * Created by Matthew on 3/17/2016.
 * Wrapper for a Notification w/a progress bar. Enables us to combine the progress of multiple
 * photos being uploaded into one progress.
 */
public class UploadProgressNotification {

    private int[] progresses;
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;
    private Messenger callbackMessenger;
    private int prevProgress;
    private SharedPreferences photoHuntPrefs;

    public UploadProgressNotification(Context c, int numPhotos, Messenger callbackMessenger) {
        // Log.d(Constants.UploadProgressNotificationTag, "Creating upload dialog with " + numPhotos + " photos.");
        progresses = new int[numPhotos];
        this.callbackMessenger = callbackMessenger;
        notificationManager = (NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder = new NotificationCompat.Builder(c);
        prevProgress = 0;
        photoHuntPrefs = c.getSharedPreferences(c.getString(R.string.current_album_pref) + "-" + ParseUser.getCurrentUser().getObjectId(),
                Context.MODE_PRIVATE);
        buildNotification();
    }

    private void buildNotification() {
        notificationBuilder.setContentTitle("PhotoHunt Upload")
            .setContentText("Upload in progress..")
            .setSmallIcon(R.drawable.ic_file_upload_black_24dp)
            .setProgress(100, 0, false);
    }

    public synchronized void setPhotoProgresses(int percentage, int photo) {
        // Log.d(Constants.UploadProgressNotificationTag, "Set photo progress of photo " + photo + " to " + percentage);
        progresses[photo] = percentage;
        double totalProgressDouble = 0;
        for(int i = 0; i < progresses.length; i++) {
            totalProgressDouble += (double)progresses[i] / (double)progresses.length;
        }

        // handle rounding as best as we can
        int totalProgress = (int)Math.ceil(totalProgressDouble);

        if(totalProgress <= prevProgress || prevProgress >= 100) {
            // only update notification if there is a point, and only update to 100% once
            return;
        }
        prevProgress = totalProgress;

        // Log.d(Constants.UploadProgressNotificationTag, "Total progress is " + totalProgress);
        // Log.d(Constants.UploadProgressNotificationTag, Arrays.toString(progresses));
        notificationBuilder.setProgress(100, totalProgress, false);
        notificationManager.notify(1, notificationBuilder.build());

        if(totalProgress >= 100) {
            notificationBuilder.setContentText("Upload complete! ")
                .setSmallIcon(R.drawable.ic_done_black_24dp)
                .setProgress(0, 0, false);



            if(!photoHuntPrefs.getBoolean("mute_notifications", true)) {
                notificationBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
            }

            notificationManager.notify(1, notificationBuilder.build());

            Message msg = Message.obtain();

            msg.what = Constants.UPLOAD_COMPLETE;

            try {
                callbackMessenger.send(msg);
            }
            catch (android.os.RemoteException e1) {
                Log.d(Constants.UploadProgressNotificationTag, e1.toString());
            }
        }
    }
}
