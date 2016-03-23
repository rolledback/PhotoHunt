package mrayer.photohunt;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailedPhotoHuntActivity extends AppCompatActivity {
    private TextView nameView;
    private TextView authorView;
    private TextView locationView;
    private TextView albumSizeView;
    private TextView typeView;

    private ImageView imageView;

    private Button viewPhotosButton;
    private Button startPhotoHuntButton;

    private String albumId;

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

        albumId = getIntent().getStringExtra("albumId");

        nameView = (TextView) findViewById(R.id.detailed_name);
        authorView = (TextView) findViewById(R.id.detailed_author);
        locationView = (TextView) findViewById(R.id.detailed_location);
        albumSizeView = (TextView) findViewById(R.id.detailed_album_size);
        typeView = (TextView) findViewById(R.id.detailed_type);

        imageView = (ImageView) findViewById(R.id.detailed_cover_photo);

        viewPhotosButton = (Button) findViewById(R.id.detailed_view_photos_button);
        viewPhotosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailedPhotoHuntActivity.this, AlbumActivity.class);
                intent.putExtra("albumId", albumId);
                startActivity(intent);
            }
        });
        startPhotoHuntButton = (Button) findViewById(R.id.detailed_start_photo_hunt_button);

        Intent intent = getIntent();
        ParseProxyObject ppo = (ParseProxyObject) intent.getSerializableExtra("albumProxy");

        nameView.setText(ppo.getString("name"));
        authorView.setText(ppo.getString("author"));
        locationView.setText(ppo.getString("location"));
        albumSizeView.setText(Integer.toString(ppo.getInt("numPhotos")));
        typeView.setText(ppo.getString("type"));

        // download image from url
        String coverPhotoUrl = ppo.getString("coverPhoto");
        new Utils.DownloadImageTask(imageView).execute(coverPhotoUrl);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
