package com.myproject.popularmovieapp.adapter;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.myproject.popularmovieapp.R;
import com.myproject.popularmovieapp.model.Trailer;

import java.util.ArrayList;

public class TrailerAdapter extends ArrayAdapter {

    public TrailerAdapter(Context context, ArrayList<Trailer> trailerList){
        super(context,0,trailerList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        Trailer trailer = (Trailer) getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.trailer_list_item, parent, false);
        }

        TextView trailerName = convertView.findViewById(R.id.tv_trailer_name);

        trailerName.setText(trailer.name);

        return convertView;
    }
}
