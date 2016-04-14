package mrayer.photohunt;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matthew on 3/25/2016.
 */
public class AlbumListAdapter extends BaseAdapter {
    private final Context context;
    private List<PhotoHuntAlbum> albums;

    public AlbumListAdapter(Context context) {
        // make sure to call one of the load functions after constructing this object
        this.context = context;
        albums = new ArrayList<PhotoHuntAlbum>();
    }

    private ParseQuery<PhotoHuntAlbum> makeGeneralQuery() {
        ParseQuery<PhotoHuntAlbum> queryNonPrivate = ParseQuery.getQuery("PhotoHuntAlbum");
        queryNonPrivate.whereEqualTo("isPrivate", false);

        ParseQuery<PhotoHuntAlbum> queryPrivate = ParseQuery.getQuery("PhotoHuntAlbum");
        queryPrivate.whereEqualTo("isPrivate", true);
        queryPrivate.whereEqualTo("whiteList", ParseUser.getCurrentUser().getUsername());

        List<ParseQuery<PhotoHuntAlbum>> queries = new ArrayList<ParseQuery<PhotoHuntAlbum>>();
        queries.add(queryNonPrivate);
        queries.add(queryPrivate);

        ParseQuery<PhotoHuntAlbum> combinedQuery = ParseQuery.or(queries);
        combinedQuery.orderByDescending("createdAt");

        return combinedQuery;
    }

    public void loadAllAlbums() {
        ParseQuery<PhotoHuntAlbum> allAlbumsQuery = makeGeneralQuery();
        allAlbumsQuery.findInBackground(new FindCallback<PhotoHuntAlbum>() {
            public void done(List<PhotoHuntAlbum> objects, ParseException e) {
                if (e == null) {
                    albums.clear();
                    albums.addAll(objects);
                    notifyDataSetChanged();
                }
            }
        });
    }

    public void loadAlbumsById(String userId) {
        ParseQuery<PhotoHuntAlbum> albumsByIdQuery = makeGeneralQuery();
        albumsByIdQuery.whereEqualTo("authorId", userId);
        albumsByIdQuery.findInBackground(new FindCallback<PhotoHuntAlbum>() {
            public void done(List<PhotoHuntAlbum> objects, ParseException e) {
                if (e == null) {
                    albums.clear();
                    albums.addAll(objects);
                    notifyDataSetChanged();
                }
                else {
                    Log.d(Constants.AlbumListAdapterTag, e.toString());
                }
            }
        });
    }

    public void loadCurrentUserAlbums() {
        loadAlbumsById(ParseUser.getCurrentUser().getObjectId());
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.album_list_row, parent, false);
            holder = new ViewHolder();
            holder.photo = (ImageView) view.findViewById(R.id.photo);
            holder.name = (TextView) view.findViewById(R.id.name);
            view.setTag(holder);
        }
        else {
            holder = (ViewHolder) view.getTag();
        }

        if(albums.size() == 0) {
            // can't do anything, we haven't finished querying for the albums yet
            return view;
        }

        PhotoHuntAlbum currAlbum = getItem(position);
        if(currAlbum != null) {
            String url = currAlbum.getCoverPhotoThumbnail().getUrl();
            holder.name.setText(currAlbum.getName());

            // Trigger the download of the URL asynchronously into the image view.
            Picasso.with(context).load(url).into(holder.photo);
        }

        return view;
    }

    @Override public int getCount() {
        return albums.size();
    }

    @Override public PhotoHuntAlbum getItem(int position) {
        return albums.get(position);
    }

    @Override public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        ImageView photo;
        TextView name;
    }
}