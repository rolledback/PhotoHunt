package mrayer.photohunt;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.squareup.picasso.Callback;
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
    private ProgressBar imageSpinner;

    private Button viewPhotosButton;
    private Button stopHuntButton;

    private String albumId;
    private String type;
    private int totalPhotos;

    private AlertDialog stopConfirmation;
    private AlertDialog.Builder dialogBuilder;
    private ProgressDialog dialog;

    private String TAG = "CurrentPH";

    GoogleApiClient googleAPI;

    SharedPreferences currentAlbumPref;

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

        imageSpinner = (ProgressBar) findViewById(R.id.spinner);

        viewPhotosButton = (Button) findViewById(R.id.current_view_photos_button);
        stopHuntButton = (Button) findViewById(R.id.action_button);

        currentAlbumPref = this.getSharedPreferences(getString(R.string.current_album_pref),
                Context.MODE_PRIVATE);

        // changed if user ends the photo hunt
        setResult(Constants.CONTINUE_HUNT);

        ParseQuery<PhotoHuntAlbum> albumsByIdQuery = Utils.makeGeneralQuery();
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

                    totalPhotos = album.getNumPhotos();

                    // download image from url
                    Picasso.with(CurrentPhotoHuntActivity.this).load(album.getCoverPhoto().getUrl()).into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            imageSpinner.setVisibility(View.GONE);
                            imageView.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError() {
                            // ???
                        }
                    });

                    // this only changes if the deletion took place
                    setResult(Constants.NO_RESULT);

                } else {
                    Log.d(Constants.AlbumListAdapterTag, e.toString());
                }
            }
        });

        stopHuntButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmStopHunt();
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

    public void onConnected(Bundle bundle) {
        Log.i(TAG, " location services connected");
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, " do not have correct permissions");
        }
        else {

            List<String> geofenceIDs = new ArrayList<String>();

            // Get the geofence IDs
            for(int i = 1; i < totalPhotos + 1; i++)
            {
                geofenceIDs.add(currentAlbumPref.getString("photo" + i, ""));
            }

            // Remove all the geofences
            LocationServices.GeofencingApi.removeGeofences(googleAPI, geofenceIDs).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if (status.isSuccess()) {
                        Log.i(TAG, " previous geofences removed");
                    }
                }
            });

            // Null out shared preferences
            SharedPreferences.Editor editor = currentAlbumPref.edit();
            editor.putInt(getString(R.string.total_photos), -1);
            editor.putInt(getString(R.string.photos_found), -1);
            editor.putString(getString(R.string.album_id), "" + -1);
            editor.commit();

            finish();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, " location services suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, " location services failed");
    }

    private void confirmStopHunt() {
        dialogBuilder.setTitle("Warning");
        dialogBuilder.setMessage("Are you sure you want to end your current photo hunt?");
        dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                stopConfirmation.dismiss();
                stopConfirmation = null;
                setResult(Constants.ENDED_HUNT);
                stopPhotoHunt();
            }

        });
        dialogBuilder.setNegativeButton("No", null);
        stopConfirmation = dialogBuilder.show();
    }

    // Stop current photo hunt -- finish ends up being called upon successful connection
    private void stopPhotoHunt() {
        // Stop location monitoring service
        stopService(new Intent(CurrentPhotoHuntActivity.this, LocationMonitoringService.class));

        // Create a new googleAPI client
        if (googleAPI == null) {
            googleAPI = new GoogleApiClient.Builder(CurrentPhotoHuntActivity.this)
                    .addConnectionCallbacks(CurrentPhotoHuntActivity.this)
                    .addOnConnectionFailedListener(CurrentPhotoHuntActivity.this)
                    .addApi(LocationServices.API)
                    .build();

            googleAPI.connect();
        } else {
            googleAPI.connect();
        }
    }
}
