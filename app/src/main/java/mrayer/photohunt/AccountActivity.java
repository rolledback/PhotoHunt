package mrayer.photohunt;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseUser;

public class AccountActivity extends AppCompatActivity {

    private TextView username;
    private TextView accountCreatedDate;
    private TextView numPhotoHunts;

    private AlbumListAdapter adapter;
    private ListView list;

    private AlertDialog.Builder dialogBuilder;
    private LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        Toolbar toolbar = (Toolbar) findViewById(R.id.account_toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Account");

        dialogBuilder = new AlertDialog.Builder(this);
        inflater = this.getLayoutInflater();

        username = (TextView) findViewById(R.id.text_username);
        accountCreatedDate = (TextView) findViewById(R.id.text_user_since);
        numPhotoHunts = (TextView) findViewById(R.id.text_num_hunts);

        setTextFields();

        // Set up the adapter to get the data from Parse
        adapter = new AlbumListAdapter(this);
        adapter.loadCurrentUserAlbums();

        // Get the list view
        list = (ListView) findViewById(R.id.my_album_list);

        // Default view is all PhotoHuntAlbums
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                View dialogView = inflater.inflate(R.layout.album_management_dialog, null);
                dialogBuilder.setView(dialogView);

                ListView managementList =  (ListView) dialogView.findViewById(R.id.management_options_list);
                managementList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Toast.makeText(getApplicationContext(), "Action not currently implemented.", Toast.LENGTH_SHORT).show();
                    }
                });

                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();
            }
        });
    }

    private void setTextFields() {
        username.setText(ParseUser.getCurrentUser().getUsername());
        if(ParseUser.getCurrentUser().has("numAlbums")) {
            numPhotoHunts.setText("Number of Photo Hunts: " + ParseUser.getCurrentUser().get("numAlbums"));
        }
        else {
            numPhotoHunts.setText("Number of Photo Hunts: " + 0);
        }

        // need to work on parsing this into human readable date
        accountCreatedDate.setText("User Since: " + parseOutJoinDate(ParseUser.getCurrentUser().getCreatedAt().toString()));
    }

    private String parseOutJoinDate(String fullDate) {
        String[] parts = fullDate.split(" ");

        StringBuilder temp = new StringBuilder();
        temp.append(parts[1] + " ");
        temp.append(parts[2] + " ");
        temp.append(parts[5]);

        return temp.toString();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}