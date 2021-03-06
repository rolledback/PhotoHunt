package mrayer.photohunt;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    public String getAuthorId() {
        return getString("authorId");
    }

    public void setAuthorId(String authorId) {
        put("authorId", authorId);
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

    public ParseFile getCoverPhotoThumbnail() {
        return getParseFile("coverPhotoThumbnail");
    }

    public void setCoverPhotoThumbnail(ParseFile file) {
        put("coverPhotoThumbnail", file);
    }

    public boolean isPrivate() {
        return getBoolean("isPrivate");
    }

    public void setIsPrivate(Boolean isPrivate) {
        put("isPrivate", isPrivate);
    }

    public List<String> getWhiteList() {
        return (ArrayList<String>)get("whiteList");
    }

    public void setWhiteList(Set<String> whiteList) {
        List<String> temp = new ArrayList<String>();
        temp.addAll(whiteList);
        put("whiteList", temp);
    }

    public double getAvgReview() {
        return getDouble("avgReview");
    }

    public void setAvgReview(double avgReview) {
        put("avgReview", avgReview);
    }

    public int getNumReviews() {
        return getInt("numReviews");
    }

    public void setNumReviews(int numReviews) {
        put("numReviews", numReviews);
    }

    public String getSearchName() {
        return getString("searchName");
    }

    public void setSearchName(String searchName) {
        put("searchName", searchName);
    }

    public String getSearchAuthor() {
        return getString("searchAuthor");
    }

    public void setSearchAuthor(String searchAuthor) {
        put("searchAuthor", searchAuthor);
    }

    public String getSearchLocation() {
        return getString("searchLocation");
    }

    public void setSearchLocation(String searchLocation) {
        put("searchLocation", searchLocation);
    }

    public String getSearchDescription() {
        return getString("searchDescription");
    }

    public void setSearchDescription(String searchDescription) {
        put("searchDescription", searchDescription);
    }
}
