package mrayer.photohunt;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matthew on 3/25/2016.
 */
public class AlbumGridAdapter extends BaseAdapter {
    private final Context context;
    private final String albumId;
    private final String type;
    private List<Photo> photos;
    private int width;

    public AlbumGridAdapter(Context context, String albumId, int width, String type) {
        this.context = context;
        this.albumId = albumId;
        this.width = width;
        this.type = type;
        photos = new ArrayList<Photo>();
        loadObjects();
    }

    public void loadObjects() {
        ParseQuery<Photo> query = ParseQuery.getQuery("Photo");
        query.whereEqualTo("albumId", albumId);
        query.orderByAscending("index");
        query.findInBackground(new FindCallback<Photo>() {
            public void done(List<Photo> objects, ParseException e) {
                if (e == null) {
                    photos.clear();
                    photos.addAll(objects);
                    notifyDataSetChanged();
                }
                else {
                    Log.d(Constants.AlbumGridAdapter_Tag, e.toString());
                }
            }
        });
    }

    @Override public View getView(int position, View view, ViewGroup parent) {
        ImageView imageView;
        if (view == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(width, width));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        else {
            imageView = (ImageView) view;
        }

        if(photos.size() == 0) {
            // can't do anything, we haven't finished querying for the photos yet
            return view;
        }

        final Photo currPhoto = getItem(position);
        if(currPhoto != null) {
            String url = currPhoto.getThumbnail().getUrl();

            // Trigger the download of the URL asynchronously into the image view.
            Picasso.with(context).load(url).into(imageView);
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewPhotoActivity.class);
                intent.putExtra("photo url", currPhoto.getPhoto().getUrl());
                intent.putExtra("type", type);
                intent.putExtra("photo id", currPhoto.getObjectId());
                context.startActivity(intent);
            }
        });

        return imageView;
    }

    @Override
    public int getCount() {
        return photos.size();
    }

    @Override
    public Photo getItem(int position) {
        return photos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
