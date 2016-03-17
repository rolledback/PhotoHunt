package mrayer.photohunt;

/**
 * Created by cjkim on 3/8/16.
 */

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class ImageAdapter extends PagerAdapter {
    Context context;
    private ArrayList<String> galImages = new ArrayList<String>();

    ImageAdapter(Context context){
        this.context=context;
    }

    ImageAdapter(Context context, ArrayList<String> list) {
        this.context = context;
        galImages = list;
    }

    @Override
    public int getCount() {
        return galImages.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((ImageView) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imageView = new ImageView(context);
        int padding = context.getResources().getDimensionPixelSize(R.dimen.fab_margin);
        imageView.setPadding(padding, padding, padding, padding);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        if (galImages == null) {
            Log.e("IMAGE ADAPTER", "LIST IS NULL!!!!!");
        }
        else {
            Log.e("IMAGE ADAPTER", "LIST = " + galImages.toString());
        }
        String picturePath = galImages.get(position);
        File file = new File(picturePath);

        int height = (int) context.getResources().getDimension(R.dimen.create_new_photo_hunt_image_size);
        int width = context.getResources().getDisplayMetrics().widthPixels;

        Bitmap bitmap = ImageUtils.decodeFile(file, width, height);
        imageView.setImageBitmap(bitmap);
//        Uri uri = Uri.fromFile(file);


        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width, height);
        imageView.setLayoutParams(params);

//        imageView.setImageURI(uri);

        ((ViewPager) container).addView(imageView, 0);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((ImageView) object);
    }

    public ArrayList<String> getGalImages() {
        return galImages;
    }

    public void setGalImages(ArrayList<String> newGalImages) {
        galImages = newGalImages;
        Log.e("IMAGE ADAPTER", "IN SET GAL IMAGES, LIST = " + galImages.toString());
    }

}