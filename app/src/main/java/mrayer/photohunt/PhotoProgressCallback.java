package mrayer.photohunt;

import android.app.ProgressDialog;
import android.util.Log;

import com.parse.ProgressCallback;

/**
 * Created by Matthew on 3/17/2016.
 */
public class PhotoProgressCallback implements ProgressCallback {

    private PhotoUploadProgressDialog parentDialog;
    private int index;
    private int numImages;

    public PhotoProgressCallback(PhotoUploadProgressDialog parentDialog, int index, int numImages) {
        this.parentDialog = parentDialog;
        this.index = index;
        this.numImages = numImages;
    }

    @Override
    public void done(Integer percentDone) {
        parentDialog.setPhotoProgresses(percentDone, index);
    }
}
