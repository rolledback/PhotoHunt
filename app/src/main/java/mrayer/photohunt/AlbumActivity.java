package mrayer.photohunt;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.widget.GridView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class AlbumActivity extends AppCompatActivity {
    private ArrayList<String> imagePaths = new ArrayList<String>();
    private GridViewImageAdapter adapter;
    private GridView gridView;
    private int columnWidth;

    private String albumId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        albumId = getIntent().getStringExtra("albumId");

        gridView = (GridView) findViewById(R.id.album_gridview);

        // Initilizing Grid View
        initializeGridView();

        imagePaths = new ArrayList<String>();

        // Gridview adapter
        adapter = new GridViewImageAdapter(AlbumActivity.this, imagePaths,
                columnWidth);

        // setting grid view adapter
        gridView.setAdapter(adapter);

        getPhotos();
    }

    private void initializeGridView() {
        Resources r = getResources();
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                Constants.GRID_PADDING, r.getDisplayMetrics());

        columnWidth = (int) ((Utils.getScreenWidth(getApplicationContext()) - ((Constants.NUM_OF_COLUMNS + 1) * padding)) / Constants.NUM_OF_COLUMNS);

        gridView.setNumColumns(Constants.NUM_OF_COLUMNS);
        gridView.setColumnWidth(columnWidth);
        gridView.setStretchMode(GridView.NO_STRETCH);
        gridView.setPadding((int) padding, (int) padding, (int) padding,
                (int) padding);
        gridView.setHorizontalSpacing((int) padding);
        gridView.setVerticalSpacing((int) padding);
    }

    private void getPhotos() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Photo");
        query.whereEqualTo("albumId", albumId);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> photoList, ParseException e) {
                if (e == null) {
                    for (ParseObject po : photoList) {
                        ParseFile image = po.getParseFile("photo");
                        imagePaths.add(image.getUrl());
                    }
                } else {
                    Log.e(Constants.AlbumGallery_Tag, "Error: " + e.getMessage());
                }
            }
        });
        adapter.notifyDataSetChanged();
    }

}
