package com.myproject.popularmovieapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.myproject.popularmovieapp.R;
import com.myproject.popularmovieapp.model.Review;

import java.util.ArrayList;

public class ReviewAdapter extends ArrayAdapter {

    public ReviewAdapter(Context context, ArrayList<Review> reviewList) {
        super(context, 0, reviewList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Review review = (Review) getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.review_list_item,
                    parent, false);
        }

        TextView reviewName = convertView.findViewById(R.id.tv_review_name);
        TextView reviewContent = convertView.findViewById(R.id.tv_review_content);

        reviewName.setText(review.name);
        reviewContent.setText(review.content);

        return convertView;
    }
}