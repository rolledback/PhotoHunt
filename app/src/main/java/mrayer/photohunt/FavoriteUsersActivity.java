package mrayer.photohunt;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;

public class FavoriteUsersActivity extends AppCompatActivity {

    private UserListAdapter adapter;
    private ListView favoriteUsersListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_users);
        Toolbar toolbar = (Toolbar) findViewById(R.id.favorite_users_toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Favorite Users");

        adapter = new UserListAdapter(this);
        adapter.loadFavoriteUsers();

        favoriteUsersListView = (ListView) findViewById(R.id.favorite_users_list);
        favoriteUsersListView.setAdapter(adapter);
    }

}
