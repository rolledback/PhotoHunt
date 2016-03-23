package mrayer.photohunt;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import mrayer.photohunt.Constants;

/**
 * Created by Matthew on 3/17/2016.
 * Custom Parse callback for the uploading of photos. Makes it easier
 * to get information needed for the saved of the associated Parse Object.
 */
public class PhotoSaveCallback implements SaveCallback {

    private String albumId;
    private ParseFile photo;
    private LatLng location;
    private PhotoHuntAlbum album;
    private int index;

    // standard callback constructor
    public PhotoSaveCallback(String albumId, ParseFile photo, LatLng location, int index) {
        this.albumId = albumId;
        this.photo = photo;
        this.location = location;
        this.index = index;
        this.album = null;
    }

    // callback constructor for a cover photo
    public PhotoSaveCallback(String albumId, ParseFile photo, LatLng location, int index, PhotoHuntAlbum album) {
        this.albumId = albumId;
        this.photo = photo;
        this.location = location;
        this.index = index;
        this.album = album;
    }

    @Override
    public void done(ParseException e) {
        if (e != null) {
            Log.d(Constants.PhotoSaveCallback_Tag, e.toString());
        } else {
            ParseObject photoObject = new ParseObject("Photo");
            photoObject.put("albumId", albumId);
            photoObject.put("photo", photo);
            photoObject.put("index", index);
            if(location != null) {
                photoObject.put("location", new ParseGeoPoint(location.latitude, location.longitude));
            }
            photoObject.saveInBackground();

            if(album != null) {
                album.setCoverPhoto(photo);
                album.saveInBackground();
            }
        }
    }
}
