package mrayer.photohunt;

import android.app.ProgressDialog;
import android.util.Log;

import com.parse.ProgressCallback;

/**
 * Created by Matthew on 3/17/2016.
 */
public class PhotoProgressCallback implements ProgressCallback {

    private ProgressDialog parentDialog;
    private int index;
    private int numImages;

    public PhotoProgressCallback(ProgressDialog parentDialog, int index, int numImages) {
        this.parentDialog = parentDialog;
        this.index = index;
        this.numImages = numImages;
    }

    @Override
    public void done(Integer percentDone) {
        Log.d(Constants.CreateNewPhotoHunt_Tag, Integer.toString(percentDone));
        parentDialog.setProgress((100 / numImages * index) + (percentDone / numImages));
        Log.d(Constants.PhotoProgressCallback, Integer.toString(parentDialog.getProgress()) + " " + Integer.toString(index));
    }
}
