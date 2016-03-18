package mrayer.photohunt;

import android.app.ListActivity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.parse.Parse;
import com.parse.ParseObject;

import java.util.ArrayList;

/* Aila's TODO:
UI includes the ActivityBar and then just a list
Need to populate list from DB
*/

// CJ: Changed this to AppCompatActivity because ListActivity made the app not run
public class AlbumGalleryActivity extends AppCompatActivity {

    private ListView view;
    private ArrayList<String> photoAlbums;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_gallery);

        // connect to Parse and register our custom classes
        Parse.enableLocalDatastore(this);
        ParseObject.registerSubclass(PhotoHuntAlbum.class);
        Parse.initialize(this, Keys.Parse_APP_ID, Keys.Parse_API_Key);

        Button createButton = (Button) findViewById(R.id.create_button);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AlbumGalleryActivity.this, CreateNewPhotoHuntActivity.class);
                startActivity(intent);
            }
        });

        Button locButton = (Button) findViewById(R.id.location_button);
        locButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent locIntent = new Intent(AlbumGalleryActivity.this, SetChangeLocationActivity.class);
                startActivity(locIntent);
           }
        });

        Button detailButton = (Button) findViewById(R.id.detail_button);
        detailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AlbumGalleryActivity.this, DetailedPhotoHuntActivity.class);
                startActivity(intent);
            }
        });
    }
}
