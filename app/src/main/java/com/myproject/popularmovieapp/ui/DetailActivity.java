package com.myproject.popularmovieapp.ui;

import static com.myproject.popularmovieapp.data.MovieContract.MovieEntry.COLUMN_DESCRIPTION;
import static com.myproject.popularmovieapp.data.MovieContract.MovieEntry.COLUMN_MOVIE_NAME;
import static com.myproject.popularmovieapp.data.MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_URL;
import static com.myproject.popularmovieapp.data.MovieContract.MovieEntry.COLUMN_RATING;
import static com.myproject.popularmovieapp.data.MovieContract.MovieEntry.COLUMN_RELEASE_DATE;
import static com.myproject.popularmovieapp.data.MovieContract.MovieEntry.CONTENT_URI;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.core.view.MenuItemCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.myproject.popularmovieapp.R;
import com.myproject.popularmovieapp.adapter.TrailerAdapter;
import com.myproject.popularmovieapp.api.NetworkApi;
import com.myproject.popularmovieapp.data.MovieContract;
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

        if (savedInstanceState != null) {
            movieId = savedInstanceState.getString("movieId");
            movieTitle = savedInstanceState.getString("originalTitle");
            releaseDate = savedInstanceState.getString("releaseDate");
            userRating = savedInstanceState.getString("userRating");
            description = savedInstanceState.getString("description");
            posterUrl = savedInstanceState.getString("moviePosterUrl");
            setData();
        } else {
            if (intent != null) {
                movieId = intent.getStringExtra("movieId");
                // if  movieId is found in favorites, use the CursorLoader to load data from DB
                favorite = isFavourite(movieId);
                if (favorite) {
                    getSupportLoaderManager().initLoader(ID_MOVIE_LOADER, null, this);
                    mSaveButton.setChecked(true);
                } else {
                    // if movieId is not found in favorites, take the data from the intent
                    movieTitle = intent.getStringExtra("originalTitle");
                    releaseDate = intent.getStringExtra("releaseDate");
                    userRating = intent.getStringExtra("userRating");
                    description = intent.getStringExtra("description");
                    posterUrl = intent.getStringExtra("moviePosterUrl");
                    setData();
                    mSaveButton.setChecked(false);
                }
            } else {
                mErrorMessageDisplay.isShown();
            }
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

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        switch (loaderId) {
            case ID_MOVIE_LOADER:
                return new CursorLoader(this,
                        CONTENT_URI,
                        null,
                        MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?",
                        new String[]{movieId},
                        null);
            default:
                throw new RuntimeException("Loader not implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            movieTitle = cursor.getString(cursor.getColumnIndex(COLUMN_MOVIE_NAME));
            releaseDate = cursor.getString(cursor.getColumnIndex(COLUMN_RELEASE_DATE));
            userRating = cursor.getString(cursor.getColumnIndex(COLUMN_RATING));
            description = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION));
            posterUrl = cursor.getString(cursor.getColumnIndex(COLUMN_MOVIE_POSTER_URL));
        }
        setData();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public void toggleFavorite(View view) {
        switch (view.getId()) {
            case R.id.save_button:
                if (!favorite) {
                    saveFavourite(movieId, movieTitle, releaseDate, userRating, description,
                            posterUrl);
                    mSaveButton.setChecked(true);
                } else {
                    removeFavourite(movieId);
                    mSaveButton.setChecked(false);
                }
        }
    }

    private boolean isFavourite(String movieId) {
        Cursor cursor = getContentResolver()
                .query(CONTENT_URI,
                        new String[]{MovieContract.MovieEntry.COLUMN_MOVIE_ID},
                        MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?",
                        new String[]{movieId}, null);
        if (cursor != null) {
            boolean isFavourite = cursor.getCount() > 0;
            cursor.close();
            return isFavourite;
        }
        return false;
    }

    private boolean saveFavourite(String movieId, String title, String releaseDate, String
            userRating, String description, String posterUrl) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movieId);
        contentValues.put(COLUMN_MOVIE_NAME, title);
        contentValues.put(COLUMN_RELEASE_DATE, releaseDate);
        contentValues.put(COLUMN_RATING, userRating);
        contentValues.put(COLUMN_DESCRIPTION, description);
        contentValues.put(COLUMN_MOVIE_POSTER_URL, posterUrl);

        if (getContentResolver().insert(CONTENT_URI, contentValues) != null) {
            Toast.makeText(getApplicationContext(), "Add movie as favourite!", Toast
                    .LENGTH_SHORT).show();
            favorite = isFavourite(movieId);
            mSaveButton.setChecked(favorite);
            return true;
        } else {
            Toast.makeText(getApplicationContext(), "Add Error!", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private boolean removeFavourite(String movieId) {
        Uri uri = CONTENT_URI.buildUpon().appendPath(movieId).build();
        int deletedRows = getContentResolver().delete(uri, null, null);
        if (deletedRows > 0) {
            Toast.makeText(getApplicationContext(), "Remove movie from favourites!", Toast
                    .LENGTH_SHORT).show();
            favorite = isFavourite(movieId);
            mSaveButton.setChecked(favorite);
            return true;
        } else {
            Toast.makeText(getApplicationContext(), "Remove error!", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("movieId", movieId);
        outState.putString("originalTitle", movieTitle);
        outState.putString("releaseDate", releaseDate);
        outState.putString("userRating", userRating);
        outState.putString("description", description);
        outState.putString("moviePosterUrl", posterUrl);
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

                Intent shareMovieIntent = new Intent(Intent.ACTION_SEND);
                shareMovieIntent.setType("text/plain");
                shareMovieIntent.putExtra(Intent.EXTRA_TEXT,
                        "Hey, we should watch this movie: "
                                + movieTitle + " "
                                + NetworkApi.youtubeUrlString(trailerList.get(0).link));
                if (mShareActionProvider != null) {
                    mShareActionProvider.setShareIntent(shareMovieIntent);
                } else {
                    Log.d(TAG, "Kein ShareActionProvider vorhanden!");
                }

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
