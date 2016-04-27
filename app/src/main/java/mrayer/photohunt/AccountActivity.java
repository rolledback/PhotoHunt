package mrayer.photohunt;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.RefreshCallback;

public class AccountActivity extends AppCompatActivity {

    private TextView username;
    private TextView accountCreatedDate;
    private TextView numPhotoHunts;

    private Button favoriteUsers;
    private Button favoritedBy;

    private AlbumListAdapter adapter;
    private ListView list;

    private AlertDialog.Builder dialogBuilder;
    private LayoutInflater inflater;

    private String whichSetup;
    private String otherUserId;
    private String otherUsername;
    private String otherDateCreated;
    private int otherNumAlbums;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        findViewById(R.id.account_main_layout).setVisibility(View.GONE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.account_toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        whichSetup = intent.getStringExtra("accountType");

        if(whichSetup.equals("currentUser")) {
            getSupportActionBar().setTitle("My Account");
        }
        else {
            otherUserId = whichSetup;
            otherUsername = intent.getStringExtra("username");
            getSupportActionBar().setTitle(otherUsername);
        }

        dialogBuilder = new AlertDialog.Builder(this);
        inflater = this.getLayoutInflater();

        username = (TextView) findViewById(R.id.text_username);
        accountCreatedDate = (TextView) findViewById(R.id.text_user_since);
        numPhotoHunts = (TextView) findViewById(R.id.text_num_hunts);

        // Set up the adapter to get the data from Parse
        adapter = new AlbumListAdapter(this, null);

        // Get the list view
        list = (ListView) findViewById(R.id.my_album_list);

        // Default view is all PhotoHuntAlbums
        list.setAdapter(adapter);

        favoritedBy = (Button) findViewById(R.id.favorited_button);
        favoriteUsers = (Button) findViewById(R.id.favorites_button);

        if(whichSetup.equals("currentUser")) {
            loadCurrentUserAccount();
        }
        else {
            getOtherAccount();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.account_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_refresh_account:
                if(whichSetup.equals("currentUser")) {
                    loadCurrentUserAccount();
                }
                else {
                    getOtherAccount();
                }
                return true;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_MANAGEMENT_RESULT && resultCode == Constants.DELETE_RESULT) {
            setupMyAccountComponents();
            setMyAccountTextFields();
            setResult(Constants.DELETE_RESULT);
        }
    }

    private void loadCurrentUserAccount() {
        findViewById(R.id.account_main_layout).setVisibility(View.VISIBLE);
        refreshCurrentUser();
        setupMyAccountComponents();
        setMyAccountTextFields();
    }

    private void refreshCurrentUser() {
        ParseUser.getCurrentUser().fetchInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    setMyAccountTextFields();
                } else {
                    Toast.makeText(getApplicationContext(), "Unable to fetch latest account data.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getOtherAccount() {
        ParseQuery<ParseUser> userQuery = ParseQuery.getQuery("_User");
        userQuery.whereContains("objectId", otherUserId);
        userQuery.getFirstInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser object, ParseException e) {
                if (e == null) {
                    otherDateCreated = object.getCreatedAt().toString();
                    if (object.has("numAlbums")) {
                        otherNumAlbums = (Integer) object.get("numAlbums");
                    } else {
                        otherNumAlbums = 0;
                    }

                    setupOtherAccountComponents();
                    setOtherAccountTextFields();
                    findViewById(R.id.account_main_layout).setVisibility(View.VISIBLE);
                } else {
                    Log.d(Constants.FavoriteUsersActivityTag, e.toString());
                }
            }
        });
    }

    private void setupOtherAccountComponents() {
        adapter.loadAlbumsById(otherUserId);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PhotoHuntAlbum selectedAlbum = adapter.getItem(position);
                ParseProxyObject ppo = new ParseProxyObject(selectedAlbum);
                Intent detailsIntent = new Intent(AccountActivity.this, DetailedPhotoHuntActivity.class);
                detailsIntent.putExtra("albumProxy", ppo);
                detailsIntent.putExtra("action", "start");
                startActivityForResult(detailsIntent, Constants.REQUEST_MANAGEMENT_RESULT);
            }
        });

        favoritedBy.setVisibility(View.GONE);
        favoriteUsers.setVisibility(View.GONE);
    }

    private void setupMyAccountComponents() {
        adapter.loadCurrentUserAlbums();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PhotoHuntAlbum selectedAlbum = adapter.getItem(position);
                ParseProxyObject ppo = new ParseProxyObject(selectedAlbum);
                Intent detailsIntent = new Intent(AccountActivity.this, DetailedPhotoHuntActivity.class);
                detailsIntent.putExtra("albumProxy", ppo);
                detailsIntent.putExtra("action", "delete");
                startActivityForResult(detailsIntent, Constants.REQUEST_MANAGEMENT_RESULT);
            }
        });

        favoritedBy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent favoritedByIntent = new Intent(AccountActivity.this, FavoritedByActivity.class);
                startActivity(favoritedByIntent);
            }
        });

        favoriteUsers = (Button) findViewById(R.id.favorites_button);
        favoriteUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent favoritesIntent = new Intent(AccountActivity.this, FavoriteUsersActivity.class);
                startActivity(favoritesIntent);
            }
        });

        // this only changes if the deletion took place
        setResult(Constants.NO_RESULT);
    }

    private void setMyAccountTextFields() {
        username.setText(ParseUser.getCurrentUser().getUsername());
        if(ParseUser.getCurrentUser().has("numAlbums")) {
            numPhotoHunts.setText("Number of Photo Hunts: " + ParseUser.getCurrentUser().get("numAlbums"));
        }
        else {
            numPhotoHunts.setText("Number of Photo Hunts: " + 0);
        }

        accountCreatedDate.setText("User Since: " + parseOutJoinDate(ParseUser.getCurrentUser().getCreatedAt().toString()));
    }

    private void setOtherAccountTextFields() {
        username.setText(otherUsername);
        numPhotoHunts.setText("Number of Photo Hunts: " + otherNumAlbums);

        accountCreatedDate.setText("User Since: " + parseOutJoinDate(otherDateCreated));
    }

    private String parseOutJoinDate(String fullDate) {
        String[] parts = fullDate.split(" ");

        StringBuilder temp = new StringBuilder();
        temp.append(parts[1] + " ");
        temp.append(parts[2] + " ");
        temp.append(parts[5]);

        return temp.toString();
    }
}
