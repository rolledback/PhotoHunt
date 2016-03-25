package mrayer.photohunt;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.parse.ParseImageView;

public class AlbumImageAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> filePaths = new ArrayList<String>();
    private int imageWidth;
    private LayoutInflater inflater;
    private AlbumImageLoader imageLoader;

    public AlbumImageAdapter(Context c, ArrayList<String> filePaths, int iW) {
        this.context = c;
        this.filePaths = filePaths;
        this.imageWidth = iW;

        inflater = LayoutInflater.from(context);
        imageLoader = new AlbumImageLoader(context);
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
        ParseImageView photo;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.album_gridview_photo, null);
            // Locate the ImageView in gridview_item.xml
            holder.photo = (ParseImageView) convertView.findViewById(R.id.album_photo);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        // Load image into GridView
        imageLoader.DisplayImage(filePaths.get(position), holder.photo);

        return convertView;
    }

    public void clearCache() {
        imageLoader.clearCache();
    }

}