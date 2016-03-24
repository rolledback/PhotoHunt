package mrayer.photohunt;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

/**
 * Created by Matthew on 3/17/2016.
 */

@ParseClassName("PhotoHuntAlbum")
public class PhotoHuntAlbum extends ParseObject {

    public PhotoHuntAlbum() {
        // required default constructor
    }

    public String getName() {
        return getString("name");
    }

    public void setName(String name) {
        put("name", name);
    }

    public String getAuthor() {
        return getString("author");
    }

    public void setAuthor(String author) {
        put("author", author);
    }

    public String getLocation() {
        return getString("location");
    }

    public void setLocation(String location) {
        put("location", location);
    }

    public String getDescription() {
        return getString("description");
    }

    public void setDescription(String description) {
        put("description", description);
    }

    public String getType() {
        return getString("type");
    }

    public void setType(String type) {
        put("type", type);
    }

    public String getAlbumId() {
        return getString("albumId");
    }

    public void setAlbumId(String albumId) {
        put("albumId", albumId);
    }

    public int getNumPhotos() {
        return getInt("numPhotos");
    }

    public void setNumPhotos(int numPhotos) {
        put("numPhotos", numPhotos);
    }

    public ParseFile getCoverPhoto() {
        return getParseFile("coverPhoto");
    }

    public void setCoverPhoto(ParseFile file) {
        put("coverPhoto", file);
    }
}
