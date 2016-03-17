package mrayer.photohunt;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDescriptor;
import com.drew.metadata.exif.GpsDirectory;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;

/**
 * Created by Matthew on 3/16/2016.
 */
public class ImageUtils {

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
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        }
        catch (FileNotFoundException e) {
            Log.d(Constants.ImageAdapter_Tag, "Unable to decode image file");
            Log.d(Constants.ImageAdapter_Tag, e.toString());
        }
        return null;
    }

    public static LatLng getImageLocation(File f) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(f);
            Directory directory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            GpsDescriptor descriptor = new GpsDescriptor((GpsDirectory)directory);

            // get the latitude and convert
            String latitudeClock = descriptor.getGpsLatitudeDescription();
            double latitudeDecimal = convertFromClockToDecimal(latitudeClock);

            // get the longitude and convert
            String longitudeClock = descriptor.getGpsLongitudeDescription();
            double longitudeDecimal = convertFromClockToDecimal(longitudeClock);

            return new LatLng(latitudeDecimal, latitudeDecimal);
        }
        catch (Exception e) {
            Log.d(Constants.ImageUtils_Tag, e.toString());
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
}
