package mrayer.photohunt;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

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
}
