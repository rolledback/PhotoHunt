package mrayer.photohunt;

import com.parse.ProgressCallback;

/**
 * Created by Matthew on 3/17/2016.
 */
public class PhotoProgressCallback implements ProgressCallback {

    private UploadProgressNotification parentDialog;
    private int index;

    public PhotoProgressCallback(UploadProgressNotification parentDialog, int index) {
        this.parentDialog = parentDialog;
        this.index = index;
    }

    @Override
    public void done(Integer percentDone) {
        // Log.d(Constants.PhotoProgressCallbackTag, this.hashCode() + " is updating the percentage of photo " + index + " to " + percentDone);
        parentDialog.setPhotoProgresses(percentDone, index);
    }
}
