package mrayer.photohunt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class GridViewImageAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<String> filePaths = new ArrayList<String>();
    private int imageWidth;

    public GridViewImageAdapter(Activity activity, ArrayList<String> filePaths,
                                int imageWidth) {
        this.activity = activity;
        this.filePaths = filePaths;
        this.imageWidth = imageWidth;
    }

    @Override
    public int getCount() {
        return this.filePaths.size();
    }

    @Override
    public Object getItem(int position) {
        return this.filePaths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(activity);
        } else {
            imageView = (ImageView) convertView;
        }

        // get screen dimensions
        // TODO: figure out how to get pictures as files from Parse from Matt
//        Bitmap image = Utils.decodeFile(NEEDS TO BE A FILE, imageWidth,
//                imageWidth);

        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new GridView.LayoutParams(imageWidth,
                imageWidth));
//        imageView.setImageBitmap(image);

        // image view click listener
        imageView.setOnClickListener(new OnImageClickListener(position));

        return imageView;
    }

    class OnImageClickListener implements OnClickListener {

        int postion;

        // constructor
        public OnImageClickListener(int position) {
            this.postion = position;
        }

        @Override
        public void onClick(View v) {
            // on selecting grid view image
            // launch full screen activity
            // TODO: IMPLEMENT THIS CLICK
//            Intent i = new Intent(activity, FullScreenViewActivity.class);
//            i.putExtra("position", postion);
//            activity.startActivity(i);
        }

    }

}