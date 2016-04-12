package mrayer.photohunt;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.util.Arrays;

import mrayer.photohunt.Constants;

/**
 * Created by Matthew on 3/17/2016.
 * Wrapper for a Notification w/a progress bar. Enables us to combine the progress of multiple
 * photos being uploaded into one progress.
 */
public class UploadProgressNotification {

    private int[] progresses;
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;

    public UploadProgressNotification(Context c, int numPhotos) {
        // Log.d(Constants.PhotoUploadProgressDialogTag, "Creating upload dialog with " + numPhotos + " photos.");
        progresses = new int[numPhotos];
        notificationManager = (NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder = new NotificationCompat.Builder(c);
        buildNotification();
    }

    private void buildNotification() {
        notificationBuilder.setContentTitle("PhotoHunt Upload");
        notificationBuilder.setContentText("Upload in progress..");
        notificationBuilder.setSmallIcon(R.drawable.ic_file_upload_black_24dp);
        notificationBuilder.setProgress(100, 0, false);
    }

    public void setPhotoProgresses(int percentage, int photo) {
        // Log.d(Constants.PhotoUploadProgressDialogTag, "Set photo progress of photo " + photo + " to " + percentage);
        progresses[photo] = percentage;
        int totalProgress = 0;
        for(int i = 0; i < progresses.length; i++) {
            // take care of rounding issues
            totalProgress += (int)Math.ceil((double)progresses[i] / (double)progresses.length);
        }
        // Log.d(Constants.PhotoUploadProgressDialogTag, "Total progress is " + totalProgress);
        notificationBuilder.setProgress(100, totalProgress, false);
        notificationManager.notify(1, notificationBuilder.build());

        if(totalProgress >= 100) {
            notificationBuilder.setContentText("Upload complete!");
            notificationBuilder.setSmallIcon(R.drawable.ic_done_black_24dp);
            notificationBuilder.setProgress(0, 0, false);
            notificationManager.notify(1, notificationBuilder.build());
        }
    }
}
