package mrayer.photohunt;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import java.util.Arrays;

import mrayer.photohunt.Constants;

/**
 * Created by Matthew on 3/17/2016.
 * Wrapper for a ProgressDialog. Enables us to combine the progress of multiple
 * photos being uploaded into one progress.
 */
public class PhotoUploadProgressDialog {

    private int[] progresses;
    private ProgressDialog dialog;

    public PhotoUploadProgressDialog(Context c, int numPhotos) {
        dialog = new ProgressDialog(c);
        progresses = new int[numPhotos];
    }

    public void setPhotoProgresses(int percentage, int photo) {
        Log.d(Constants.PhotoUploadProgressDialog_Tag, "Updating progress.");
        progresses[photo] = percentage;
        int totalProgress = 0;
        for(int i = 0; i < progresses.length; i++) {
            // take care of rounding issues
            totalProgress += (int)Math.ceil((double)progresses[i] / (double)progresses.length);
        }
        dialog.setProgress(totalProgress);

        if(dialog.getProgress() == 100) {
            dialog.setCancelable(true);
            dialog.setMessage("Uploading...complete!");
        }
    }

    public void setup() {
        dialog.setMessage("Uploading...");
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setProgress(0);
        dialog.setMax(100);
        dialog.setCancelable(false);
        dialog.show();
    }
}
