package mrayer.photohunt;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.model.LatLng;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;

public class ViewPhotoActivity extends AppCompatActivity {
    private RelativeLayout relLayout;
    private ImageView imageView;
    private Button viewLocationButton;
    private ProgressBar spinner;

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

        relLayout = (RelativeLayout) findViewById(R.id.view_photo_relative_layout);
        imageView = (ImageView) findViewById(R.id.view_photo_imageview);
        viewLocationButton = (Button) findViewById(R.id.view_photo_location_button);
        viewLocationButton.setVisibility(View.GONE);
        relLayout.setVisibility(View.INVISIBLE);

        spinner = (ProgressBar) findViewById(R.id.view_photo_spinner);

        if(getIntent().hasExtra("url")) {
            url = getIntent().getStringExtra("url");
            type = getIntent().getStringExtra("type");
            photo_id = getIntent().getStringExtra("id");
            downloadPhoto();
        }
        else {
            type = "";
            url = getIntent().getStringExtra("path");
            loadPhoto();
        }
    }

    private void makeRelLayoutVisible() {
        relLayout.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.GONE);
        viewLocationButton.setClickable(false);
        if (type.equals("Tour")) {
            viewLocationButton.setVisibility(View.VISIBLE);
            getLocation();
        }
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
                    viewLocationButton.setClickable(true);
                }
            }
        });
    }

    private void downloadPhoto() {
        Picasso.with(this).load(url).into(imageView, new Callback() {
            @Override
            public void onSuccess() {
                makeRelLayoutVisible();
            }

            @Override
            public void onError() {
                // ???
            }
        });
    }

    private void loadPhoto() {
        new LoadPhotoTask(url, imageView, this).execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private static class LoadPhotoTask extends AsyncTask<Void, Void, Bitmap> {

        private String path;
        private ImageView into;
        private ViewPhotoActivity caller;

        public LoadPhotoTask(String path, ImageView into, ViewPhotoActivity caller) {
            this.path = path;
            this.into = into;
            this.caller = caller;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(bitmap != null) {
                into.setImageBitmap(bitmap);
                caller.makeRelLayoutVisible();
            }
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            File imgFile = new File(path);
            if(imgFile.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                return myBitmap;
            }
            return null;
        }

    }
}
