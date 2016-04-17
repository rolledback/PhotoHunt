package mrayer.photohunt;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CurrentPhotoHuntActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

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

    private String TAG = "CurrentPH";

    GoogleApiClient googleAPI;

    SharedPreferences currentAlbumPref;

    PendingIntent geofencePendingIntent;

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
        actionButton = (Button) findViewById(R.id.action_button);

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

        // Stop current photo hunt -- remove geofences
        actionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                stopService(new Intent(CurrentPhotoHuntActivity.this, LocationService.class));

                // TODO: Remove the geofences so the service doesn't start up again

                // Create a new googleAPI client
//                if (googleAPI == null) {
//                    googleAPI = new GoogleApiClient.Builder(CurrentPhotoHuntActivity.this)
//                            .addConnectionCallbacks(CurrentPhotoHuntActivity.this)
//                            .addOnConnectionFailedListener(CurrentPhotoHuntActivity.this)
//                            .addApi(LocationServices.API)
//                            .build();
//                }
//                else
//                {
//                    googleAPI.connect();
//                }
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

    public void onConnected(Bundle bundle) {
        Log.i(TAG, " location services connected");
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, " do not have correct permissions");
        } else {

            // Null out shared preferences
            SharedPreferences.Editor editor = currentAlbumPref.edit();
            editor.putInt(getString(R.string.total_photos), -1);
            editor.putInt(getString(R.string.photos_found), -1);
            editor.putString(getString(R.string.album_id), "" + -1);
            editor.commit();

            // Remove previous current album geofences
            LocationServices.GeofencingApi.removeGeofences(
                    googleAPI,
                    getGeofencePendingIntent()
                    ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if (status.isSuccess()) {
                        Log.i(TAG, " previous geofences removed");
                    }
                }
            });
        }
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, LocationService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences()
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, " location services suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, " location services failed");
    }

}
