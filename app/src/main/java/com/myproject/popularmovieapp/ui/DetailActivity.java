package com.myproject.popularmovieapp.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;

import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;


import com.myproject.popularmovieapp.R;
import com.myproject.popularmovieapp.adapter.TrailerAdapter;
import com.myproject.popularmovieapp.api.NetworkApi;

import com.myproject.popularmovieapp.model.Trailer;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity implements LoaderManager
        .LoaderCallbacks<Cursor> {

    private final String TAG = DetailActivity.class.getName();

    private TextView mMovieTitle;
    private TextView mReleaseDate;
    private TextView mRating;
    private TextView mDescription;
    private ImageView mMoviePoster;
    private CheckBox mSaveButton;
    private ListView mTrailerListView;
    private TextView mTrailerListViewTitle;

    private String movieId;
    private String movieTitle;
    private String releaseDate;
    private String userRating;
    private String description;
    private String posterUrl;
    private ArrayList<Trailer> trailerList;

    private ShareActionProvider mShareActionProvider;

    private boolean favorite;

    private static final int ID_MOVIE_LOADER = 11;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mMovieTitle = findViewById(R.id.tv_title);
        mReleaseDate = findViewById(R.id.tv_release_date);
        mRating = findViewById(R.id.tv_rating);
        mDescription = findViewById(R.id.tv_description);
        mMoviePoster = findViewById(R.id.iv_detail_movie_poster);
        mTrailerListView = findViewById(R.id.listview_trailer);
        TextView mErrorMessageDisplay = findViewById(R.id.tv_error_message_display);
        mSaveButton = findViewById(R.id.save_button);
        Button mReviewButton = findViewById(R.id.btn_reviews);
        mTrailerListViewTitle = findViewById(R.id.movie_trailerlist_title);


        Intent intent = getIntent();

            if (intent != null) {
                movieId = intent.getStringExtra("movieId");
                    movieTitle = intent.getStringExtra("originalTitle");
                    releaseDate = intent.getStringExtra("releaseDate");
                    userRating = intent.getStringExtra("userRating");
                    description = intent.getStringExtra("description");
                    posterUrl = intent.getStringExtra("moviePosterUrl");
                    setData();
                    mSaveButton.setChecked(false);

            } else {
                mErrorMessageDisplay.isShown();
            }

        mReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startIntent = new Intent(DetailActivity.this, ReviewActivity.class);
                startIntent.putExtra("movieId", movieId);
                startActivity(startIntent);
            }
        });

        new FetchTrailerData().execute(movieId);
    }

    private void setData() {
        mMovieTitle.setText(movieTitle);
        String releaseDateString = getString(R.string.release_date) + " " + releaseDate;
        mReleaseDate.setText(releaseDateString);
        String userRatingString = getString(R.string.user_rating) + " " + userRating;
        mRating.setText(userRatingString);
        mDescription.setText(description);

        Picasso.get()
                .load(NetworkApi.buildThumbString(posterUrl))
                .resize(400, 550)
                .centerCrop()
                .error(R.mipmap.ic_launcher)
                .into(mMoviePoster);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void setListViewHeight(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {
            int numberOflistItems = listAdapter.getCount();
            int totalItemsHeight = 0;
            int totalWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View
                    .MeasureSpec.AT_MOST);
            for (int itemNum = 0; itemNum < numberOflistItems; itemNum++) {
                View listItem = listAdapter.getView(itemNum, null, listView);
                listItem.measure(totalWidth, View.MeasureSpec.UNSPECIFIED);
                totalItemsHeight += listItem.getMeasuredHeight();
            }
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + (listView.getDividerHeight() * listAdapter
                    .getCount() - 1);
            listView.requestLayout();
        }
    }

    private class FetchTrailerData extends AsyncTask<String, Void, ArrayList<Trailer>> {
        @Override
        protected ArrayList<Trailer> doInBackground(String... params) {
            ArrayList<Trailer> newTrailers = new ArrayList<>();
            try {
                URL trailerUrl = NetworkApi.buildTrailerUrl(params[0]);
                String jsonResponse = NetworkApi.getResponseFromHttpUrl(trailerUrl);
                try {
                    JSONObject movieDataObject = new JSONObject(jsonResponse);
                     JSONArray trailerArray = movieDataObject.getJSONArray("results");
                    newTrailers = Trailer.fromJson(trailerArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return newTrailers;
        }

        @Override
        protected void onPostExecute(final ArrayList<Trailer> trailers) {
            super.onPostExecute(trailers);
            trailerList = trailers;
            if (trailerList.size() < 1) {
                mTrailerListViewTitle.setVisibility(View.INVISIBLE);
                mTrailerListView.setVisibility(View.INVISIBLE);
            } else {
                TrailerAdapter trailerAdapter = new TrailerAdapter(DetailActivity.this,
                        trailerList);
                mTrailerListView.setAdapter(trailerAdapter);
                setListViewHeight(mTrailerListView);


                mTrailerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapter, View v, int position, long
                            arg3) {
                        String trailerKey = trailerList.get(position).link;
                        String youtubeUrlLink = NetworkApi.youtubeUrlString(trailerKey);
                        Intent startIntent = new Intent(Intent.ACTION_VIEW, Uri.parse
                                (youtubeUrlLink));
                        startActivity(startIntent);
                    }
                });
            }
        }
    }
}
