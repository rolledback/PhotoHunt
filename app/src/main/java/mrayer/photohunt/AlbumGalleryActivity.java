package mrayer.photohunt;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseQueryAdapter;

import java.util.ArrayList;

/* Aila's TODO:
Set onClickListener to open the AlbumDetailsScreen for selected PhotoAlbum
*/

public class AlbumGalleryActivity extends AppCompatActivity {

    private CustomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_gallery);

        // connect to Parse and register our custom classes
        Parse.enableLocalDatastore(this);
        ParseObject.registerSubclass(PhotoHuntAlbum.class);
        Parse.initialize(this, Keys.Parse_APP_ID, Keys.Parse_API_Key);

        // Set up the adapter to get the data from Parse
        adapter = new CustomAdapter(this);

        // Get the list view
        ListView list = (ListView) findViewById(R.id.list);

        // Default view is all PhotoHuntAlbums
        list.setAdapter(adapter);

        // Set listener
        // Need to start an Intent for the AlbumDetailsScreen for the selected album
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "You selected item: " + position, Toast.LENGTH_SHORT).show();
                PhotoHuntAlbum selectedAlbum = adapter.getItem(position);

                ParseProxyObject ppo = new ParseProxyObject(selectedAlbum);
                Intent detailsIntent = new Intent(AlbumGalleryActivity.this, DetailedPhotoHuntActivity.class);
                detailsIntent.putExtra("albumProxy", ppo);
                startActivity(detailsIntent);
            }
        });

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
