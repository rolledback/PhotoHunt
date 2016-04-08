package mrayer.photohunt;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


// Geofence code from http://developer.android.com/training/location/geofencing.html

// Aila's TODO: Need to connect to the Google API Client
// TODO: Figure out if I need to do something with the Request and Intent methods

public class DetailedPhotoHuntActivity extends AppCompatActivity {

    static final int GEOFENCE_RADIUS_IN_METERS = 150;
    static final String TAG = "DetailedPHActivity";
    PendingIntent geofencePendingIntent;

    private TextView nameView;
    private TextView authorView;
    private TextView locationView;
    private TextView albumSizeView;
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
        getSupportActionBar().setTitle("Detailed Photo Hunt");

        dialogBuilder = new AlertDialog.Builder(this);

        Intent intent = getIntent();
        ParseProxyObject ppo = (ParseProxyObject) intent.getSerializableExtra("albumProxy");

        albumId = ppo.getString("albumId");
        type = ppo.getString("type");

        nameView = (TextView) findViewById(R.id.detailed_name);
        authorView = (TextView) findViewById(R.id.detailed_author);
        locationView = (TextView) findViewById(R.id.detailed_location);
        albumSizeView = (TextView) findViewById(R.id.detailed_album_size);
        descriptionView = (TextView) findViewById(R.id.detailed_description);
        typeView = (TextView) findViewById(R.id.detailed_type);

        imageView = (ImageView) findViewById(R.id.detailed_cover_photo);

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

        nameView.setText(ppo.getString("name"));
        authorView.setText(ppo.getString("author"));
        locationView.setText(ppo.getString("location"));
        albumSizeView.setText(Integer.toString(ppo.getInt("numPhotos")));
        descriptionView.setText(ppo.getString("description"));
        typeView.setText(type);

        // download image from url
        String coverPhotoUrl = ppo.getString("coverPhoto");
        Picasso.with(this).load(coverPhotoUrl).into(imageView);

        // this only changes if the deletion took place
        setResult(Constants.NO_RESULT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void setActionButtonListener(String action) {
        if(action.equals("start")){
            actionButton.setText("Start Photo Hunt");
            actionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

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
                            }
                            else {
                                Log.d(TAG, " " + e.toString());
                            }
                        }
                    });

                    List<Geofence> geofences = new ArrayList<Geofence>();

                    // Create a list of geofences
                    for(Photo p: photos) {
                        geofences.add(new Geofence.Builder()
                        // Set the request ID, a string to identify geofence as photo ID
                                .setRequestId("" + p.getIndex())
                                .setCircularRegion(
                                        p.getLocation().latitude,
                                        p.getLocation().longitude,
                                        GEOFENCE_RADIUS_IN_METERS)
                                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                        Geofence.GEOFENCE_TRANSITION_EXIT)
                        .build());
                    }

                    // Not sure what these do...
                    GeofencingRequest req = getGeofencingRequest(geofences);
                    getGeofencePendingIntent();

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
        Intent intent = new Intent(this, LocationService.class);
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

        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);

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
                            dialog.dismiss();
                            finish();
                        }
                    });
                } else {
                    Log.d(Constants.DetailedPhotoHuntActivityTag, e.toString());
                }
            }
        });
    }
}
