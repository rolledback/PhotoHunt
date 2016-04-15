package mrayer.photohunt;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CurrentPhotoHuntActivity extends AppCompatActivity {

    private TextView nameView;
    private TextView authorView;
    private TextView locationView;
    private TextView completedView;
    private TextView typeView;
    private TextView descriptionView;

    private ImageView imageView;

    private Button viewPhotosButton;
    private Button actionButton;

    private String albumId;
    private String type;

    private AlertDialog deleteConfirmation;
    private AlertDialog.Builder dialogBuilder;
    private ProgressDialog dialog;

    SharedPreferences currentAlbumPref;

    // TODO: Do logic for ending a photo hunt - stop geofences, clean sharedPref
    // TODO: What happens if you don't have a current photo hunt?

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_photo_hunt);
        Toolbar toolbar = (Toolbar) findViewById(R.id.current_photo_hunt_toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Current Photo Hunt");

        nameView = (TextView) findViewById(R.id.current_name);
        authorView = (TextView) findViewById(R.id.current_author);
        locationView = (TextView) findViewById(R.id.current_location);
        completedView = (TextView) findViewById(R.id.current_completed);
        descriptionView = (TextView) findViewById(R.id.current_description);
        typeView = (TextView) findViewById(R.id.current_type);

        imageView = (ImageView) findViewById(R.id.current_cover_photo);

        viewPhotosButton = (Button) findViewById(R.id.current_view_photos_button);

        currentAlbumPref = this.getSharedPreferences(getString(R.string.current_album_pref),
                Context.MODE_PRIVATE);

        ParseQuery<PhotoHuntAlbum> albumsByIdQuery = makeGeneralQuery();
        albumsByIdQuery.whereEqualTo("albumId", currentAlbumPref.getString(getString(R.string.album_id), "-1"));
        albumsByIdQuery.findInBackground(new FindCallback<PhotoHuntAlbum>() {
            public void done(List<PhotoHuntAlbum> objects, ParseException e) {
                if (e == null) {
                    // Do set up stuff
                    PhotoHuntAlbum album = objects.get(0);
                    albumId = album.getAlbumId();
                    type = album.getType();

                    viewPhotosButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(CurrentPhotoHuntActivity.this, AlbumActivity.class);
                            intent.putExtra("albumId", albumId);
                            intent.putExtra("type", type);
                            startActivity(intent);
                        }
                    });

                    actionButton = (Button) findViewById(R.id.action_button);
//                    setActionButtonListener(intent.getStringExtra("action"));

                    nameView.setText(album.getName());
                    authorView.setText(album.getAuthor());
                    locationView.setText(album.getLocation());
                    completedView.setText(currentAlbumPref.getInt(getString(R.string.photos_found), -1) + " out of " + album.getNumPhotos());
                    descriptionView.setText(album.getDescription());
                    typeView.setText(type);

                    // download image from url
                    Picasso.with(CurrentPhotoHuntActivity.this).load(album.getCoverPhoto().getUrl()).into(imageView);

                    // this only changes if the deletion took place
                    setResult(Constants.NO_RESULT);

                } else {
                    Log.d(Constants.AlbumListAdapterTag, e.toString());
                }
            }
        });

        dialogBuilder = new AlertDialog.Builder(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
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

}
