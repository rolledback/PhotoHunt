package mrayer.photohunt;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Matthew on 4/7/2016.
 */
public class UserListAdapter extends BaseAdapter {
    private final Context context;
    private List<Pair<String, String>> userList;
    private Set<Pair<String, String>> checked;
    boolean useCheck;

    public UserListAdapter(Context context, boolean useCheck) {
        // make sure to call one of the load functions after constructing this object
        this.context = context;
        this.useCheck = useCheck;
        userList = new ArrayList<Pair<String, String>>();
        checked = new HashSet<Pair<String, String>>();
    }

    public void setCheckedState(int position, boolean isChecked) {
        if(isChecked) {
            checked.add(userList.get(position));
        }
        else {
            checked.remove(userList.get(position));
        }
    }

    public Set<Pair<String, String>> getChecked() {
        return checked;
    }

    public void doSearch(String queryString) {
        final List<String> alreadyFavorited = new ArrayList<String>();
        for(String entry : (ArrayList<String>)ParseUser.getCurrentUser().get("favoriteUsers")) {
            String[] parts = entry.split(",");
            alreadyFavorited.add(parts[0]);
        }

        ParseQuery<ParseUser> userQuery = ParseQuery.getQuery("_User");
        userQuery.whereContains("username", queryString);
        userQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    userList.clear();
                    for(ParseUser user : objects) {
                        if(!alreadyFavorited.contains(user.getUsername())) {
                            userList.add(new Pair<String, String>(user.getUsername(), user.getObjectId()));
                        }
                    }
                    notifyDataSetChanged();
                }
                else {
                    Log.d(Constants.UserListAdapterTag, e.toString());
                }
            }
        });
    }

    public void loadFavoriteUsers() {
        userList.clear();
        ParseUser currUser = ParseUser.getCurrentUser();
        ArrayList<String> temp = (ArrayList<String>)currUser.get("favoriteUsers");
        Collections.sort(temp);

        for(String pair : temp) {
            String[] parts = pair.split(",");
            userList.add(new Pair(parts[0], parts[1]));
        }

        notifyDataSetChanged();
    }

    public void loadFavoritedBy() {
        String userId = ParseUser.getCurrentUser().getObjectId();
        String userName = ParseUser.getCurrentUser().getUsername();

        ParseQuery<ParseUser> query = ParseQuery.getQuery("_User");
        query.whereEqualTo("favoriteUsers", userName + "," + userId);
        query.orderByAscending("username");
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    userList.clear();
                    for(ParseUser user : objects) {
                        userList.add(new Pair<String, String>(user.getUsername(), user.getObjectId()));
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
            holder.checkBox = (CheckBox) view.findViewById(R.id.checkbox);
            holder.checkBox.setClickable(false);
            if(!useCheck) {
                holder.checkBox.setVisibility(View.GONE);
            }
            view.setTag(holder);
        }
        else {
            holder = (ViewHolder) view.getTag();
        }

        if(userList.size() == 0) {
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
        return userList.size();
    }

    @Override public Pair<String, String> getItem(int position) {
        return userList.get(position);
    }

    @Override public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        CheckBox checkBox;
        TextView username;
    }
}
