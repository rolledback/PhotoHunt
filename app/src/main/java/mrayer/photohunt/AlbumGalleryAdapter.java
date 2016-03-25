package mrayer.photohunt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

/**
 * Created by ailae on 3/18/16.
 */
public class AlbumGalleryAdapter extends ParseQueryAdapter<PhotoHuntAlbum> {

    private MemoryCache memoryCache = new MemoryCache();

    public AlbumGalleryAdapter(Context context) {
        super(context, new ParseQueryAdapter.QueryFactory<PhotoHuntAlbum>() {
            public ParseQuery<PhotoHuntAlbum> create() {
                // Just get everything
                ParseQuery query = new ParseQuery("PhotoHuntAlbum");
                return query;
            }
        });
    }

    @Override
    public View getItemView(PhotoHuntAlbum album, View v, ViewGroup parent) {

        if (v == null) {
            v = View.inflate(getContext(), R.layout.list_layout, null);
        }

        super.getItemView(album, v, parent);

        ParseImageView coverImage = (ParseImageView) v.findViewById(R.id.photo);
        final ParseFile photoFile = album.getParseFile("coverPhoto");
        if (photoFile != null) {
            coverImage.setParseFile(photoFile);

            Bitmap cachedBitmap = memoryCache.get(photoFile.getUrl());
            if(cachedBitmap != null) {
                coverImage.setImageBitmap(cachedBitmap);
                Log.d(Constants.FileCache_Tag, "Used the cache!");
            }
            else {
                coverImage.loadInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] data, ParseException e) {
                        if(data != null) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                            if (bitmap != null) {
                                memoryCache.put(photoFile.getUrl(), bitmap);
                                Log.d(Constants.FileCache_Tag, "Saved to the cache!");
                            }
                        }
                    }
                });
            }
        }

        TextView titleTextView = (TextView) v.findViewById(R.id.name);
        titleTextView.setText(album.getName());
        return v;
    }


}
