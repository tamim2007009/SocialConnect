package com.example.myapplica23;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.myapplica23.Adapter.CommentAdapter;
import com.example.myapplica23.Model.Comment;
import com.example.myapplica23.Model.Notification;
import com.example.myapplica23.Model.Post;
import com.example.myapplica23.Model.User;
import com.example.myapplica23.databinding.ActivityCommentBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class CommentActivity extends AppCompatActivity {

    ActivityCommentBinding binding;
    Intent intent;
    String postId, postedBy;
    FirebaseDatabase database;
    FirebaseAuth auth;
    ArrayList<Comment> list = new ArrayList<>();
    CommentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        intent = getIntent();

        setSupportActionBar(binding.toolbar3);
        CommentActivity.this.setTitle("Comments");
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_svgrepo_com);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        postId = intent.getStringExtra("postId");
        postedBy = intent.getStringExtra("postedBy");

        adapter = new CommentAdapter(this, list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.commentRV.setLayoutManager(layoutManager);
        binding.commentRV.setNestedScrollingEnabled(false);
        binding.commentRV.setAdapter(adapter);

        fetchPostInfo();
        fetchComments();
        setupCommentButton();

        // --- NEW CODE: Use a Firebase Transaction to handle likes ---
        binding.like.setOnClickListener(v -> {
            DatabaseReference postRef = database.getReference().child("posts").child(postId);
            postRef.runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                    Post post = mutableData.getValue(Post.class);
                    if (post == null) {
                        return Transaction.success(mutableData);
                    }

                    String currentUserId = auth.getUid();

                    // Check if the user has already liked the post
                    if (post.getLikes() != null && post.getLikes().containsKey(currentUserId)) {
                        // User has liked it, so UNLIKE it
                        post.setPostLike(post.getPostLike() - 1);
                        post.getLikes().remove(currentUserId);
                    } else {
                        // User has not liked it, so LIKE it
                        post.setPostLike(post.getPostLike() + 1);
                        post.getLikes().put(currentUserId, true);
                    }

                    // Save the updated post object back to the database
                    mutableData.setValue(post);
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                    if (error != null) {
                        Toast.makeText(CommentActivity.this, "Failed to update like.", Toast.LENGTH_SHORT).show();
                    }
                    if (committed) {
                        // Transaction was successful. Now check if we need to send a notification.
                        Post post = currentData.getValue(Post.class);
                        String currentUserId = auth.getUid();

                        // Condition: Was the post just liked? And was it by a different user?
                        if (post != null && post.getLikes().containsKey(currentUserId) && !currentUserId.equals(postedBy)) {
                            // Send notification
                            Notification notification = new Notification();
                            notification.setNotificationBy(currentUserId);
                            notification.setNotificationAt(new Date().getTime());
                            notification.setPostId(postId);
                            notification.setPostedBy(postedBy);
                            notification.setType("like");

                            FirebaseDatabase.getInstance().getReference()
                                    .child("notifications")
                                    .child(postedBy)
                                    .push()
                                    .setValue(notification);
                        }
                    }
                }
            });
        });
    }

    private void fetchComments() {
        database.getReference()
                .child("posts")
                .child(postId)
                .child("comments").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Comment comment = dataSnapshot.getValue(Comment.class);
                            if (comment != null) {
                                list.add(comment);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CommentActivity.this, "Failed to load comments.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchPostInfo() {
        // This single listener now handles all post updates, including likes.
        database.getReference()
                .child("posts")
                .child(postId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Post post = snapshot.getValue(Post.class);
                        if (post == null) return;

                        Picasso.get()
                                .load(post.getPostImage())
                                .placeholder(R.drawable.cover_placeholder)
                                .into(binding.postImage);

                        binding.description.setText(post.getPostDescription());
                        binding.like.setText(String.valueOf(post.getPostLike()));
                        binding.comment.setText(String.valueOf(post.getCommentCount()));

                        // Update the like button icon
                        if (post.getLikes() != null && post.getLikes().containsKey(Objects.requireNonNull(auth.getUid()))) {
                            binding.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_red_heart_svgrepo_com, 0, 0, 0);
                        } else {
                            binding.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_svgrepo_com, 0, 0, 0);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

        database.getReference().child("Users")
                .child(postedBy).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user == null) return;
                        Picasso.get()
                                .load(user.getProfilePhoto())
                                .placeholder(R.drawable.cover_placeholder)
                                .into(binding.profileImage);
                        binding.name.setText(user.getName());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void setupCommentButton() {
        binding.commentPostBtn.setOnClickListener(v -> {
            if (binding.commentET.getText().toString().trim().isEmpty()) {
                Toast.makeText(CommentActivity.this, "Comment cannot be empty.", Toast.LENGTH_SHORT).show();
                return;
            }
            // Logic to increment commentCount using a transaction can also be implemented here
            // For now, keeping the existing logic for comments
            Comment comment = new Comment();
            comment.setCommentBody(binding.commentET.getText().toString());
            comment.setCommentedAt(new Date().getTime());
            comment.setCommentedBy(auth.getUid());

            database.getReference()
                    .child("posts")
                    .child(postId)
                    .child("comments")
                    .push()
                    .setValue(comment).addOnSuccessListener(aVoid -> {
                        binding.commentET.setText("");
                        Toast.makeText(CommentActivity.this, "Commented", Toast.LENGTH_SHORT).show();
                        if (!Objects.requireNonNull(auth.getUid()).equals(postedBy)) {
                            // Send notification for the comment
                        }
                    });
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}