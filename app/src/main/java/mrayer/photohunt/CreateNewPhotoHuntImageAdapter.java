package mrayer.photohunt;

/**
 * Created by cjkim on 3/8/16.
 */

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class CreateNewPhotoHuntImageAdapter extends PagerAdapter {
    private Context context;
    private ArrayList<String> galImages = new ArrayList<String>();
    private ArrayList<Integer> galImageViews = new ArrayList<Integer>();

    // file path - > manual location
    private HashMap<String, LatLng> manualLocations;

    // file path -> meta location
    private HashMap<String, LatLng> metaLocations;

    private LayoutInflater inflater;
    private AlertDialog.Builder dialogBuilder;

    // I hate myself for doing this
    private CreateNewPhotoHuntActivity parent;

    CreateNewPhotoHuntImageAdapter(Context context, LayoutInflater inflater, AlertDialog.Builder dialogBuilder, CreateNewPhotoHuntActivity parent){
        this.context = context;
        this.inflater = inflater;
        this.dialogBuilder = dialogBuilder;
        this.parent = parent;
        manualLocations = new HashMap<String, LatLng>();
        metaLocations = new HashMap<String, LatLng>();
    }

    CreateNewPhotoHuntImageAdapter(Context context, LayoutInflater inflater, AlertDialog.Builder dialogBuilder, ArrayList<String> list, CreateNewPhotoHuntActivity parent) {
        this.context = context;
        this.inflater = inflater;
        this.dialogBuilder = dialogBuilder;
        this.parent = parent;
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
    public Object instantiateItem(final ViewGroup container, int position) {
        ImageView imageView = new ImageView(context);
        int padding = context.getResources().getDimensionPixelSize(R.dimen.fab_margin);
        imageView.setPadding(padding, padding, padding, padding);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        final String picturePath = galImages.get(position);
        File file = new File(picturePath);

        // get location if it exists
        LatLng metaLocation = Utils.getImageLocation(file);
        if(metaLocation != null) {
            metaLocations.put(picturePath, metaLocation);
        }

        int height = (int) context.getResources().getDimension(R.dimen.create_new_photo_hunt_image_size);
        int width = context.getResources().getDisplayMetrics().widthPixels;

        Bitmap bitmap = Utils.decodeFile(file, width, height);
        Bitmap correctedBitmap = Utils.imageOrientationValidator(bitmap, picturePath);
        imageView.setImageBitmap(correctedBitmap);

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width, height);
        imageView.setLayoutParams(params);

        ((ViewPager) container).addView(imageView, 0);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parent.launchViewPhoto(picturePath);
            }
        });
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                dialogBuilder.setTitle("Photo Options");
                dialogBuilder.setItems(context.getResources().getStringArray(R.array.create_hunt_photo_dialog_options), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            parent.launchViewSetLocation();
                        }
                        else if (which == 1) {
                            if (((ViewPager) container).getCurrentItem() == 0) {
                                return;
                            }

                            String imageToMove = galImages.get(((ViewPager) container).getCurrentItem());
                            Integer viewToMove = galImageViews.get(((ViewPager) container).getCurrentItem());

                            galImages.remove(((ViewPager) container).getCurrentItem());
                            galImageViews.remove(((ViewPager) container).getCurrentItem());

                            galImages.add(0, imageToMove);
                            galImageViews.add(0, viewToMove);

                            notifyDataSetChanged();

                            ((ViewPager) container).setCurrentItem(0, true);
                        }
                        else if(which == 2) {
                            Integer viewToRemove = galImageViews.get(((ViewPager) container).getCurrentItem());

                            galImages.remove(((ViewPager) container).getCurrentItem());
                            galImageViews.remove(viewToRemove);

                            notifyDataSetChanged();

                            if(galImages.size() == 0) {
                                parent.hideViewPager();
                            }
                        }
                    }
                });
                dialogBuilder.show();

                return true;
            }
        });

        if(galImageViews.size() > position) {
            // if you are instantiating a view not at the end of the list
            galImageViews.set(position, imageView.hashCode());
        }
        else if(!galImageViews.contains(imageView)) {
            // if you are instantiating a view at the end which isn't already in the list
            galImageViews.add(imageView.hashCode());
        }

        return imageView;
    }

    @Override
    public int getItemPosition(Object object) {
        if(galImageViews.contains(((ImageView) object).hashCode())) {
            return galImageViews.indexOf(((ImageView) object).hashCode());
        }
        else {
            return POSITION_NONE;
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((ImageView) object);
    }

    public ArrayList<String> getGalImages() {
        return galImages;
    }

    public void setGalImages(ArrayList<String> newGalImages) {
        this.galImages = newGalImages;
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