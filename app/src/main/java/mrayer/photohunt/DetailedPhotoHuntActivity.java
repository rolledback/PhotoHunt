package mrayer.photohunt;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


// Geofence code from http://developer.android.com/training/location/geofencing.html

public class DetailedPhotoHuntActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    static final int GEOFENCE_RADIUS_IN_METERS = 150;
    private PendingIntent geofencePendingIntent;

    private TextView authorView;
    private TextView locationView;
    private TextView albumSizeView;
    private TextView typeView;
    private TextView descriptionView;

    private LinearLayout ratingsLayout;
    private TextView ratingBarLabel;
    private RatingBar ratingBar;

    private ImageView imageView;
    private ProgressBar imageSpinner;

    private Button viewPhotosButton;
    private Button actionButton;

    private String albumId;
    private String type;

    private AlertDialog deleteConfirmation;
    private AlertDialog errorDialog;
    private AlertDialog confirmNewHuntDialog;
    private AlertDialog.Builder dialogBuilder;
    private ProgressDialog dialog;

    private SharedPreferences currentAlbumPref;

    private GoogleApiClient googleAPI;
    private List<Geofence> geofences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_photo_hunt);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detailed_photo_hunt_toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        dialogBuilder = new AlertDialog.Builder(this);

        Intent intent = getIntent();
        ParseProxyObject ppo = (ParseProxyObject) intent.getSerializableExtra("albumProxy");

        albumId = ppo.getString("albumId");
        type = ppo.getString("type");

        authorView = (TextView) findViewById(R.id.detailed_author);
        locationView = (TextView) findViewById(R.id.detailed_location);
        albumSizeView = (TextView) findViewById(R.id.detailed_album_size);
        descriptionView = (TextView) findViewById(R.id.detailed_description);
        typeView = (TextView) findViewById(R.id.detailed_type);

        ratingsLayout = (LinearLayout) findViewById(R.id.ratings_layout);
        ratingBarLabel = (TextView) findViewById(R.id.album_rating_bar_label);
        ratingBar = (RatingBar) findViewById(R.id.album_rating_bar);

        imageView = (ImageView) findViewById(R.id.detailed_cover_photo);
        imageView.setVisibility(View.INVISIBLE);

        imageSpinner = (ProgressBar) findViewById(R.id.spinner);

        viewPhotosButton = (Button) findViewById(R.id.detailed_view_photos_button);
        viewPhotosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailedPhotoHuntActivity.this, AlbumActivity.class);
                intent.putExtra("albumId", albumId);
                intent.putExtra("type", type);
                startActivity(intent);
            }
        });

        actionButton = (Button) findViewById(R.id.action_button);
        setActionButtonListener(intent.getStringExtra("action"));

        getSupportActionBar().setTitle(ppo.getString("name"));
        authorView.setText(ppo.getString("author"));
        locationView.setText(ppo.getString("location"));
        albumSizeView.setText(Integer.toString(ppo.getInt("numPhotos")));
        descriptionView.setText(ppo.getString("description"));
        typeView.setText(type);

        try {
            // If the avg review was a whole number, Parse saves it as an int, so sometimes
            // this call will fail.
            ratingBar.setRating((float) (double) ppo.getDouble("avgReview"));
        }
        catch(ClassCastException e) {
            Log.d(Constants.DetailedPhotoHuntActivityTag, "Parse stored avg reviews as int.");
            ratingBar.setRating((float) (double) (int) ppo.getInt("avgReview"));
        }

        final int numReviews = ppo.getInt("numReviews");
        if(numReviews == 1) {
            ratingBarLabel.setText("(" + numReviews + " review)");
        }
        else {
            ratingBarLabel.setText("(" + numReviews + " reviews)");
        }
        ratingsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(numReviews > 0) {
                    launchReviewsDialog();
                }
                else {
                    makeAndShowNoReviewsToast();
                }
            }
        });

        // download image from url
        String coverPhotoUrl = ppo.getString("coverPhoto");
        Picasso.with(this).load(coverPhotoUrl).into(imageView, new Callback() {
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

        // Create a new googleAPI client
        if (googleAPI == null) {
            googleAPI = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // Create the shared preferences file
        currentAlbumPref = this.getSharedPreferences(getString(R.string.current_album_pref) + "-" + ParseUser.getCurrentUser().getObjectId(),
                Context.MODE_PRIVATE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void launchReviewsDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("User Reviews");
        dialog.setCancelable(true);

        View view = getLayoutInflater().inflate(R.layout.review_list_dialog, null);

        ListView list = (ListView) view.findViewById(R.id.review_list_dialog_list);
        ReviewDialogListAdapter adapter = new ReviewDialogListAdapter(this, albumId);
        adapter.loadReviews();
        list.setAdapter(adapter);

        dialog.setView(view);
        dialog.show();
    }

    private void makeAndShowStartingToast() {
        Toast toast = Toast.makeText(this, "Starting the photo hunt.", Toast.LENGTH_SHORT);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if( v != null) v.setGravity(Gravity.CENTER);
        toast.show();
    }

    private void makeAndShowNoReviewsToast() {
        Toast toast = Toast.makeText(this, "There are currently no reviews for this photo hunt.", Toast.LENGTH_SHORT);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if( v != null) v.setGravity(Gravity.CENTER);
        toast.show();
    }

    private void setActionButtonListener(String action) {
        if(action.equals("start")){
            actionButton.setText("Start Photo Hunt");
            actionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startPhotoHunt();
                }
            });
        }
        else if(action.equals("delete")){
            actionButton.setText("Delete Photo Hunt");
            actionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmDelete();
                }
            });
        }
    }

    private void confirmNewHuntWhenHunting() {
        dialogBuilder.setTitle("Warning");
        dialogBuilder.setMessage("You already have a current photo hunt. Are you sure you want to start a new one?");
        dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                confirmNewHuntDialog.dismiss();
                confirmNewHuntDialog = null;
                queryAndSetupGeofences();
            }

        });
        dialogBuilder.setNegativeButton("No", null);
        confirmNewHuntDialog = dialogBuilder.show();
    }

    private void startPhotoHunt() {
        final SharedPreferences.Editor editor = currentAlbumPref.edit();

        boolean currentlyOnHunt = currentAlbumPref.getBoolean(getString(R.string.currently_have_active_hunt), false);
        if(currentlyOnHunt) {
            confirmNewHuntWhenHunting();
        }
        else {
            queryAndSetupGeofences();
        }
    }

    private void queryAndSetupGeofences() {
        final SharedPreferences.Editor editor = currentAlbumPref.edit();

        // immediately set user as being on a hunt
        editor.putBoolean(getString(R.string.currently_have_active_hunt), true);
        editor.commit();

        final List<Photo> photos = new ArrayList<Photo>();

        // Get all the photos for that album
        ParseQuery<Photo> query = ParseQuery.getQuery("Photo");
        query.whereEqualTo("albumId", albumId);
        query.orderByAscending("index");
        query.findInBackground(new FindCallback<Photo>() {
            public void done(List<Photo> objects, ParseException e) {
                if (e == null) {
                    photos.clear();
                    photos.addAll(objects);

                    geofences = new ArrayList<Geofence>();

                    // Add the total number of photos to the shared pref
                    // Set total number of photos found to 0
                    int totalPhotos = photos.size();
                    editor.putInt(getString(R.string.total_photos), totalPhotos);
                    editor.putInt(getString(R.string.photos_found), 0);
                    editor.putString(getString(R.string.album_id), albumId);
                    int count = 1;

                    // Create a list of geofences
                    for (Photo p : photos) {
                        // Add in the geofence requestID so it can later be removed in CurrentPhotoHunt
                        editor.putString("photo" + count, p.getLocation().latitude + "," + p.getLocation().longitude);
                        count++;
                        geofences.add(new Geofence.Builder()
                                // Set the request ID, a string to identify geofence as photo ID
                                .setRequestId(p.getLocation().latitude + "," + p.getLocation().longitude)
                                .setCircularRegion(
                                        p.getLocation().latitude,
                                        p.getLocation().longitude,
                                        GEOFENCE_RADIUS_IN_METERS)
                                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                        Geofence.GEOFENCE_TRANSITION_EXIT)
                                .build());
                    }

                    editor.commit();

                    // Actually connect to the Google API
                    googleAPI.connect();
                    makeAndShowStartingToast();

                } else {
                    Log.d(Constants.DetailedPhotoHuntActivityTag, " " + e.toString());
                }
            }
        });
    }

    private GeofencingRequest getGeofencingRequest(List<Geofence> geofences) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofences);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences()
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    private void confirmDelete() {
        dialogBuilder.setTitle("Warning");
        dialogBuilder.setMessage("This action cannot be undone. Are you sure you want to delete your photo hunt?");
        dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteConfirmation.dismiss();
                deleteConfirmation = null;
                deleteAlbum();
            }

        });
        dialogBuilder.setNegativeButton("No", null);
        deleteConfirmation = dialogBuilder.show();
    }

    private void deleteError(String msg) {
        dialogBuilder.setTitle("Deletion Error");
        dialogBuilder.setMessage(msg);
        dialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                errorDialog.dismiss();
                errorDialog = null;
                finish();
            }

        });
        errorDialog = dialogBuilder.show();
    }

    private void deleteAlbum() {
        // delete all associated photos
        ParseQuery<Photo> photoQuery = ParseQuery.getQuery("Photo");
        photoQuery.whereEqualTo("albumId", albumId);
        photoQuery.orderByAscending("index");
        photoQuery.findInBackground(new FindCallback<Photo>() {
            public void done(List<Photo> objects, ParseException e) {
                if (e == null) {
                    for (Photo photo : objects) {
                        photo.deleteInBackground();
                    }
                } else {
                    Log.d(Constants.DetailedPhotoHuntActivityTag, e.toString());
                }
            }
        });

        // delete the album
        ParseQuery<PhotoHuntAlbum> albumQuery = ParseQuery.getQuery("PhotoHuntAlbum");
        albumQuery.whereEqualTo("albumId", albumId);
        albumQuery.orderByAscending("index");
        albumQuery.getFirstInBackground(new GetCallback<PhotoHuntAlbum>() {
            public void done(PhotoHuntAlbum object, ParseException e) {
                dialog.show();
                if (e == null) {
                    object.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            setResult(Constants.DELETE_RESULT);
                            finish();
                        }
                    });
                } else {
                    Log.d(Constants.DetailedPhotoHuntActivityTag, e.toString());
                    deleteError(e.toString());
                }
            }
        });

        int numHunts = ParseUser.getCurrentUser().getInt("numAlbums");
        ParseUser.getCurrentUser().put("numAlbums", numHunts - 1);
        ParseUser.getCurrentUser().saveInBackground();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(Constants.DetailedPhotoHuntActivityTag, " location services connected");
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Log.d(Constants.DetailedPhotoHuntActivityTag, " do not have correct permissions");
        }
        else
        {
            // Remove previous current album geofences
            LocationServices.GeofencingApi.removeGeofences(
                    googleAPI,
                    getGeofencePendingIntent()
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if (status.isSuccess()) {
                        Log.i(Constants.DetailedPhotoHuntActivityTag, " previous geofences removed");
                    }
                }
            });

            // Send the geofences to the GoogleAPI
            LocationServices.GeofencingApi.addGeofences(
                    googleAPI,
                    getGeofencingRequest(geofences),
                    getGeofencePendingIntent()
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if (status.isSuccess()) {
                        // Success
                        Log.i(Constants.DetailedPhotoHuntActivityTag, " geofence request success");

                        // Geofences were set, can now start monitoring your location
                        // By now, sharedPref has the list of photoLocations
                        Intent locMonitoring = new Intent(getApplicationContext(), LocationMonitoringService.class);
                        stopService(locMonitoring);
                        startService(locMonitoring);
                    }
                }
            });
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(Constants.DetailedPhotoHuntActivityTag, " location services suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(Constants.DetailedPhotoHuntActivityTag, " location services failed");
    }
}
