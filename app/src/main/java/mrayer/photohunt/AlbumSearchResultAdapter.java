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
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matthew on 3/25/2016.
 */
public class AlbumSearchResultAdapter extends BaseAdapter {
    private final Context context;
    private List<PhotoHuntAlbum> results;
    private boolean searchEmpty;

    public AlbumSearchResultAdapter(Context context) {
        // make sure to call one of the load functions after constructing this object
        this.context = context;
        results = new ArrayList<PhotoHuntAlbum>();
        searchEmpty = false;
    }

    public void doSearch(String queryString) {
        ParseQuery<PhotoHuntAlbum> nameQuery = ParseQuery.getQuery("PhotoHuntAlbum");
        nameQuery.whereContains("searchName", queryString);

        ParseQuery<PhotoHuntAlbum> authorQuery = ParseQuery.getQuery("PhotoHuntAlbum");
        authorQuery.whereContains("searchAuthor", queryString);

        ParseQuery<PhotoHuntAlbum> locationQuery = ParseQuery.getQuery("PhotoHuntAlbum");
        locationQuery.whereContains("searchLocation", queryString);

        ParseQuery<PhotoHuntAlbum> descriptionQuery = ParseQuery.getQuery("PhotoHuntAlbum");
        descriptionQuery.whereContains("searchDescription", queryString);

        List<ParseQuery<PhotoHuntAlbum>> queries = new ArrayList<ParseQuery<PhotoHuntAlbum>>();
        queries.add(nameQuery);
        queries.add(authorQuery);
        queries.add(locationQuery);
        queries.add(descriptionQuery);

        ParseQuery<PhotoHuntAlbum> searchQueryNonPrivate = ParseQuery.or(queries);
        searchQueryNonPrivate.whereEqualTo("isPrivate", false);

        ParseQuery<PhotoHuntAlbum> searchQueryPrivate = ParseQuery.or(queries);
        searchQueryPrivate.whereEqualTo("isPrivate", true);
        searchQueryPrivate.whereEqualTo("whiteList", ParseUser.getCurrentUser().getUsername());

        queries.clear();
        queries.add(searchQueryNonPrivate);
        queries.add(searchQueryPrivate);

        ParseQuery<PhotoHuntAlbum> mainQuery = ParseQuery.or(queries);

        mainQuery.orderByDescending("createdAt");
        mainQuery.findInBackground(new FindCallback<PhotoHuntAlbum>() {
            public void done(List<PhotoHuntAlbum> searchResults, ParseException e) {
                if (searchResults == null) {
                    Log.d(Constants.AlbumSearchResultsAdapterTag, "Search result is null");
                }
                else {
                    Log.d(Constants.AlbumSearchResultsAdapterTag, "Search successful");
                    results = searchResults;
//                    for (PhotoHuntAlbum p : results) {
//                        Log.d("SERACH", "NAME = " + p.getName());
//                    }
//                    if (results.size() == 0) {
//                        Log.d("SEARCH", "RESULT EMPTY");
//                        searchEmpty = true;
//                    }
//                    else {
//                        searchEmpty = false;
//                    }
                    notifyDataSetChanged();

                }
            }
        });
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
//        Log.d("SEARCH", "SEARCH EMPTY = " + searchEmpty);
        ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.album_search_result_list_row, parent, false);
            holder = new ViewHolder();
            holder.photo = (ImageView) view.findViewById(R.id.search_result_photo);
            holder.name = (TextView) view.findViewById(R.id.search_result_name);
            holder.author = (TextView) view.findViewById(R.id.search_result_author);
            holder.location = (TextView) view.findViewById(R.id.search_result_location);
            view.setTag(holder);
        }
        else {
            holder = (ViewHolder) view.getTag();
        }

//        if (searchEmpty) {
//            Toast.makeText(context, "Search result empty", Toast.LENGTH_LONG).show();
//            return view;
//        }

        if(results.size() == 0) {
            // can't do anything, we haven't finished querying for the albums yet
            return view;
        }

        PhotoHuntAlbum currAlbum = getItem(position);
        if(currAlbum != null) {
            String url = currAlbum.getCoverPhotoThumbnail().getUrl();
            holder.name.setText(currAlbum.getName());
            holder.author.setText(currAlbum.getAuthor());
            holder.location.setText(currAlbum.getLocation());

            // Trigger the download of the URL asynchronously into the image view.
            Picasso.with(context).load(url).into(holder.photo);
        }
//        else {
//            Log.e("IN SEARCH", "EMPTY");
//        }

        return view;
    }

    @Override public int getCount() {
        return results.size();
    }

    @Override public PhotoHuntAlbum getItem(int position) {
        return results.get(position);
    }

    @Override public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        ImageView photo;
        TextView name;
        TextView author;
        TextView location;
    }
}