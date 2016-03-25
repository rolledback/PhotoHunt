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

    public PhotoProgressCallback(PhotoUploadProgressDialog parentDialog, int index) {
        this.parentDialog = parentDialog;
        this.index = index;
    }

    @Override
    public void done(Integer percentDone) {
        parentDialog.setPhotoProgresses(percentDone, index);
    }
}
