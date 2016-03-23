package mrayer.photohunt;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.parse.Parse;
import com.parse.ParseObject;

public class AlbumGalleryActivity extends AppCompatActivity {

    private CustomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_gallery);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        // connect to Parse and register our custom classes
        Parse.enableLocalDatastore(this);
        ParseObject.registerSubclass(PhotoHuntAlbum.class);
        Parse.initialize(this, Keys.Parse_APP_ID, Keys.Parse_API_Key);

        // Set up the adapter to get the data from Parse
        adapter = new CustomAdapter(this);

        // Get the list view
        ListView list = (ListView) findViewById(R.id.list);

        // Default view is all PhotoHuntAlbums
        list.setAdapter(adapter);

        // Set listener
        // Start an Intent for the AlbumDetailsScreen for the selected album
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PhotoHuntAlbum selectedAlbum = adapter.getItem(position);
                ParseProxyObject ppo = new ParseProxyObject(selectedAlbum);
                Intent detailsIntent = new Intent(AlbumGalleryActivity.this, DetailedPhotoHuntActivity.class);
                detailsIntent.putExtra("albumProxy", ppo);
                detailsIntent.putExtra("albumId", selectedAlbum.getAlbumId());
                startActivity(detailsIntent);
            }
        });

        /**
         * May the souls of the buttons that were one initialized here rest in peace.
         * May they find true usefulness and be clicked many times in button heaven.
         * In Novak's name we pray.
         *
         * Amen.
         **/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.album_gallery_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_create_photo_hunt:
                Intent intent = new Intent(AlbumGalleryActivity.this, CreateNewPhotoHuntActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_test_set_add_location:
                Intent locIntent = new Intent(AlbumGalleryActivity.this, SetChangeLocationActivity.class);
                startActivity(locIntent);
                return true;

            case R.id.action_test_album:
                Intent albumIntent = new Intent(AlbumGalleryActivity.this, AlbumActivity.class);
                startActivity(albumIntent);
                return true;
        }
        return true;
    }

}
