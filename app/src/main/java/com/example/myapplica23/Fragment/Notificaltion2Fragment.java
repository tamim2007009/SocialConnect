package com.example.myapplica23.Fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
// Note: We don't need to import the standard RecyclerView anymore
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast; // Good practice to show errors

import com.cooltechworks.views.shimmer.ShimmerRecyclerView; // Import ShimmerRecyclerView
import com.example.myapplica23.Adapter.NotificationAdapter;
import com.example.myapplica23.Model.Notification;
import com.example.myapplica23.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class Notificaltion2Fragment extends Fragment {

    // 1. Change the variable type from RecyclerView to ShimmerRecyclerView ✅
    ShimmerRecyclerView recyclerView;

    ArrayList<Notification> list;
    FirebaseDatabase database;
    FirebaseAuth auth;
    NotificationAdapter notificationAdapter;

    public Notificaltion2Fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notificaltion2, container, false);

        recyclerView = view.findViewById(R.id.notification2RV);
        list = new ArrayList<>();

        notificationAdapter = new NotificationAdapter(list, getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        // The adapter is set inside the ShimmerRecyclerView in the XML, but setting it again is fine.

        // Fetch notifications from Firebase
        database.getReference()
                .child("notifications")
                .child(auth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Notification notification = dataSnapshot.getValue(Notification.class);
                            if (notification != null) {
                                notification.setNotificationId(dataSnapshot.getKey());
                                list.add(notification);
                            }
                        }
                        // Show newest notifications first
                        Collections.reverse(list);

                        // Set the adapter to the RecyclerView
                        recyclerView.setAdapter(notificationAdapter);

                        // 2. Hide the shimmer effect now that the data is loaded ✅
                        recyclerView.hideShimmerAdapter();

                        notificationAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to load notifications.", Toast.LENGTH_SHORT).show();
                    }
                });

        return view;
    }
}