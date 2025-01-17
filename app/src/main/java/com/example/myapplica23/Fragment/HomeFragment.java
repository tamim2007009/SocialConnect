package com.example.myapplica23.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.example.myapplica23.Adapter.PostAdapter;
import com.example.myapplica23.Adapter.StoryAdapter;
import com.example.myapplica23.Model.Post;
import com.example.myapplica23.Model.Story;
import com.example.myapplica23.Model.UserStories;
import com.example.myapplica23.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment {

    ShimmerRecyclerView dashboardRV, storyRV;
    ArrayList<Story> storyList;
    ArrayList<Post> postList;
    FirebaseDatabase database;
    FirebaseAuth auth;
    FirebaseStorage storage;
    RoundedImageView addStoryImage;
    ActivityResultLauncher<String> galleryLauncher;
    ProgressDialog dialog;

    public HomeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dialog = new ProgressDialog(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        dashboardRV = view.findViewById(R.id.dashboardRV);
        dashboardRV.showShimmerAdapter();

        storyRV = view.findViewById(R.id.storyRV);
        storyRV.showShimmerAdapter();

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Story Uploading");
        dialog.setMessage("Please Wait");
        dialog.setCancelable(false);

        // Story list setup
        storyList = new ArrayList<>();
        StoryAdapter storyAdapter = new StoryAdapter(storyList, getContext());
        LinearLayoutManager storyLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        storyRV.setLayoutManager(storyLayoutManager);
        storyRV.setNestedScrollingEnabled(false);

        database.getReference()
                .child("stories")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            storyList.clear();
                            for (DataSnapshot storySnapshot : snapshot.getChildren()) {
                                Story story = new Story();
                                story.setStoryBy(storySnapshot.getKey());
                                story.setStoryAt(storySnapshot.child("postedBy").getValue(Long.class));

                                ArrayList<UserStories> userStories = new ArrayList<>();
                                for (DataSnapshot snapshot1 : storySnapshot.child("userStories").getChildren()) {
                                    UserStories singleStory = snapshot1.getValue(UserStories.class);
                                    userStories.add(singleStory);
                                }
                                story.setStories(userStories);
                                storyList.add(story);
                            }

                            storyRV.setAdapter(storyAdapter);
                            storyRV.hideShimmerAdapter();
                            storyAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        // Post list setup
        postList = new ArrayList<>();
        PostAdapter postAdapter = new PostAdapter(postList, getContext());
        LinearLayoutManager postLayoutManager = new LinearLayoutManager(getContext());
        dashboardRV.setLayoutManager(postLayoutManager);
        dashboardRV.setNestedScrollingEnabled(false);

        // Fetch posts in newest-first order
        database.getReference()
                .child("posts")
                .orderByChild("postedAt") // Ensure posts are sorted by their timestamp in Firebase
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        postList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Post post = dataSnapshot.getValue(Post.class);
                            if (post != null) {
                                post.setPostId(dataSnapshot.getKey());
                                postList.add(post);
                            }
                        }
                        // Reverse the post list for newest-first display
                        Collections.reverse(postList);

                        dashboardRV.setAdapter(postAdapter);
                        dashboardRV.hideShimmerAdapter();
                        postAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        // Other UI interactions
        addStoryImage = view.findViewById(R.id.addStoryImage);
        addStoryImage.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                result -> {
                    if (result != null) {
                        addStoryImage.setImageURI(result);
                        dialog.show();

                        StorageReference reference = storage.getReference()
                                .child("stories")
                                .child(FirebaseAuth.getInstance().getUid())
                                .child(new Date().getTime() + "");
                        reference.putFile(result).addOnSuccessListener(taskSnapshot -> reference.getDownloadUrl().addOnSuccessListener(uri -> {
                            Story story = new Story();
                            story.setStoryAt(new Date().getTime());
                            database.getReference()
                                    .child("stories")
                                    .child(FirebaseAuth.getInstance().getUid())
                                    .child("postedBy")
                                    .setValue(story.getStoryAt())
                                    .addOnSuccessListener(unused -> {
                                        UserStories userStory = new UserStories(uri.toString(), story.getStoryAt());
                                        database.getReference()
                                                .child("stories")
                                                .child(FirebaseAuth.getInstance().getUid())
                                                .child("userStories")
                                                .push()
                                                .setValue(userStory)
                                                .addOnSuccessListener(aVoid -> dialog.dismiss());
                                    });
                        }));
                    }
                });

        return view;
    }
}
