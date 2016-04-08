package mrayer.photohunt;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Matthew on 4/7/2016.
 */
public class UserListAdapter extends BaseAdapter {
    private final Context context;
    private List<Pair<String, String>> favoriteUsers;

    public UserListAdapter(Context context) {
        // make sure to call one of the load functions after constructing this object
        this.context = context;
        favoriteUsers = new ArrayList<Pair<String, String>>();
    }

    public void loadFavoriteUsers() {
        // just grab from current user object
        favoriteUsers.clear();
        ParseUser currUser = ParseUser.getCurrentUser();
        ArrayList<String> temp = (ArrayList<String>)currUser.get("favoriteUsers");
        Collections.sort(temp);

        for(String pair : temp) {
            String[] parts = pair.split(",");
            favoriteUsers.add(new Pair(parts[0], parts[1]));
        }

        notifyDataSetChanged();
    }

    public void loadFavoritedBy() {
        String userId = ParseUser.getCurrentUser().getObjectId();
        String userName = ParseUser.getCurrentUser().getUsername();
        Log.d("temp", userName + "," + userId);

        ParseQuery<ParseUser> query = ParseQuery.getQuery("_User");
        query.whereEqualTo("favoriteUsers", userName + "," + userId);
        query.orderByAscending("username");
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    Log.d("temp", Integer.toString(objects.size()));
                    favoriteUsers.clear();
                    for(ParseUser user : objects) {
                        favoriteUsers.add(new Pair<String, String>(user.getUsername(), user.getObjectId()));
                        notifyDataSetChanged();
                    }
                }
                else {
                    Log.d(Constants.UserListAdapterTag, e.toString());
                }
            }
        });
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.user_list_row, parent, false);
            holder = new ViewHolder();
            holder.username = (TextView) view.findViewById(R.id.username);
            view.setTag(holder);
        }
        else {
            holder = (ViewHolder) view.getTag();
        }

        if(favoriteUsers.size() == 0) {
            // can't do anything yet, we don't have any users to display
            return view;
        }

        Pair<String, String> thisUser = getItem(position);
        if(thisUser != null) {
            String username = thisUser.first;
            holder.username.setText(username);
        }

        return view;
    }

    @Override public int getCount() {
        return favoriteUsers.size();
    }

    @Override public Pair<String, String> getItem(int position) {
        return favoriteUsers.get(position);
    }

    @Override public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        TextView username;
    }
}
