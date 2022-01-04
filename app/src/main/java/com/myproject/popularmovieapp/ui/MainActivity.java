package com.myproject.popularmovieapp.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.myproject.popularmovieapp.R;
import com.myproject.popularmovieapp.adapter.MovieAdapter;
import com.myproject.popularmovieapp.api.NetworkApi;
import com.myproject.popularmovieapp.data.MovieContract;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<ArrayList<HashMap<String, String>>> {

    private RecyclerView mRecyclerView;
    private MovieAdapter mMovieAdapter;
    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;

    private static final String PREF_MOVIE_LIST = "MyMovieList";
    private static final String KEY_POPULAR = "popular";
    private static final String KEY_RATING = "top_rated";
    private static final String KEY_FAVORITE = "favorite";

    private SharedPreferences sharedPref;

    private static final int MOVIE_LOADER_ID = 0;
    private static final String BUNDLE_RECYCLER_LAYOUT = "recyler_layout";
    private Parcelable savedRecyclerLayoutState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = findViewById(R.id.rv_movies);
        mErrorMessageDisplay = findViewById(R.id.tv_error_message_display);
        mLoadingIndicator = findViewById(R.id.pb_loading_indicator);
        sharedPref = getSharedPreferences(PREF_MOVIE_LIST, MODE_PRIVATE);

        final int numColumns = getResources().getInteger(R.integer.gallery_columns);

        GridLayoutManager mGridLayoutManager = new GridLayoutManager(this, numColumns);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mMovieAdapter = new MovieAdapter();
        mRecyclerView.setAdapter(mMovieAdapter);
        getSupportLoaderManager().initLoader(MOVIE_LOADER_ID, null, this);
        if (savedInstanceState != null) {
            savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
            mRecyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
        }
    }

    private void showMovieDataView() {
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }


    private void showErrorMessage() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    @NonNull
    @Override
    public Loader<ArrayList<HashMap<String, String>>> onCreateLoader(int loaderId, @Nullable
            Bundle args) {

        return new AsyncTaskLoader<ArrayList<HashMap<String, String>>>(this) {

            ArrayList<HashMap<String, String>> movieDbData = null;

            @Override
            protected void onStartLoading() {
                if (movieDbData != null) {
                    deliverResult(movieDbData);
                } else {
                    mLoadingIndicator.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            @Nullable
            @Override
            public ArrayList<HashMap<String, String>> loadInBackground() {

                String prefMovieList = sharedPref.getString(PREF_MOVIE_LIST, KEY_POPULAR);

                ArrayList<HashMap<String, String>> parsedMovieData = new ArrayList<>();
                if (prefMovieList.equals(KEY_FAVORITE)) {

                    Cursor cursor = getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            null);

                    if ((cursor != null) && (cursor.getCount() > 0)) {
                        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                            @SuppressLint("Range") String movieId = cursor.getString(cursor.getColumnIndex(MovieContract
                                    .MovieEntry.COLUMN_MOVIE_ID));
                            @SuppressLint("Range") String originalTitle = cursor.getString(cursor.getColumnIndex
                                    (MovieContract.MovieEntry.COLUMN_MOVIE_NAME));
                            @SuppressLint("Range") String releaseDate = cursor.getString(cursor.getColumnIndex
                                    (MovieContract.MovieEntry.COLUMN_RELEASE_DATE));
                            @SuppressLint("Range") String userRating = cursor.getString(cursor.getColumnIndex
                                    (MovieContract.MovieEntry.COLUMN_RATING));
                            @SuppressLint("Range") String description = cursor.getString(cursor.getColumnIndex
                                    (MovieContract.MovieEntry.COLUMN_DESCRIPTION));
                            @SuppressLint("Range") String moviePosterUrl = cursor.getString(cursor.getColumnIndex
                                    (MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_URL));

                            HashMap<String, String> movieDetail = new HashMap<>();
                            movieDetail.put("id", movieId);
                            movieDetail.put("originalTitle", originalTitle);
                            movieDetail.put("releaseDate", releaseDate);
                            movieDetail.put("userRating", userRating);
                            movieDetail.put("description", description);
                            movieDetail.put("moviePosterUrl", moviePosterUrl);
                            parsedMovieData.add(movieDetail);
                        }
                        cursor.close();
                    } else {
                        return null;
                    }
                } else {
                    try {
                        URL movieDbUrl = NetworkApi.buildMovieUrl(prefMovieList);
                        String jsonResponse = NetworkApi.getResponseFromHttpUrl(movieDbUrl);
                        parsedMovieData = NetworkApi.parseJsonData(jsonResponse);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
                return parsedMovieData;
            }

            public void deliverResult(ArrayList<HashMap<String, String>> data) {
                movieDbData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<ArrayList<HashMap<String, String>>> loader,
                               ArrayList<HashMap<String, String>> data) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        if (null == data) {
            showErrorMessage();
        } else {
            showMovieDataView();
            mMovieAdapter.setMovieData(data);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<ArrayList<HashMap<String, String>>> loader) {

    }

    private void invalidateData() {
        mMovieAdapter.setMovieData(null);
    }

    // the options menu to select which movie list to load and save in sharedPreferences
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences.Editor editor = sharedPref.edit();
        int id = item.getItemId();

        if (id == R.id.most_popular_movies) {
            invalidateData();
            editor.putString(PREF_MOVIE_LIST, KEY_POPULAR);
            editor.apply();
            getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, mRecyclerView.getLayoutManager()
                .onSaveInstanceState());
    }
}