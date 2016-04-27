package mrayer.photohunt;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class AlbumGalleryActivity extends AppCompatActivity {

    private AlbumListAdapter adapter;
    private ListView list;
    private Handler messageHandler;
    private AlertDialog requestReviewDialog;
    private AlertDialog.Builder dialogBuilder;
    private SharedPreferences currentAlbumPref;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_gallery);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.album_gallery_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList();
            }
        });

        // Set up the adapter to get the data from Parse
        adapter = new AlbumListAdapter(this, swipeRefreshLayout);
        adapter.loadAllAlbums();

        // Get the list view
        list = (ListView) findViewById(R.id.album_list);

        // Default view is all PhotoHuntAlbums
        list.setAdapter(adapter);

        // Set listener
        // Start an Intent for the AlbumDetailsScreen for the selected album
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PhotoHuntAlbum selectedAlbum = adapter.getItem(position);
                ParseProxyObject ppo = new ParseProxyObject(selectedAlbum);
                Intent detailsIntent = new Intent(AlbumGalleryActivity.this, DetailedPhotoHuntActivity.class);
                detailsIntent.putExtra("albumProxy", ppo);
                detailsIntent.putExtra("action", "start");
                startActivity(detailsIntent);
            }
        });

        messageHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == Constants.UPLOAD_COMPLETE) {
                    refreshList();
                }
            }
        };

        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.add_photo_hunt_fab);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(AlbumGalleryActivity.this, CreateNewPhotoHuntActivity.class);
                intent.putExtra("callbackMessenger", new Messenger(messageHandler));
                startActivityForResult(intent, Constants.REQUEST_CREATE_NEW_PHOTO_HUNT);
            }
        });

        dialogBuilder = new AlertDialog.Builder(this);

        currentAlbumPref = this.getSharedPreferences(getString(R.string.current_album_pref) + "-" + ParseUser.getCurrentUser().getObjectId(),
                Context.MODE_PRIVATE);

        /**
         * May the souls of the buttons that were one initialized here rest in peace.
         * May they find true usefulness and be clicked many times in button heaven.
         * In Novak's name we pray.
         *
         * Amen.
         **/
    }

    // Refresh list
    private void refreshList() {
        adapter.loadAllAlbums();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.album_gallery_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /* case R.id.action_refresh_list:
                refreshList();
                return true; */
            /* case R.id.action_create_photo_hunt:
                Intent intent = new Intent(AlbumGalleryActivity.this, CreateNewPhotoHuntActivity.class);
                intent.putExtra("callbackMessenger", new Messenger(messageHandler));
                startActivityForResult(intent, Constants.REQUEST_CREATE_NEW_PHOTO_HUNT);
                return true; */
            case R.id.action_logout:
                Intent logoutIntent = new Intent(AlbumGalleryActivity.this, LoginActivity.class);
                startActivity(logoutIntent);
                ParseUser.logOut();
                ParseUser currentUser = ParseUser.getCurrentUser(); // this will now be null
                return true;
            case R.id.action_my_account:
                Intent accountIntent = new Intent(AlbumGalleryActivity.this, AccountActivity.class);
                accountIntent.putExtra("accountType", "currentUser");
                startActivityForResult(accountIntent, Constants.REQUEST_MANAGEMENT_RESULT);
                return true;
            /* case R.id.action_settings:
                Intent settingsIntent = new Intent(AlbumGalleryActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true; */
            case R.id.action_current_photo_hunt:
                SharedPreferences currentAlbumPref = this.getSharedPreferences(getString(R.string.current_album_pref) + "-" + ParseUser.getCurrentUser().getObjectId(),
                        Context.MODE_PRIVATE);
                Log.d("AlbumGalleryActivity ", currentAlbumPref.getBoolean(getString(R.string.currently_have_active_hunt), false) ? "yes" : "no");
                if (!currentAlbumPref.getBoolean(getString(R.string.currently_have_active_hunt), false)) {
                    Toast.makeText(AlbumGalleryActivity.this, "You do not have a current photo hunt!", Toast.LENGTH_LONG).show();
                    return false;
                }
                else {
                    Intent currentIntent = new Intent(AlbumGalleryActivity.this, CurrentPhotoHuntActivity.class);
                    startActivityForResult(currentIntent, Constants.REQUEST_CURRENT_RESULT);
                }
                return true;

            // uncomment corresponding test items in album_gallery_menu.xml to access these
//            case R.id.action_test_set_add_location:
//                Intent locIntent = new Intent(AlbumGalleryActivity.this, SetChangeLocationActivity.class);
//                startActivity(locIntent);
//                return true;
//            case R.id.action_test_album:
//                Intent albumIntent = new Intent(AlbumGalleryActivity.this, AlbumActivity.class);
//                startActivity(albumIntent);
//                return true;
//            case R.id.action_test_login:
//                Intent loginIntent = new Intent(AlbumGalleryActivity.this, LoginActivity.class);
//                startActivity(loginIntent);
//                return true;
//            case R.id.action_test_sign_up:
//                Intent signUpIntent = new Intent(AlbumGalleryActivity.this, SignUpActivity.class);
//                startActivity(signUpIntent);
//                return true;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CREATE_NEW_PHOTO_HUNT && resultCode == RESULT_OK) {
            refreshList();
        }
        else if (requestCode == Constants.REQUEST_MANAGEMENT_RESULT && resultCode == Constants.DELETE_RESULT) {
            refreshList();
        }
        else if (requestCode == Constants.REQUEST_CURRENT_RESULT) {
            if(resultCode == Constants.ENDED_HUNT) {
                likeToReviewDialog();
                Utils.resetCurrentAlbumPrefs(this, currentAlbumPref);
            }
        }
    }

    private void likeToReviewDialog() {
        final String justEndedId = currentAlbumPref.getString(getString(R.string.album_id), "-1");

        LayoutInflater inflater = this.getLayoutInflater();
        dialogBuilder.setTitle("Rate Photo Hunt");

        View dialogView = inflater.inflate(R.layout.review_dialog, null);
        final RatingBar ratingBar = (RatingBar) dialogView.findViewById(R.id.rating_bar);
        final TextView comments = (TextView) dialogView.findViewById(R.id.comments_box);

        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton("Save Review", null);
        dialogBuilder.setNegativeButton("No Thanks", null);

        requestReviewDialog = dialogBuilder.create();
        requestReviewDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button b = requestReviewDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(ratingBar.getRating() == 0 && comments.getText().length() == 0) {
                            makeAndShowInvalidReviewToast();
                        }
                        else {
                            Review review = new Review();
                            review.setAuthor(ParseUser.getCurrentUser().getUsername());
                            review.setRating(ratingBar.getRating());
                            review.setAlbum(justEndedId);
                            review.setText(comments.getText().toString());
                            uploadReview(review);

                            requestReviewDialog.dismiss();
                            requestReviewDialog = null;
                        }
                    }
                });
            }
        });

        requestReviewDialog.show();
    }

    private void makeAndShowInvalidReviewToast() {
        Toast toast = Toast.makeText(this, "Please leave a comment for a 0 star review.", Toast.LENGTH_SHORT);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if( v != null) v.setGravity(Gravity.CENTER);
        toast.show();
    }

    private void uploadReview(final Review review) {
        review.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null) {
                    // update the average review of the album
                    String albumId = review.getAlbum();
                    try {
                        ParseQuery<PhotoHuntAlbum> albumQuery = Utils.makeGeneralQuery();
                        albumQuery.whereEqualTo("albumId", albumId);
                        PhotoHuntAlbum album = albumQuery.getFirst();

                        ParseQuery<Review> matchingReviewsQuery = new ParseQuery<Review>("Review");
                        matchingReviewsQuery.whereEqualTo("albumId", albumId);
                        List<Review> reviews = matchingReviewsQuery.find();

                        double avg = 0;
                        for(Review r: reviews) {
                            avg += r.getRating();
                        }
                        avg /= reviews.size();

                        album.setAvgReview(avg);
                        album.setNumReviews(reviews.size());
                        album.save();
                    }
                    catch (Exception e2) {
                        Log.d(Constants.AlbumGalleryTag, e2.toString());
                    }
                }
                else {
                    Log.d(Constants.AlbumGalleryTag, e.toString());
                }
            }
        });
    }
}
