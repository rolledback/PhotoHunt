package mrayer.photohunt;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

public class AlbumSearchActivity extends AppCompatActivity {

    private AlbumSearchResultAdapter adapter;
    private ListView listView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.album_search_toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Search Results");

        adapter = new AlbumSearchResultAdapter(this);
        listView = (ListView) findViewById(R.id.album_search_result_list);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PhotoHuntAlbum selectedAlbum = adapter.getItem(position);
                ParseProxyObject ppo = new ParseProxyObject(selectedAlbum);
                Intent detailsIntent = new Intent(AlbumSearchActivity.this, DetailedPhotoHuntActivity.class);
                detailsIntent.putExtra("albumProxy", ppo);
                detailsIntent.putExtra("albumId", selectedAlbum.getAlbumId());
                detailsIntent.putExtra("type", selectedAlbum.getType());
                detailsIntent.putExtra("action", "start");
                startActivity(detailsIntent);
            }
        });

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            adapter.doSearch(query);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
