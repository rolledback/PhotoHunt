package mrayer.photohunt;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Set;

public class FavoriteUsersActivity extends AppCompatActivity {

    private UserListAdapter adapter;
    private ListView favoriteUsersListView;

    private LayoutInflater inflater;
    private AlertDialog.Builder dialogBuilder;

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

        adapter = new UserListAdapter(this, false);
        adapter.loadFavoriteUsers();

        dialogBuilder = new AlertDialog.Builder(this);
        inflater = this.getLayoutInflater();

        favoriteUsersListView = (ListView) findViewById(R.id.favorite_users_list);
        favoriteUsersListView.setAdapter(adapter);
        favoriteUsersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                launchAccountIntent(position);
            }
        });
        favoriteUsersListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                dialogBuilder.setTitle("Photo Options");
                dialogBuilder.setItems(getResources().getStringArray(R.array.user_dialog_options), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0) {
                            launchAccountIntent(position);
                        }
                        else {
                            deleteUser(position);
                        }
                    }
                });
                dialogBuilder.show();
                return true;
            }
        });
    }

    private void launchAccountIntent(int position) {
        Intent accountIntent = new Intent(FavoriteUsersActivity.this, AccountActivity.class);
        accountIntent.putExtra("accountType", adapter.getItem(position).second);
        accountIntent.putExtra("username", adapter.getItem(position).first);
        startActivity(accountIntent);
    }

    private void deleteUser(int position) {
        ParseUser currUser = ParseUser.getCurrentUser();
        ArrayList<String> temp = (ArrayList<String>)currUser.get("favoriteUsers");
        temp.remove(position);
        currUser.put("favoriteUsers", temp);
        currUser.saveInBackground();

        adapter.removeItem(position);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.favorite_users_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_user_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.loadFavoriteUsers();
    }
}
