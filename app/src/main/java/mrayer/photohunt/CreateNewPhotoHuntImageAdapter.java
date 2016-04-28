package mrayer.photohunt;

/**
 * Created by cjkim on 3/8/16.
 */

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class CreateNewPhotoHuntImageAdapter extends PagerAdapter {
    private ArrayList<String> galImages = new ArrayList<String>();
    private ArrayList<Integer> galImageViews = new ArrayList<Integer>();
    private ViewGroup currContainer;

    // file path - > manual location
    private HashMap<String, LatLng> manualLocations;

    // file path -> meta location
    private HashMap<String, LatLng> metaLocations;

    private LayoutInflater inflater;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog photoDialog;

    // I hate myself for doing this
    private CreateNewPhotoHuntActivity parent;

    CreateNewPhotoHuntImageAdapter(CreateNewPhotoHuntActivity parent){
        this.parent = parent;
        this.inflater = parent.getLayoutInflater();
        this.dialogBuilder = new AlertDialog.Builder(parent);
        this.parent = parent;
        manualLocations = new HashMap<String, LatLng>();
        metaLocations = new HashMap<String, LatLng>();
        currContainer = null;
        setupDialog();
    }

    @Override
    public int getCount() {
        return galImages.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((FrameLayout) object);
    }

    @Override
    public Object instantiateItem(final ViewGroup container, int position) {
        ViewHolder holder = new ViewHolder();
        View view = LayoutInflater.from(parent).inflate(R.layout.create_photo_hunt_view_pager, container, false);
        currContainer = container;

        holder.photo = (ImageView) view.findViewById(R.id.main_image);
        holder.photo.setVisibility(View.INVISIBLE);
        holder.locStatus = (ImageView) view.findViewById(R.id.location_status);
        holder.locStatus.setVisibility(View.INVISIBLE);
        holder.spinner = (ProgressBar) view.findViewById(R.id.spinner);

        ImageView imageView = holder.photo;
        int padding = parent.getResources().getDimensionPixelSize(R.dimen.fab_margin);
        imageView.setPadding(padding, padding, padding, padding);

        final String picturePath = galImages.get(position);
        File file = new File(picturePath);

        int height = (int) parent.getResources().getDimension(R.dimen.create_new_photo_hunt_image_size);
        int width = parent.getResources().getDisplayMetrics().widthPixels;

        new OpenImageTask(picturePath, width, height, holder).execute();

        // get location if it exists
        LatLng metaLocation = Utils.getImageLocation(file);
        LatLng manLocation = manualLocations.get(picturePath);
        if(metaLocation != null) {
            holder.locStatus.setImageResource(R.drawable.ic_done_green_24dp);
            metaLocations.put(picturePath, metaLocation);
        }
        else if(manLocation != null) {
            holder.locStatus.setImageResource(R.drawable.ic_done_green_24dp);
        }
        else {
            holder.locStatus.setImageResource(R.drawable.ic_report_problem_black_24dp);
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parent.launchViewPhoto(picturePath);
            }
        });
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                photoDialog.show();
                return true;
            }
        });

        view.setTag(holder);
        ((ViewPager) container).addView(view, 0);

        if(galImageViews.size() > position) {
            // if you are instantiating a view not at the end of the list
            galImageViews.set(position, imageView.hashCode());
        }
        else if(!galImageViews.contains(imageView)) {
            // if you are instantiating a view at the end which isn't already in the list
            galImageViews.add(imageView.hashCode());
            if(metaLocation == null && manLocation == null) {
                parent.makeAndShowSetLocationHintToast();
            }
        }

        return view;
    }

    @Override
    public int getItemPosition(Object object) {
        if(galImageViews.contains(((FrameLayout) object).hashCode())) {
            return galImageViews.indexOf(((FrameLayout) object).hashCode());
        }
        else {
            return POSITION_NONE;
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((FrameLayout) object);
    }

    private void setupDialog() {
        dialogBuilder.setTitle("Photo Options");
        String[] menuOptions = parent.getResources().getStringArray(R.array.create_hunt_photo_dialog_options);
        dialogBuilder.setItems(menuOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    parent.launchViewSetLocation();
                } else if (which == 1) {
                    if (((ViewPager) currContainer).getCurrentItem() == 0) {
                        return;
                    }

                    String imageToMove = galImages.get(((ViewPager) currContainer).getCurrentItem());
                    Integer viewToMove = galImageViews.get(((ViewPager) currContainer).getCurrentItem());

                    galImages.remove(((ViewPager) currContainer).getCurrentItem());
                    galImageViews.remove(((ViewPager) currContainer).getCurrentItem());

                    galImages.add(0, imageToMove);
                    galImageViews.add(0, viewToMove);

                    notifyDataSetChanged();

                    ((ViewPager) currContainer).setCurrentItem(0, true);
                } else if (which == 2) {
                    Integer viewToRemove = galImageViews.get(((ViewPager) currContainer).getCurrentItem());

                    galImages.remove(((ViewPager) currContainer).getCurrentItem());
                    galImageViews.remove(viewToRemove);

                    notifyDataSetChanged();

                    if (galImages.size() == 0) {
                        parent.hideViewPager();
                    }
                }
            }
        });
        photoDialog = dialogBuilder.create();
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

    public void addManualLocation(String filePath, LatLng loc) {
        manualLocations.put(filePath, loc);
        notifyDataSetChanged();
    }

    public void addMetaLocation(String filePath, LatLng loc) {
        metaLocations.put(filePath, loc);
        notifyDataSetChanged();
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

    static class ViewHolder {
        ImageView photo;
        ImageView locStatus;
        ProgressBar spinner;
    }

    private static class OpenImageTask extends AsyncTask<Void, Void, Bitmap> {

        private File imageFile;
        private String path;
        private int width, height;
        private ViewHolder holder;

        public OpenImageTask(String path, int width, int height, ViewHolder holder) {
            this.path = path;
            this.width = width;
            this.height = height;
            this.holder = holder;
            imageFile = new File(path);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            holder.photo.setImageBitmap(bitmap);
            holder.photo.setVisibility(View.VISIBLE);
            holder.locStatus.setVisibility(View.VISIBLE);
            holder.spinner.setVisibility(View.GONE);
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            Bitmap bitmap = Utils.decodeFile(imageFile, width, height);
            Bitmap correctedBitmap = Utils.imageOrientationValidator(bitmap, path);

            return correctedBitmap;
        }
    }
}