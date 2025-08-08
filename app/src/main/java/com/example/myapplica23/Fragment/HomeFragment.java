package com.example.myapplica23.Fragment;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.example.myapplica23.Adapter.PostAdapter;
import com.example.myapplica23.Adapter.StoryAdapter;
import com.example.myapplica23.Model.Post;
import com.example.myapplica23.Model.Story;
import com.example.myapplica23.Model.User;
import com.example.myapplica23.Model.UserStories;
import com.example.myapplica23.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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

    private Set<String> followingIds;
    private ValueEventListener followingListener, postsListener, storiesListener;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        followingIds = new HashSet<>();
        storyList = new ArrayList<>();
        postList = new ArrayList<>();

        dialog = new ProgressDialog(getContext());
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setTitle("Story Uploading");
        dialog.setMessage("Please Wait...");
        dialog.setCancelable(false);

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), this::uploadStory);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        setupViews(view);
        setupAdapters();

        dashboardRV.showShimmerAdapter();
        storyRV.showShimmerAdapter();

        fetchFollowingListThenContent();

        addStoryImage.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        CircleImageView profileImage = view.findViewById(R.id.profile_image);
        profileImage.setOnClickListener(v -> navigateToProfile());

        return view;
    }

    private void setupViews(View view) {
        dashboardRV = view.findViewById(R.id.dashboardRV);
        storyRV = view.findViewById(R.id.storyRV);
        addStoryImage = view.findViewById(R.id.addStoryImage);
    }

    private void setupAdapters() {
        StoryAdapter storyAdapter = new StoryAdapter(storyList, getContext());
        storyRV.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        storyRV.setAdapter(storyAdapter);
        storyRV.setNestedScrollingEnabled(false);

        PostAdapter postAdapter = new PostAdapter(postList, getContext());
        dashboardRV.setLayoutManager(new LinearLayoutManager(getContext()));
        dashboardRV.setAdapter(postAdapter);
        dashboardRV.setNestedScrollingEnabled(false);
    }

    private void fetchFollowingListThenContent() {
        followingListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followingIds.clear();
                followingIds.add(auth.getUid());
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    followingIds.add(dataSnapshot.getKey());
                }

                fetchStories();
                fetchPosts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Could not fetch following list.", Toast.LENGTH_SHORT).show();
            }
        };
        database.getReference().child("Users")
                .child(auth.getUid())
                .child("following")
                .addValueEventListener(followingListener);
    }

    private void fetchStories() {
        storiesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                storyList.clear();
                storyRV.getAdapter().notifyDataSetChanged();

                if (snapshot.exists()) {
                    for (DataSnapshot storySnapshot : snapshot.getChildren()) {
                        String storyById = storySnapshot.getKey();

                        if (followingIds.contains(storyById)) {

                            Story tempStory = new Story();
                            tempStory.setStoryBy(storyById);
                            tempStory.setStoryAt(storySnapshot.child("postedBy").getValue(Long.class));

                            ArrayList<UserStories> userStories = new ArrayList<>();
                            for (DataSnapshot snapshot1 : storySnapshot.child("userStories").getChildren()) {
                                UserStories singleStory = snapshot1.getValue(UserStories.class);
                                userStories.add(singleStory);
                            }
                            tempStory.setStories(userStories);

                            if (tempStory.getStories() != null && !tempStory.getStories().isEmpty()) {
                                database.getReference().child("Users").child(storyById)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                                User user = userSnapshot.getValue(User.class);
                                                if (user != null) {
                                                    tempStory.setUserName(user.getName());
                                                    tempStory.setUserProfilePhoto(user.getProfilePhoto());

                                                    storyList.add(tempStory);
                                                    storyRV.getAdapter().notifyItemInserted(storyList.size() - 1);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {}
                                        });
                            }
                        }
                    }
                }
                storyRV.hideShimmerAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        database.getReference().child("stories").addValueEventListener(storiesListener);
    }


    private void fetchPosts() {
        postsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);

                    if (post != null && followingIds.contains(post.getPostedBy())) {
                        post.setPostId(dataSnapshot.getKey());
                        postList.add(post);
                    }
                }
                Collections.reverse(postList);
                dashboardRV.getAdapter().notifyDataSetChanged();
                dashboardRV.hideShimmerAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        database.getReference().child("posts").addValueEventListener(postsListener);
    }

    private void uploadStory(Uri resultUri) {
        if (resultUri == null) return;

        addStoryImage.setImageURI(resultUri);
        dialog.show();

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), resultUri);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream);
            byte[] data = stream.toByteArray();

            final StorageReference reference = storage.getReference()
                    .child("stories")
                    .child(auth.getUid())
                    .child(new Date().getTime() + ".jpg");

            reference.putBytes(data).addOnProgressListener(snapshot -> {
                double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                dialog.setMessage("Uploaded " + (int) progress + "%");
            }).addOnSuccessListener(taskSnapshot -> reference.getDownloadUrl().addOnSuccessListener(uri -> {

                Story story = new Story();
                story.setStoryAt(new Date().getTime());

                database.getReference()
                        .child("stories")
                        .child(auth.getUid())
                        .child("postedBy")
                        .setValue(story.getStoryAt())
                        .addOnSuccessListener(unused -> {
                            UserStories userStory = new UserStories(uri.toString(), story.getStoryAt());
                            database.getReference()
                                    .child("stories")
                                    .child(auth.getUid())
                                    .child("userStories")
                                    .push()
                                    .setValue(userStory)
                                    .addOnSuccessListener(aVoid -> dialog.dismiss());
                        });
            })).addOnFailureListener(e -> {
                dialog.dismiss();
                Toast.makeText(getContext(), "Upload failed.", Toast.LENGTH_SHORT).show();
            });

        } catch (IOException e) {
            e.printStackTrace();
            dialog.dismiss();
        }
    }

    private void navigateToProfile() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new ProfileFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (followingListener != null) {
            database.getReference().child("Users").child(auth.getUid()).child("following").removeEventListener(followingListener);
        }
        if (postsListener != null) {
            database.getReference().child("posts").removeEventListener(postsListener);
        }
        if (storiesListener != null) {
            database.getReference().child("stories").removeEventListener(storiesListener);
        }
    }
}