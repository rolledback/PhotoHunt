package mrayer.photohunt;

/**
 * Created by cjkim on 3/8/16.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

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
        String picturePath = galImages.get(position);
        File file = new File(picturePath);

        int height = (int) context.getResources().getDimension(R.dimen.create_new_photo_hunt_image_size);
        int width = context.getResources().getDisplayMetrics().widthPixels;

        Bitmap bitmap = Utils.decodeFile(file, width, height);
        imageView.setImageBitmap(bitmap);

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width, height);
        imageView.setLayoutParams(params);

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
    }

}