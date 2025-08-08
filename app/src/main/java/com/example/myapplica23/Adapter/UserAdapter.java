package com.example.myapplica23.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplica23.Model.Follow;
import com.example.myapplica23.Model.Notification;
import com.example.myapplica23.Model.User;
import com.example.myapplica23.R;
import com.example.myapplica23.databinding.UserSampleBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.viewHolder> {

    Context context;
    ArrayList<User> list;
    FirebaseAuth auth;
    FirebaseDatabase database;

    public UserAdapter(Context context, ArrayList<User> list) {
        this.context = context;
        this.list = list;
        this.auth = FirebaseAuth.getInstance();
        this.database = FirebaseDatabase.getInstance();
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_sample, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        User user = list.get(position);
        Picasso.get()
                .load(user.getProfilePhoto())
                .placeholder(R.drawable.cover_placeholder)
                .into(holder.binding.profileImage);
        holder.binding.name.setText(user.getName());
        holder.binding.profession.setText(user.getProfession());

        database.getReference()
                .child("Users")
                .child(auth.getUid())
                .child("following")
                .child(user.getUserID())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            setFollowingButton(holder);
                        } else {
                            holder.binding.followBtn.setOnClickListener(v -> {
                                Follow follow = new Follow();
                                follow.setFollowedBy(auth.getUid());
                                follow.setFollowedAt(new Date().getTime());

                                // ACTION 1: Add current user to the target user's "followers" list
                                database.getReference()
                                        .child("Users")
                                        .child(user.getUserID())
                                        .child("followers")
                                        .child(auth.getUid())
                                        .setValue(follow)
                                        .addOnSuccessListener(aVoid -> {
                                            // ACTION 2: Increment the target user's follower count
                                            database.getReference()
                                                    .child("Users")
                                                    .child(user.getUserID())
                                                    .child("followerCount")
                                                    .setValue(user.getFollowerCount() + 1)
                                                    .addOnSuccessListener(aVoid1 -> {
                                                        // ACTION 3: Add the target user to the current user's "following" list
                                                        database.getReference()
                                                                .child("Users")
                                                                .child(auth.getUid())
                                                                .child("following")
                                                                .child(user.getUserID())
                                                                .setValue(true)
                                                                .addOnSuccessListener(aVoid2 -> {
                                                                    setFollowingButton(holder);
                                                                    Toast.makeText(context, "You followed " + user.getName(), Toast.LENGTH_SHORT).show();


                                                                    // ACTION 4: Notification logic is now commented out
                                                                    Notification notification = new Notification();
                                                                    notification.setNotificationBy(auth.getUid());
                                                                    notification.setNotificationAt(new Date().getTime());
                                                                    notification.setType("follow");

                                                                    database.getReference()
                                                                            .child("notifications")
                                                                            .child(user.getUserID())
                                                                            .push()
                                                                            .setValue(notification);

                                                                });
                                                    });
                                        });
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void setFollowingButton(viewHolder holder) {
        holder.binding.followBtn.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.follow_active_btn));
        holder.binding.followBtn.setText("Following");
        holder.binding.followBtn.setTextColor(context.getResources().getColor(R.color.silver));
        holder.binding.followBtn.setEnabled(false);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        UserSampleBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = UserSampleBinding.bind(itemView);
        }
    }
}