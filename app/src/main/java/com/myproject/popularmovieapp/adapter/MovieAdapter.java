package com.myproject.popularmovieapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.myproject.popularmovieapp.R;
import com.myproject.popularmovieapp.api.NetworkApi;
import com.myproject.popularmovieapp.ui.DetailActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieListAdapterViewHolder> {

    private ArrayList<HashMap<String, String>> mMovieData;

    public MovieAdapter() {
    }

    public static class MovieListAdapterViewHolder extends RecyclerView.ViewHolder {

        final ImageView moviePoster;
        TextView movieName;
        TextView movieLanguage;
        TextView moviePopularity;
        CardView movieCardview;

        MovieListAdapterViewHolder(View view) {
            super(view);
            moviePoster = view.findViewById(R.id.iv_movie_image);
            movieName = view.findViewById(R.id.movieIdTextview);
            movieLanguage = view.findViewById(R.id.movieLanguage);
            movieCardview = view.findViewById(R.id.cardView);
            moviePopularity = view.findViewById(R.id.moviePopularity);
        }
    }

    @NonNull
    @Override
    public MovieListAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int
            viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.movies_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, viewGroup, false);
        return new MovieListAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MovieListAdapterViewHolder holder, final int
            position) {

        final String urlForThisItem = mMovieData.get(position).get("moviePosterUrl");
        final String movieId = mMovieData.get(position).get("originalTitle");
        final String movieLanguageData = mMovieData.get(position).get("language");
        final String moviePopularityData = mMovieData.get(position).get("popularity");

        Picasso.get()
                .load(NetworkApi.buildThumbString(urlForThisItem))
                .fit()
                .error(R.mipmap.ic_launcher)
                .into(holder.moviePoster);

        holder.movieName.setText(movieId);
        holder.movieLanguage.setText(movieLanguageData);
        holder.moviePopularity.setText(moviePopularityData);

        holder.movieCardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), DetailActivity.class);
                intent.putExtra("movieId", mMovieData.get(position).get("id"));
                intent.putExtra("originalTitle", mMovieData.get(position).get("originalTitle"));
                intent.putExtra("releaseDate", mMovieData.get(position).get("releaseDate"));
                intent.putExtra("userRating", mMovieData.get(position).get("userRating"));
                intent.putExtra("description", mMovieData.get(position).get("description"));
                intent.putExtra("moviePosterUrl", urlForThisItem);

                view.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (null == mMovieData) return 0;
        return mMovieData.size();
    }

    public void setMovieData(ArrayList<HashMap<String, String>> movieData) {
        mMovieData = movieData;
        notifyDataSetChanged();
    }
}