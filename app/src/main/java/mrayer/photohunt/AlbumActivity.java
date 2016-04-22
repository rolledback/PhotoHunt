package mrayer.photohunt;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

public class AlbumActivity extends AppCompatActivity {
    private AlbumGridAdapter adapter;
    private GridView gridView;
    private int columnWidth;

    private String albumId;

    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("View Photos");

        albumId = getIntent().getStringExtra("albumId");
        type = getIntent().getStringExtra("type");
        gridView = (GridView) findViewById(R.id.album_gridview);

        // Initilizing Grid View
        initializeGridView();

        // Gridview adapter
        adapter = new AlbumGridAdapter(AlbumActivity.this, albumId, columnWidth, type);

        // setting grid view adapter
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(AlbumActivity.this, ViewPhotoActivity.class);
                intent.putExtra("url", adapter.getItem(position).getPhoto().getUrl());
                intent.putExtra("type", type);
                intent.putExtra("id", adapter.getItem(position).getObjectId());
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void initializeGridView() {
        Resources r = getResources();
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Constants.GRID_PADDING, r.getDisplayMetrics());

        columnWidth = (int) ((Utils.getScreenWidth(getApplicationContext()) - ((Constants.NUM_OF_COLUMNS + 1) * padding)) / Constants.NUM_OF_COLUMNS);

        gridView.setNumColumns(Constants.NUM_OF_COLUMNS);
        gridView.setColumnWidth(columnWidth);
        gridView.setStretchMode(GridView.NO_STRETCH);
        gridView.setPadding((int) padding, (int) padding, (int) padding, (int) padding);
        gridView.setHorizontalSpacing((int) padding);
        gridView.setVerticalSpacing((int) padding);
    }

}
