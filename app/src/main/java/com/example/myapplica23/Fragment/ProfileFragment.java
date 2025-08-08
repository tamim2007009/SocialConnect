package com.example.myapplica23.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplica23.Adapter.FollowersAdapter;
import com.example.myapplica23.Model.Follow;
import com.example.myapplica23.Model.User;
import com.example.myapplica23.R;
import com.example.myapplica23.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ProfileFragment extends Fragment {

    ArrayList<Follow> list;
    FragmentProfileBinding binding;
    FirebaseAuth auth;
    FirebaseStorage storage;
    FirebaseDatabase database;
    ProgressDialog dialog;
    private ValueEventListener userValueListener;
    private ChildEventListener followingListener; // Renamed for clarity


    public ProfileFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        dialog = new ProgressDialog(getContext());
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setTitle("Uploading Image");
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        setupUserDataListener();
        setupFollowersAdapter();
        setupFollowingListener(); // Renamed for clarity

        binding.changeCoverPhoto.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 11);
        });

        binding.verifiedAccount.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 22);
        });

        return binding.getRoot();
    }

    private void setupUserDataListener() {
        userValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null && getContext() != null) {
                        Picasso.get().load(user.getCoverPhoto()).placeholder(R.drawable.cover_placeholder).into(binding.coverPhoto);
                        binding.UserName.setText(user.getName());
                        binding.profession.setText(user.getProfession());
                        binding.followers.setText(String.valueOf(user.getFollowerCount()));
                        Picasso.get().load(user.getProfilePhoto()).placeholder(R.drawable.cover_placeholder).into(binding.profileImage);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        database.getReference().child("Users").child(auth.getUid()).addValueEventListener(userValueListener);
    }

    private void setupFollowersAdapter() {
        list = new ArrayList<>();
        FollowersAdapter adapter = new FollowersAdapter(list, getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.friendRecyclerView.setLayoutManager(linearLayoutManager);
        binding.friendRecyclerView.setAdapter(adapter);
    }

    // FIX: This method is completely rewritten to fetch the "following" list
    private void setupFollowingListener() {
        followingListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String followedUserId = snapshot.getKey();
                if (followedUserId != null) {
                    // Fetch the profile of the user we are following
                    database.getReference().child("Users").child(followedUserId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                    User user = userSnapshot.getValue(User.class);
                                    if (user != null) {
                                        Follow follow = new Follow();
                                        follow.setUserId(userSnapshot.getKey());
                                        follow.setUserName(user.getName());
                                        follow.setUserProfilePhoto(user.getProfilePhoto());
                                        list.add(follow);
                                        if (binding.friendRecyclerView.getAdapter() != null) {
                                            binding.friendRecyclerView.getAdapter().notifyItemInserted(list.size() - 1);
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        // FIX: Pointing the listener to the correct "following" node
        database.getReference().child("Users")
                .child(auth.getUid())
                .child("following")
                .addChildEventListener(followingListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getData() != null) {
            Uri uri = data.getData();
            if (requestCode == 11) {
                binding.coverPhoto.setImageURI(uri);
                uploadImage(uri, "cover_photo", "coverPhoto");
            } else if (requestCode == 22) {
                binding.profileImage.setImageURI(uri);
                uploadImage(uri, "profile_image", "profilePhoto");
            }
        }
    }

    private void uploadImage(Uri imageUri, String storagePath, String databaseKey) {
        dialog.show();
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
            byte[] data = stream.toByteArray();
            StorageReference reference = storage.getReference().child(storagePath).child(auth.getUid());
            UploadTask uploadTask = reference.putBytes(data);
            uploadTask.addOnProgressListener(snapshot -> {
                double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                dialog.setMessage("Uploaded " + (int) progress + "%");
            });
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                dialog.setMessage("Finalizing...");
                reference.getDownloadUrl().addOnSuccessListener(uri -> {
                    database.getReference().child("Users").child(auth.getUid()).child(databaseKey).setValue(uri.toString());
                    dialog.dismiss();
                    Toast.makeText(getContext(), "Photo updated successfully!", Toast.LENGTH_SHORT).show();
                });
            });
        } catch (IOException e) {
            e.printStackTrace();
            dialog.dismiss();
            Toast.makeText(getContext(), "Image processing failed.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userValueListener != null) {
            database.getReference().child("Users").child(auth.getUid()).removeEventListener(userValueListener);
        }
        // FIX: Remove listener from the correct "following" path
        if (followingListener != null) {
            database.getReference().child("Users").child(auth.getUid()).child("following").removeEventListener(followingListener);
        }
        binding = null;
    }
}