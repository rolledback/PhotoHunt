package mrayer.photohunt;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.maps.model.LatLng;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;

public class ViewPhotoActivity extends AppCompatActivity {
    private ImageView imageView;
    private Button viewLocationButton;

    private String url;
    private String type;
    private String photo_id;
    private LatLng location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.view_photos_toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("View Photo");

        imageView = (ImageView) findViewById(R.id.view_photo_imageview);
        viewLocationButton = (Button) findViewById(R.id.view_photo_location_button);

        url = getIntent().getStringExtra("photo url");
        type = getIntent().getStringExtra("type");
        photo_id = getIntent().getStringExtra("photo id");

        viewLocationButton.setClickable(false);
        if (type.equals("Scavenger")) {
            viewLocationButton.setVisibility(View.GONE);
        }
        if (type.equals("Tour")) {
            getLocation();
        }

        downLoadPhoto();
    }

    private void getLocation() {
        ParseQuery<Photo> query = ParseQuery.getQuery("Photo");
        query.whereEqualTo("objectId", photo_id);
        query.getFirstInBackground(new GetCallback<Photo>() {
            public void done(Photo photo, ParseException e) {
                if (photo == null) {
                    Log.d(Constants.ViewPhotoActivityTag, "The getFirst request failed.");
                }
                else if(e != null) {
                    Log.d(Constants.ViewPhotoActivityTag, e.toString());
                }
                else {
                    viewLocationButton.setClickable(true);
                    location = photo.getLocation();
                    viewLocationButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(ViewPhotoActivity.this, ViewPhotoLocationActivity.class);
                            Bundle args = new Bundle();
                            args.putParcelable("location", location);
                            intent.putExtra("bundle", args);
                            startActivity(intent);
                        }
                    });
                }
            }
        });
    }

    private void downLoadPhoto() {
        Picasso.with(this).load(url).into(imageView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
