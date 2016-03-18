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
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.w3c.dom.Text;

import java.util.List;

public class DetailedPhotoHuntActivity extends AppCompatActivity {
    private TextView nameView;
    private TextView authorView;
    private TextView locationView;
    private TextView albumSizeView;
    private TextView typeView;

    private Button viewPhotosButton;
    private Button startPhotoHuntButton;

    private String currentAlbumId;

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

        nameView = (TextView) findViewById(R.id.detailed_name);
        authorView = (TextView) findViewById(R.id.detailed_author);
        locationView = (TextView) findViewById(R.id.detailed_location);
        albumSizeView = (TextView) findViewById(R.id.detailed_album_size);
        typeView = (TextView) findViewById(R.id.detailed_type);

        viewPhotosButton = (Button) findViewById(R.id.detailed_view_photos_button);
        startPhotoHuntButton = (Button) findViewById(R.id.detailed_start_photo_hunt_button);

        Intent intent = getIntent();
        // CAUTION, THIS INTENT KEY MAY CHANGE DEPENDING ON THE INTENT PASSED IN FROM ALBUM GALLERY
        currentAlbumId = intent.getStringExtra("albumId");

        // TODO: using the currentAlbumId, get the photo hunt album information to fill in everything else
        // Dummy photo hunt for now
        PhotoHuntAlbum currentPhotoHunt = new PhotoHuntAlbum();

        nameView.setText(currentPhotoHunt.getName());
        authorView.setText(currentPhotoHunt.getAuthor());
        locationView.setText(currentPhotoHunt.getLocation());
        String albumSize = "" + currentPhotoHunt.getNumPhotos();
        albumSizeView.setText(albumSize);
        typeView.setText(currentPhotoHunt.getType());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}
