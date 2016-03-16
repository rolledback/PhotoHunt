package mrayer.photohunt;

import android.app.ListActivity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

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

        Button button = (Button) findViewById(R.id.create_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AlbumGalleryActivity.this, CreateNewPhotoHuntActivity.class);
                startActivity(intent);
            }
        });
    }

}
