package mrayer.photohunt;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDescriptor;
import com.drew.metadata.exif.GpsDirectory;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matthew on 3/16/2016.
 */
public class Utils {

    // Decodes image and scales it to reduce memory consumption
    public static Bitmap decodeFile(File f, int requiredWidth, int requiredHeight) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= requiredWidth && o.outHeight / scale / 2 >= requiredHeight) {
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            Bitmap retBitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
            return retBitmap;
        }
        catch (FileNotFoundException e) {
            Log.d(Constants.UtilsTag, "Unable to decode image file");
            Log.d(Constants.UtilsTag, e.toString());
        }
        return null;
    }

    public static LatLng getImageLocation(File f) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(f);
            Directory directory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            if(directory == null) {
                return null;
            }

            GpsDescriptor descriptor = new GpsDescriptor((GpsDirectory)directory);

            // get the latitude and convert
            String latitudeClock = descriptor.getGpsLatitudeDescription();
            double latitudeDecimal = convertFromClockToDecimal(latitudeClock);

            // get the longitude and convert
            String longitudeClock = descriptor.getGpsLongitudeDescription();
            double longitudeDecimal = convertFromClockToDecimal(longitudeClock);

            return new LatLng(latitudeDecimal, longitudeDecimal);
        }
        catch (Exception e) {
            Log.d(Constants.UtilsTag, e.toString());
        }
        return null;
    }

    private static double convertFromClockToDecimal(String coordinate) {
        String[] parts = coordinate.split(" ");
        double degrees = Double.parseDouble(parts[0].substring(0, parts[0].length() - 1));
        double minutes = Double.parseDouble(parts[1].substring(0, parts[1].length() - 1));
        double seconds = Double.parseDouble(parts[2].substring(0, parts[2].length() - 1));
        return Math.signum(degrees) * (Math.abs(degrees) + (minutes / 60.0) + (seconds / 3600.0));
    }

    /*
     * getting screen width
     */
    public static int getScreenWidth(Context context) {
        int columnWidth;
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        final Point point = new Point();
        try {
            display.getSize(point);
        } catch (java.lang.NoSuchMethodError ignore) { // Older device
            point.x = display.getWidth();
            point.y = display.getHeight();
        }
        columnWidth = point.x;
        return columnWidth;
    }

    public static byte[] compressImage(Bitmap bitmap, Bitmap.CompressFormat format) {
        int streamLength = Constants.MAX_IMAGE_SIZE;
        int compressQuality = 105;

        ByteArrayOutputStream bmpStream = new ByteArrayOutputStream();
        while (streamLength >= Constants.MAX_IMAGE_SIZE && compressQuality > 5) {
            try {
                bmpStream.flush();//to avoid out of memory error
                bmpStream.reset();
            } catch (IOException e) {
                e.printStackTrace();
            }
            compressQuality -= 5;
            bitmap.compress(format, compressQuality, bmpStream);
            byte[] bmpPicByteArray = bmpStream.toByteArray();
            streamLength = bmpPicByteArray.length;
        }

        return bmpStream.toByteArray();
    }

    public static Bitmap.CompressFormat determineCompresionFormat(String fileName) {
        fileName = fileName.toLowerCase();
        if(fileName.endsWith(".png")) {
            return Bitmap.CompressFormat.PNG;
        }
        else if(fileName.endsWith(".jpeg") || fileName.endsWith(".jpg")) {
            return Bitmap.CompressFormat.JPEG;
        }
        return null;
    }

    public static Bitmap createThumbnail(Bitmap bitmap) {
        Bitmap resized = ThumbnailUtils.extractThumbnail(bitmap, Constants.THUMBNAIL_WIDTH, Constants.THUMBNAIL_HEIGHT);
        return resized;
    }

    public static Bitmap imageOrientationValidator(Bitmap bitmap, String path) {

        ExifInterface ei;
        try {
            ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    bitmap = rotateImage(bitmap, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    bitmap = rotateImage(bitmap, 180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    bitmap = rotateImage(bitmap, 270);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {

        Bitmap bitmap = null;
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        try {
            bitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                    matrix, true);
        } catch (OutOfMemoryError err) {
            err.printStackTrace();
        }
        return bitmap;
    }

    public static void copyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
                int count=is.read(bytes, 0, buffer_size);
                if(count==-1)
                    break;
                os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }

    public static boolean validImage(String file) {
        file = file.toLowerCase();
        return file.endsWith(".jpg") || file.endsWith(".jpeg") || file.endsWith(".png");
    }

    public static ParseQuery<PhotoHuntAlbum> makeGeneralQuery() {
        ParseQuery<PhotoHuntAlbum> queryNonPrivate = ParseQuery.getQuery("PhotoHuntAlbum");
        queryNonPrivate.whereEqualTo("isPrivate", false);

        ParseQuery<PhotoHuntAlbum> queryPrivate = ParseQuery.getQuery("PhotoHuntAlbum");
        queryPrivate.whereEqualTo("isPrivate", true);
        queryPrivate.whereEqualTo("whiteList", ParseUser.getCurrentUser().getUsername());

        List<ParseQuery<PhotoHuntAlbum>> queries = new ArrayList<ParseQuery<PhotoHuntAlbum>>();
        queries.add(queryNonPrivate);
        queries.add(queryPrivate);

        ParseQuery<PhotoHuntAlbum> combinedQuery = ParseQuery.or(queries);
        combinedQuery.orderByDescending("createdAt");

        return combinedQuery;
    }

    public static void resetCurrentAlbumPrefs(Context context, SharedPreferences currentAlbumPref) {
        SharedPreferences.Editor editor = currentAlbumPref.edit();
        editor.putInt(context.getString(R.string.total_photos), -1);
        editor.putInt(context.getString(R.string.photos_found), -1);
        editor.putString(context.getString(R.string.album_id), "" + -1);
        editor.putBoolean(context.getString(R.string.currently_have_active_hunt), false);
        editor.commit();
    }
}
