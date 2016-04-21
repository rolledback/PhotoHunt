package mrayer.photohunt;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.squareup.picasso.Callback;
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
                    Log.d(Constants.AlbumGridAdapterTag, e.toString());
                }
            }
        });
    }

    @Override public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view == null) {
            // if it's not recycled, initialize some attributes
            view = LayoutInflater.from(context).inflate(R.layout.album_grid_cell, parent, false);
            holder = new ViewHolder();

            holder.photo = (ImageView) view.findViewById(R.id.grid_photo);
            holder.photo.setVisibility(View.INVISIBLE);
            holder.photo.setScaleType(ImageView.ScaleType.CENTER_CROP);

            holder.spinner = (ProgressBar) view.findViewById(R.id.grid_spinner);
            holder.spinner.setVisibility(View.VISIBLE);
            view.setTag(holder);
        }
        else {
            holder = (ViewHolder) view.getTag();
            holder.photo.setVisibility(View.INVISIBLE);
            holder.spinner.setVisibility(View.VISIBLE);
        }
        view.setLayoutParams(new GridView.LayoutParams(width, width));

        if(photos.size() == 0) {
            // can't do anything, we haven't finished querying for the photos yet
            return view;
        }

        final Photo currPhoto = getItem(position);
        final ViewHolder finalView = holder;
        if(currPhoto != null) {
            String url = currPhoto.getThumbnail().getUrl();

            // Trigger the download of the URL asynchronously into the image view.
            Picasso.with(context).load(url).into(holder.photo, new Callback() {
                @Override
                public void onSuccess() {
                    // it ends up looking better with the if in there, I swear
                    if(Math.random() > -1) {
                        finalView.spinner.setVisibility(View.INVISIBLE);
                        finalView.photo.setVisibility(View.VISIBLE);
                    }
                }
                public void onError() {
                    // ???
                }
            });
        }
        return view;
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

    static class ViewHolder {
        ImageView photo;
        ProgressBar spinner;
    }

}
