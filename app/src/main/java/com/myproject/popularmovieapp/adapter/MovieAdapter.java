package com.myproject.popularmovieapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
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

    public class MovieListAdapterViewHolder extends RecyclerView.ViewHolder {

        final ImageView mMoviePoster;

        public MovieListAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            mMoviePoster = itemView.findViewById(R.id.iv_movie_image);
        }
    }

    @NonNull
    @Override
    public MovieListAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.movies_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, parent, false);
        return new MovieListAdapterViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull MovieListAdapterViewHolder holder, int position) {

        final String urlForThisItem = mMovieData.get(position).get("moviePosterUrl");

        Picasso.get()
                .load(NetworkApi.buildThumbString(urlForThisItem))
                .fit()
                .error(R.mipmap.ic_launcher)
                .into(holder.mMoviePoster);

        holder.mMoviePoster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), DetailActivity.class);
                intent.putExtra("movieId", mMovieData.get(position).get("id"));
                intent.putExtra("originalTitle",mMovieData.get(position).get("originalTitle"));
                intent.putExtra("releaseDate",mMovieData.get(position).get("releaseDate"));
                intent.putExtra("userRating", mMovieData.get(position).get("description"));
                intent.putExtra("moviePosterUrl", urlForThisItem);

                view.getContext().startActivity(intent);

            }
        }
        );
    }

    @Override
    public int getItemCount() {
        if(null == mMovieData) return 0;
        return  mMovieData.size();
    }
    public void setmMovieData(ArrayList<HashMap<String, String>> movieData){
        mMovieData = movieData;
        notifyDataSetChanged();
    }
}
