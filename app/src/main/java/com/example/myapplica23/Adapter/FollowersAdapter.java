package com.example.myapplica23.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplica23.Model.Follow;
import com.example.myapplica23.Model.FriendModel;
import com.example.myapplica23.Model.User;
import com.example.myapplica23.R;
import com.example.myapplica23.databinding.FriendRvSampleBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(follow.getFollowedBy()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        Picasso.get()
                                .load(user.getProfilePhoto())
                                .placeholder(R.drawable.cover_placeholder)
                                .into(holder.binding.profileImage);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{

        FriendRvSampleBinding binding;
        public viewHolder(@NonNull View itemView) {

            super(itemView);

            binding = FriendRvSampleBinding.bind(itemView);
        }
    }
}
