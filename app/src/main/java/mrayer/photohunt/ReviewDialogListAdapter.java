package mrayer.photohunt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matthew on 4/26/2016.
 */
public class ReviewDialogListAdapter extends BaseAdapter {

    private final Context context;
    private List<Review> reviews;
    private String albumId;

    public ReviewDialogListAdapter(Context context, String albumId) {
        this.albumId = albumId;
        this.context = context;
        reviews = new ArrayList<Review>();
    }

    public void loadReviews() {
        ParseQuery<Review> matchingReviewsQuery = new ParseQuery<Review>("Review");
        matchingReviewsQuery.whereEqualTo("albumId", albumId);
        matchingReviewsQuery.findInBackground(new FindCallback<Review>() {
            @Override
            public void done(List<Review> objects, ParseException e) {
                reviews.clear();
                reviews.addAll(objects);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.review_list_dialog_row, parent, false);
            holder = new ViewHolder();
            holder.reviewAuthor = (TextView) view.findViewById(R.id.review_author);
            holder.reviewRating = (RatingBar) view.findViewById(R.id.review_rating);
            holder.reviewComments = (TextView) view.findViewById(R.id.review_comments);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        if (reviews.size() == 0) {
            // can't do anything, we haven't finished querying for the albums yet
            return view;
        }

        Review currReview = reviews.get(position);
        holder.reviewAuthor.setText(currReview.getAuthor());
        holder.reviewRating.setRating((float) currReview.getRating());
        holder.reviewComments.setText(currReview.getText());

        return view;
    }

    @Override public int getCount() {
        return reviews.size();
    }

    @Override public Review getItem(int position) {
        return reviews.get(position);
    }

    @Override public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        TextView reviewAuthor;
        RatingBar reviewRating;
        TextView reviewComments;
    }
}
