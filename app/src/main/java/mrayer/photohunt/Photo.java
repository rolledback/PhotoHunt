package mrayer.photohunt;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

/**
 * Created by Matthew on 3/25/2016.
 */
@ParseClassName("Photo")
public class Photo extends ParseObject {

    public Photo() {
        // required default constructor
    }

    public String getAlbumId() {
        return getString("albumId");
    }

    public void setAlbumId(String albumId) {
        put("albumId", albumId);
    }

    public int getIndex() {
        return getInt("index");
    }

    public void setIndex(int index) {
        put("index", index);
    }

    public LatLng getLocation() {
        ParseGeoPoint temp = getParseGeoPoint("location");
        return new LatLng(temp.getLatitude(), temp.getLongitude());
    }

    public void setLocation(LatLng location) {
        put("location", new ParseGeoPoint(location.latitude, location.longitude));
    }

    public ParseFile getPhoto() {
        return getParseFile("photo");
    }

    public void setPhoto(ParseFile photo) {
        put("photo", photo);
    }

    public ParseFile getThumbnail() {
        return getParseFile("thumbnail");
    }

    public void setThumbnail(ParseFile thumbnail) {
        put("thumbnail", thumbnail);
    }
}
