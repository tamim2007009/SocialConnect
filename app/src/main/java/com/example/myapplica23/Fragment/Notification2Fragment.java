package com.example.myapplica23.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.example.myapplica23.Adapter.NotificationAdapter;
import com.example.myapplica23.Model.Notification;
import com.example.myapplica23.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Notification2Fragment extends Fragment {

    ShimmerRecyclerView recyclerView;
    ArrayList<Notification> list;
    FirebaseDatabase database;

    public Notification2Fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notification2, container, false);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.notification2RV);
        recyclerView.showShimmerAdapter();

        list = new ArrayList<>(); // Initialize list

        NotificationAdapter adapter = new NotificationAdapter(list, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Fetch notifications
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            database.getReference().child("notification").child(uid)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            list.clear(); // Clear list to avoid duplication
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                Notification notification = dataSnapshot.getValue(Notification.class);
                                if (notification != null) {
                                    notification.setNotificationId(dataSnapshot.getKey());
                                    list.add(notification);
                                }
                            }
                            recyclerView.hideShimmerAdapter();
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            recyclerView.hideShimmerAdapter();
                        }
                    });
        }

        return view;
    }
}
