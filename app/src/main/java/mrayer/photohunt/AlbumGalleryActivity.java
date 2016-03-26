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

import com.parse.ParseObject;
import com.parse.ParseUser;

public class AlbumGalleryActivity extends AppCompatActivity {

    private AlbumGalleryListAdapter adapter;
    private ListView list;

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

        // Set up the adapter to get the data from Parse
        adapter = new AlbumGalleryListAdapter(this);

        // Get the list view
        list = (ListView) findViewById(R.id.list);

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

    // Refresh list
    private void refreshList() {
        adapter.loadObjects();
        list.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.album_gallery_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh_list:
                refreshList();
                return true;
            case R.id.action_create_photo_hunt:
                Intent intent = new Intent(AlbumGalleryActivity.this, CreateNewPhotoHuntActivity.class);
                startActivityForResult(intent, Constants.REQUEST_CREATE_NEW_PHOTO_HUNT);
                return true;
            case R.id.action_logout:
                Intent logoutIntent = new Intent(AlbumGalleryActivity.this, LoginActivity.class);
                startActivity(logoutIntent);
                ParseUser.logOut();
                ParseUser currentUser = ParseUser.getCurrentUser(); // this will now be null
                return true;

            // uncomment corresponding test items in album_gallery_menu.xml to access these
//            case R.id.action_test_set_add_location:
//                Intent locIntent = new Intent(AlbumGalleryActivity.this, SetChangeLocationActivity.class);
//                startActivity(locIntent);
//                return true;
//            case R.id.action_test_album:
//                Intent albumIntent = new Intent(AlbumGalleryActivity.this, AlbumActivity.class);
//                startActivity(albumIntent);
//                return true;
//            case R.id.action_test_login:
//                Intent loginIntent = new Intent(AlbumGalleryActivity.this, LoginActivity.class);
//                startActivity(loginIntent);
//                return true;
//            case R.id.action_test_sign_up:
//                Intent signUpIntent = new Intent(AlbumGalleryActivity.this, SignUpActivity.class);
//                startActivity(signUpIntent);
//                return true;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CREATE_NEW_PHOTO_HUNT && resultCode == RESULT_OK ) {
            refreshList();
        }
    }

}
