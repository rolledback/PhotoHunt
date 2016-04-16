package mrayer.photohunt;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.ParseUser;

public class AlbumGalleryActivity extends AppCompatActivity {

    private AlbumListAdapter adapter;
    private ListView list;
    private Handler messageHandler;

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
        adapter = new AlbumListAdapter(this);
        adapter.loadAllAlbums();

        // Get the list view
        list = (ListView) findViewById(R.id.album_list);

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
                detailsIntent.putExtra("action", "start");
                startActivity(detailsIntent);
            }
        });

        messageHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == Constants.UPLOAD_COMPLETE) {
                    refreshList();
                }
            }
        };

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
        adapter.loadAllAlbums();
        list.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.album_gallery_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh_list:
                refreshList();
                return true;
            case R.id.action_create_photo_hunt:
                Intent intent = new Intent(AlbumGalleryActivity.this, CreateNewPhotoHuntActivity.class);
                intent.putExtra("callbackMessenger", new Messenger(messageHandler));
                startActivityForResult(intent, Constants.REQUEST_CREATE_NEW_PHOTO_HUNT);
                return true;
            case R.id.action_logout:
                Intent logoutIntent = new Intent(AlbumGalleryActivity.this, LoginActivity.class);
                startActivity(logoutIntent);
                ParseUser.logOut();
                ParseUser currentUser = ParseUser.getCurrentUser(); // this will now be null
                return true;
            case R.id.action_my_account:
                Intent accountIntent = new Intent(AlbumGalleryActivity.this, AccountActivity.class);
                accountIntent.putExtra("accountType", "currentUser");
                startActivityForResult(accountIntent, Constants.REQUEST_MANAGEMENT_RESULT);
                return true;
            case R.id.action_settings:
                Intent settingsIntent = new Intent(AlbumGalleryActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.action_current_photo_hunt:
                Intent currentIntent = new Intent(AlbumGalleryActivity.this, CurrentPhotoHuntActivity.class);
                startActivity(currentIntent);
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

        if (requestCode == Constants.REQUEST_CREATE_NEW_PHOTO_HUNT && resultCode == RESULT_OK) {
            refreshList();
        }
        else if (requestCode == Constants.REQUEST_MANAGEMENT_RESULT && resultCode == Constants.DELETE_RESULT) {
            refreshList();
        }
    }
}
