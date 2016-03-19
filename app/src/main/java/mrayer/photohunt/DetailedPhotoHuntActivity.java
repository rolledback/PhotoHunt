package mrayer.photohunt;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class DetailedPhotoHuntActivity extends AppCompatActivity {
    private TextView nameView;
    private TextView authorView;
    private TextView locationView;
    private TextView albumSizeView;
    private TextView typeView;

    private ImageView imageView;

    private Button viewPhotosButton;
    private Button startPhotoHuntButton;

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

        imageView = (ImageView) findViewById(R.id.detailed_cover_photo);

        viewPhotosButton = (Button) findViewById(R.id.detailed_view_photos_button);
        startPhotoHuntButton = (Button) findViewById(R.id.detailed_start_photo_hunt_button);

        Intent intent = getIntent();
        ParseProxyObject ppo = (ParseProxyObject) intent.getSerializableExtra("albumProxy");

        nameView.setText(ppo.getString("name"));
        authorView.setText(ppo.getString("author"));
        locationView.setText(ppo.getString("location"));
        albumSizeView.setText(Integer.toString(ppo.getInt("albumSize")));
        typeView.setText(ppo.getString("type"));

        // download image from url
        String coverPhotoUrl = ppo.getString("coverPhoto");
        new DownloadImageTask().execute(coverPhotoUrl);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        protected Bitmap doInBackground(String... params) {
            Bitmap returnImage = null;
            URL url = null;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                returnImage = BitmapFactory.decodeStream(in); //note, this is not a return statement…the variable
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            }
            catch (NullPointerException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                urlConnection.disconnect();
                return returnImage;
            }
        }
        protected void onPostExecute(Bitmap result) {
            if(result != null) {
                imageView.setImageBitmap(result);
            }
        }
    }

}
