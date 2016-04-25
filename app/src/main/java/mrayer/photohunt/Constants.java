package mrayer.photohunt;

/**
 * Created by Matthew on 3/16/2016.
 */
public class Constants {
    // log tags
    public static final String CreateNewPhotoHuntTag = "CREATE_HUNT_ACTIVITY";
    public static final String UtilsTag = "UTILS";
    public static final String PhotoSaveCallbackTag = "PHOTO_SAVE_CALLBACK";
    public static final String PhotoProgressCallbackTag = "PHOTO_PROGRESS_CALLBACK";
    public static final String UploadProgressNotificationTag = "UPLOAD_NOTIFICATION";
    public static final String AlbumGalleryTag = "ALBUM_GALLERY_ACTIVITY";
    public static final String SetChangeLocation_Tag = "SET_CHANGE_LOCATION";
    public static final String SignUpTag = "SIGN_UP_ACTIVITY";
    public static final String AccountTag ="ACCOUNT_ACTIVITY";
    public static final String AlbumListAdapterTag = "ALBUM_LIST_ADAPTER";
    public static final String AlbumGridAdapterTag = "ALBUM_GRID_ADAPTER";
    public static final String ViewPhotoActivityTag = "VIEW_PHOTO_ACTIVITY";
    public static final String DetailedPhotoHuntActivityTag = "DETAILED_PHOTO_HUNT";
    public static final String UserListAdapterTag = "USER_LIST_ADAPTER";
    public static final String AlbumSearchResultsAdapterTag = "ALBUM_SEARCH_RESULTS";
    public static final String UserSearchActivityTag = "USER_SEARCH_ACTIVITY";
    public static final String FavoriteUsersActivityTag = "FAV_USERS_ACTIVITY";

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

    // Thumbnail dimensions
    public static final int THUMBNAIL_WIDTH = 500;
    public static final int THUMBNAIL_HEIGHT = 500;

    // start activity for result code for AccountActivity
    public static final int REQUEST_MANAGEMENT_RESULT = 1;

    // result codes for DetailedPhotoHuntActivity
    public static final int NO_RESULT = 1;
    public static final int DELETE_RESULT = 2;

    // start activity for result code for CurrentPhotoHuntActivity
    public static final int REQUEST_CURRENT_RESULT = 1;

    // result codes for CurrentPhotoHuntActivity
    public static final int ENDED_HUNT = 1;
    public static final int CONTINUE_HUNT = 2;

    // message codes
    public static final int UPLOAD_COMPLETE = 1;
}
