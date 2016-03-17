package mrayer.photohunt;

import android.provider.ContactsContract;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.SaveCallback;

/**
 * Created by Matthew on 3/17/2016.
 * Custom Parse callback for the uploading of photos. Makes it easier
 * to get information needed for the saved of the associated Parse Object.
 */
public class PhotoSaveCallback implements SaveCallback {

    private String albumId;
    private ParseFile photo;
    private LatLng location;
    private boolean isCover;

    public PhotoSaveCallback(String albumId, ParseFile photo, LatLng location, boolean isCover) {
        this.albumId = albumId;
        this.photo = photo;
        this.location = location;
        this.isCover = isCover;
    }

    @Override
    public void done(ParseException e) {
        if (e != null) {
            Log.d(Constants.PHotoSaveCallback, e.toString());
        } else {
            ParseObject photoObject = new ParseObject("Photo");
            photoObject.add("album_id", albumId);
            photoObject.add("photo", photo);
            photoObject.add("is_cover", isCover);

            if(location != null) {
                photoObject.put("location", new ParseGeoPoint(location.latitude, location.longitude));
            }

            photoObject.saveInBackground();
        }
    }
}
