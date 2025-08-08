package com.example.myapplica23.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.myapplica23.Adapter.FollowersAdapter;
import com.example.myapplica23.Model.Follow;
import com.example.myapplica23.Model.User;
import com.example.myapplica23.R;
import com.example.myapplica23.databinding.FragmentProfileBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
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
    // OPTIMIZATION: Create listener variables to manage their lifecycle
    private ValueEventListener userValueListener;
    private ChildEventListener followersChildListener;


    public ProfileFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        // Initialize ProgressDialog for uploads
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
        setupFollowersListener();

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
        // Use addValueEventListener to keep profile data updated in real-time
        userValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    Picasso.get()
                            .load(user.getCoverPhoto())
                            .placeholder(R.drawable.cover_placeholder)
                            .into(binding.coverPhoto);
                    binding.UserName.setText(user.getName());
                    binding.profession.setText(user.getProfession());
                    binding.followers.setText(String.valueOf(user.getFollowerCount()));
                    Picasso.get()
                            .load(user.getProfilePhoto())
                            .placeholder(R.drawable.cover_placeholder)
                            .into(binding.profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load user data.", Toast.LENGTH_SHORT).show();
            }
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

    private void setupFollowersListener() {
        // OPTIMIZATION: Use a more efficient ChildEventListener
        followersChildListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Follow follow = snapshot.getValue(Follow.class);
                list.add(follow);
                binding.friendRecyclerView.getAdapter().notifyItemInserted(list.size() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                // Optional: Handle follower removal if needed
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        database.getReference().child("Users")
                .child(auth.getUid())
                .child("followers").addChildEventListener(followersChildListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && data.getData() != null) {
            Uri uri = data.getData();
            // OPTIMIZATION: Reusable function handles both cases
            if (requestCode == 11) {
                binding.coverPhoto.setImageURI(uri);
                uploadImage(uri, "cover_photo", "coverPhoto");
            } else if (requestCode == 22) {
                binding.profileImage.setImageURI(uri);
                uploadImage(uri, "profile_image", "profilePhoto");
            }
        }
    }

    // OPTIMIZATION: A single, reusable, and compressed image upload function
    private void uploadImage(Uri imageUri, String storagePath, String databaseKey) {
        dialog.show();
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
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
        // OPTIMIZATION: Remove listeners to prevent memory leaks when the view is destroyed
        if (userValueListener != null) {
            database.getReference().child("Users").child(auth.getUid()).removeEventListener(userValueListener);
        }
        if (followersChildListener != null) {
            database.getReference().child("Users").child(auth.getUid()).child("followers").removeEventListener(followersChildListener);
        }
        binding = null; // Avoid memory leaks
    }
}