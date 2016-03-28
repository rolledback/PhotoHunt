package mrayer.photohunt;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.CountCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class AccountActivity extends AppCompatActivity {

    private TextView username;
    private TextView accountCreatedDate;
    private TextView numPhotoHunts;

    private AccountListAdapter adapter;
    private ListView list;

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

        username = (TextView) findViewById(R.id.text_username);
        accountCreatedDate = (TextView) findViewById(R.id.text_user_since);
        numPhotoHunts = (TextView) findViewById(R.id.text_num_hunts);

        setTextFields();

        // Set up the adapter to get the data from Parse
        adapter = new AccountListAdapter(this);

        // Get the list view
        list = (ListView) findViewById(R.id.my_album_list);

        // Default view is all PhotoHuntAlbums
        list.setAdapter(adapter);
    }

    private void setTextFields() {
        username.setText(ParseUser.getCurrentUser().getUsername());
        numPhotoHunts.setText("Number of Photo Hunts: " + ParseUser.getCurrentUser().get("numAlbums"));

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

}
