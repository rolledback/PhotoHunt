package mrayer.photohunt;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

public class FavoritedByActivity extends AppCompatActivity {

    private UserListAdapter adapter;
    private ListView favoritedByListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorited_by);
        Toolbar toolbar = (Toolbar) findViewById(R.id.favorited_by_toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Favorited By");

        adapter = new UserListAdapter(this, false);
        adapter.loadFavoritedBy();

        favoritedByListView = (ListView) findViewById(R.id.favorited_by_list);
        favoritedByListView.setAdapter(adapter);
        favoritedByListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                launchAccountIntent(position);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void launchAccountIntent(int position) {
        Intent accountIntent = new Intent(FavoritedByActivity.this, AccountActivity.class);
        accountIntent.putExtra("accountType", adapter.getItem(position).second);
        accountIntent.putExtra("username", adapter.getItem(position).first);
        startActivity(accountIntent);
    }

}
