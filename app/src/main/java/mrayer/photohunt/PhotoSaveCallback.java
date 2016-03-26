package mrayer.photohunt;

import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.SaveCallback;

/**
 * Created by Matthew on 3/17/2016.
 * Custom Parse callback for the uploading of photos. Makes it easier
 * to get information needed for the saved of the associated Parse Object.
 */
public class PhotoSaveCallback implements SaveCallback {

    private Photo photoObject;
    private ParseFile fullPhoto;
    private ParseFile thumbnailPhoto;
    private PhotoHuntAlbum album;
    private int numPhotoOfThumbnail;
    private PhotoUploadProgressDialog dialog;

    // standard callback constructor
    public PhotoSaveCallback(ParseFile fullPhoto, ParseFile thumbnailPhoto, Photo photoObject, int numPhotoOfThumbnail, PhotoUploadProgressDialog dialog) {
        this.photoObject = photoObject;
        this.fullPhoto = fullPhoto;
        this.thumbnailPhoto = thumbnailPhoto;
        this.album = null;
        this.numPhotoOfThumbnail = numPhotoOfThumbnail;
        this.dialog = dialog;
    }

    // callback constructor for a cover photo
    public PhotoSaveCallback(ParseFile fullPhoto, ParseFile thumbnailPhoto, Photo photoObject, PhotoHuntAlbum album, int numPhotoOfThumbnail, PhotoUploadProgressDialog dialog) {
        this.photoObject = photoObject;
        this.fullPhoto = fullPhoto;
        this.thumbnailPhoto = thumbnailPhoto;
        this.album = album;
        this.numPhotoOfThumbnail = numPhotoOfThumbnail;
        this.dialog = dialog;
    }

    @Override
    public void done(ParseException e) {
        if (e != null) {
            Log.d(Constants.PhotoSaveCallback_Tag, e.toString());
        }
        else {
            // save the thumbnail next
            thumbnailPhoto.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Log.d(Constants.PhotoSaveCallback_Tag, e.toString());
                    }
                    else {
                        // both photo have been saved, ok to save the photo object
                        photoObject.setPhoto(fullPhoto);
                        photoObject.setThumbnail(thumbnailPhoto);
                        photoObject.saveInBackground();
                        if(album != null) {
                            // save the album if it was passed in
                            album.setCoverPhoto(fullPhoto);
                            album.setCoverPhotoThumbnail(thumbnailPhoto);
                            album.saveInBackground();
                        }
                    }
                }
            }, new PhotoProgressCallback(dialog, numPhotoOfThumbnail));
        }
    }
}
