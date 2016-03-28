package mrayer.photohunt;

/**
 * Created by Matthew on 3/16/2016.
 */
public class Constants {
    // log tags
    public static final String CreateNewPhotoHunt_Tag = "CREATE_HUNT";
    public static final String ImageAdapter_Tag = "IMAGE_ADAPTER";
    public static final String Utils_Tag = "UTILS";
    public static final String PhotoSaveCallback_Tag = "PHOTO_SAVE_CALLBACK";
    public static final String PhotoProgressCallback_Tag = "PHOTO_PROGRESS_CALLBACK";
    public static final String PhotoUploadProgressDialog_Tag = "PHOTO_PROGRESS_DIALOG";
    public static final String AlbumGallery_Tag = "ALBUM_GALLERY";
    public static final String SetChangeLocation_Tag = "SET_CHANGE_LOCATION";
    public static final String FileCache_Tag = "FILE_CACHE";
    public static final String MemoryCacheTag = "MEMORY_CACHE";
    public static final String SignUpTag = "SIGN_UP";
    public static final String AccountTag ="ACCOUNT";

    // start activity for result codes for CreateNewPhotoHuntActivity
    public static final int REQUEST_LOAD_IMAGE = 1;
    public static final int REQUEST_IMAGE_CAPTURE = 2;
    public static final int REQUEST_SET_ADD_LOCATION = 3;

    // start activity for result codes for AlbumGalleryActivity
    public static final int REQUEST_CREATE_NEW_PHOTO_HUNT = 1;

    // Number of columns of Album Grid View
    public static final int NUM_OF_COLUMNS = 3;

    // Gridview image padding
    public static final int GRID_PADDING = 8; // in dp

    // Max uploadable image size
    public static final int MAX_IMAGE_SIZE = 1000 * 1024;

    // Cache directory name
    public static final String CACHE_NAME = "PhotoHunt";

    // Thumbnail dimensions
    public static final int THUMBNAIL_WIDTH = 500;
    public static final int THUMBNAIL_HEIGHT = 500;
}
