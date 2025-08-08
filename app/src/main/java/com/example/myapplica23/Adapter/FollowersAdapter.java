package com.example.myapplica23.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplica23.Model.Follow;
import com.example.myapplica23.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class FollowersAdapter extends RecyclerView.Adapter<FollowersAdapter.viewHolder> {

    ArrayList<Follow> list;
    Context context;

    public FollowersAdapter(ArrayList<Follow> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.friend_rv_sample, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Follow follow = list.get(position);

        // FIX: No database calls here! Using pre-fetched data for high performance.
        Picasso.get()
                .load(follow.getUserProfilePhoto())
                .placeholder(R.drawable.cover_placeholder)
                .into(holder.profile);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        ImageView profile;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            // Make sure the ID in your friend_rv_sample.xml layout is 'profileImage'
            profile = itemView.findViewById(R.id.profile_image);
        }
    }
}