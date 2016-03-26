package mrayer.photohunt;

/**
 * Created by cjkim on 3/8/16.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class CreateNewPhotoHuntImageAdapter extends PagerAdapter {
    Context context;
    private ArrayList<String> galImages = new ArrayList<String>();

    // file path - > manual location
    private HashMap<String, LatLng> manualLocations;

    // file path -> meta location
    private HashMap<String, LatLng> metaLocations;

    CreateNewPhotoHuntImageAdapter(Context context){
        this.context=context;
        manualLocations = new HashMap<String, LatLng>();
        metaLocations = new HashMap<String, LatLng>();
    }

    CreateNewPhotoHuntImageAdapter(Context context, ArrayList<String> list) {
        this.context = context;
        galImages = list;
        manualLocations = new HashMap<String, LatLng>();
        metaLocations = new HashMap<String, LatLng>();
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

        // get location if it exists
        LatLng metaLocation = Utils.getImageLocation(file);
        if(metaLocation != null) {
            metaLocations.put(picturePath, metaLocation);
        }

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

    public LatLng getManualLocation(String filePath) {
        if(!manualLocations.containsKey(filePath)) {
            return null;
        }
        return manualLocations.get(filePath);
    }

    public LatLng getMetaLocation(String filePath) {
        if(!metaLocations.containsKey(filePath)) {
            return null;
        }
        return metaLocations.get(filePath);
    }

    public LatLng addManualLocation(String filePath, LatLng loc) {
        return manualLocations.put(filePath, loc);
    }

    public LatLng addMetaLocation(String filePath, LatLng loc) {
        return metaLocations.put(filePath, loc);
    }

    public LatLng getLocation(String filePath) {
        if(manualLocations.containsKey(filePath)) {
            return manualLocations.get(filePath);
        }
        else if(metaLocations.containsKey(filePath)){
            return metaLocations.get(filePath);
        }
        else {
            return null;
        }
    }

}