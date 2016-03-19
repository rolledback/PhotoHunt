package mrayer.photohunt;

/**
 * Created by Matthew on 3/16/2016.
 */
public class Constants {
    // log tags
    public static final String CreateNewPhotoHunt_Tag = "CREATE_HUNT";
    public static final String ImageAdapter_Tag = "IMAGE_ADAPTER";
    public static final String ImageUtils_Tag = "IMAGE_UTILS";
    public static final String PhotoSaveCallback_Tag = "PHOTO_SAVE_CALLBACK";
    public static final String PhotoProgressCallback_Tag = "PHOTO_PROGRESS_CALLBACK";
    public static final String PhotoUploadProgressDialog_Tag = "PHOTO_PROGRESS_DIALOG";
    public static final String AlbumGallery_Tag = "ALBUM_GALLERY";

    // start activity for result codes for CreateNewPhotoHuntActivity
    public static final int REQUEST_LOAD_IMAGE = 1;
    public static final int REQUEST_IMAGE_CAPTURE = 2;
    public static final int REQUEST_SET_ADD_LOCATION = 3;

    // Number of columns of Album Grid View
    public static final int NUM_OF_COLUMNS = 3;

    // Gridview image padding
    public static final int GRID_PADDING = 8; // in dp
}
