package mrayer.photohunt;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class GridViewImageAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> filePaths = new ArrayList<String>();
    private int imageWidth;
    private LayoutInflater inflater;
    ImageLoader imageLoader;

    public GridViewImageAdapter(Context context, ArrayList<String> filePaths,
                                int imageWidth) {
        this.context = context;
        this.filePaths = filePaths;
        this.imageWidth = imageWidth;

        inflater = LayoutInflater.from(context);
        imageLoader = new ImageLoader(context);
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

    public class ViewHolder {
        ImageView photo;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.album_gridview_photo, null);
            // Locate the ImageView in gridview_item.xml
            holder.photo = (ImageView) convertView.findViewById(R.id.album_photo);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        // Load image into GridView
        imageLoader.DisplayImage(filePaths.get(position),
                holder.photo);

        return convertView;



//
//
//        ParseImageView imageView;
//        if (convertView == null) {
//            imageView = new ParseImageView(activity);
//        } else {
//            imageView = (ParseImageView) convertView;
//        }
//
//        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//        imageView.setLayoutParams(new GridView.LayoutParams(imageWidth,
//                imageWidth));
//
//        // get screen dimensions
//
//        ParseFile file = filePaths.get(position);
//        imageView.setParseFile(file);
//        imageView.loadInBackground(new GetDataCallback() {
//            @Override
//            public void done(byte[] data, ParseException e) {
//                // nothing to do
//            }
//        });
////        try {
////            byte[] bitmapdata = file.getData();
////            Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);
////            imageView.setImageBitmap(bitmap);
////        }
////        catch (ParseException e) {
////            Log.e("GridViewImageAdapter", "Parse Exception in getting data from Parsefile");
////        }
//
//        // TODO: figure out how to get pictures as files from Parse from Matt
////        Bitmap image = Utils.decodeFile(filePaths.get(position), imageWidth,
////                imageWidth);
//
//
////        imageView.setImageBitmap(image);
//
//        Log.e("IN ADAPTER", "IMAGE = " + imageView.toString());
//
//        return imageView;
    }

}