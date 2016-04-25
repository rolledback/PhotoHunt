package mrayer.photohunt;

import com.parse.ParseObject;

/**
 * Created by Matthew on 4/25/2016.
 */
public class Review extends ParseObject {

    public Review() {
        // required default constructor
    }

    public String getAuthor() {
        return getString("author");
    }

    public void setAuthor(String author) {
        put("author", author);
    }

    public double getRating() {
        return getDouble("rating");
    }

    public void setRating(double rating) {
        if(rating % 0.5 != 0) {
            return;
        }
        put("rating", rating);
    }

    public String getAlbum() {
        return getString("albumId");
    }

    public void setAlbum(String albumId) {
        put("albumId", albumId);
    }

    public String getText() {
        return getString("text");
    }

    public void setText(String text) {
        put("text", text);
    }
}
