package com.example.myapplica23.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplica23.CommentActivity;
import com.example.myapplica23.Model.Post;
import com.example.myapplica23.Model.User;
import com.example.myapplica23.R;
import com.example.myapplica23.databinding.DashboardRvBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.viewHolder>{
    ArrayList<Post> list;
    Context context;
    FirebaseAuth auth;
    FirebaseDatabase database;

    public PostAdapter(ArrayList<Post> list, Context context) {
        this.list = list;
        this.context = context;
        this.auth = FirebaseAuth.getInstance();
        this.database = FirebaseDatabase.getInstance();
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.dashboard_rv, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Post model = list.get(position);
        String currentUserId = auth.getUid();

        // Load post image
        Picasso.get()
                .load(model.getPostImage())
                .placeholder(R.drawable.cover_placeholder)
                .into(holder.binding.postImg);

        // Set like and comment counts
        holder.binding.like.setText(String.valueOf(model.getPostLike()));
        holder.binding.comment.setText(String.valueOf(model.getCommentCount()));

        // Handle post description
        String description = model.getPostDescription();
        if (description == null || description.isEmpty()){
            holder.binding.postDescription.setVisibility(View.GONE);
        } else {
            holder.binding.postDescription.setText(model.getPostDescription());
            holder.binding.postDescription.setVisibility(View.VISIBLE);
        }

        // Fetch user info for the post ONCE
        database.getReference().child("Users")
                .child(model.getPostedBy()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            Picasso.get()
                                    .load(user.getProfilePhoto())
                                    .placeholder(R.drawable.cover_placeholder)
                                    .into(holder.binding.profileImage);
                            holder.binding.userName.setText(user.getName());
                            holder.binding.about.setText(user.getProfession());
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        // ================== HIGH-PERFORMANCE LIKE LOGIC ==================
        // No database call here! We check the data pre-fetched by HomeFragment.
        if (model.getLikes() != null && model.getLikes().containsKey(currentUserId)) {
            // User has liked this post, show red heart
            holder.binding.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_red_heart_svgrepo_com, 0, 0, 0);
        } else {
            // User has not liked this post, show default heart
            holder.binding.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_svgrepo_com, 0, 0, 0); // Make sure you have a default grey heart icon named ic_heart
        }

        // Handle clicking the like button
        holder.binding.like.setOnClickListener(v -> {
            // Use the local data to decide whether to like or unlike
            boolean isCurrentlyLiked = model.getLikes() != null && model.getLikes().containsKey(currentUserId);

            if (isCurrentlyLiked) {
                // UNLIKE: Remove the like from the database
                database.getReference()
                        .child("posts")
                        .child(model.getPostId())
                        .child("likes")
                        .child(currentUserId)
                        .removeValue();

                // Update local model and UI
                model.getLikes().remove(currentUserId);
                model.setPostLike(model.getPostLike() - 1);
                holder.binding.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_svgrepo_com, 0, 0, 0);

            } else {
                // LIKE: Add the like to the database
                database.getReference()
                        .child("posts")
                        .child(model.getPostId())
                        .child("likes")
                        .child(currentUserId)
                        .setValue(true);

                // Update local model and UI
                model.getLikes().put(currentUserId, true);
                model.setPostLike(model.getPostLike() + 1);
                holder.binding.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_red_heart_svgrepo_com, 0, 0, 0);
            }
            // Update the like count text in both cases
            holder.binding.like.setText(String.valueOf(model.getPostLike()));
        });
        // ==============================================================

        // Comment button listener
        holder.binding.comment.setOnClickListener(v -> {
            Intent intent = new Intent(context, CommentActivity.class);
            intent.putExtra("postId", model.getPostId());
            intent.putExtra("postedBy", model.getPostedBy());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{
        DashboardRvBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DashboardRvBinding.bind(itemView);
        }
    }
}