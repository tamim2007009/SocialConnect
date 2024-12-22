package com.example.myapplica23.Fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapplica23.Adapter.NotificationAdapter;
import com.example.myapplica23.Model.NotificationModel;
import com.example.myapplica23.R;

import java.util.ArrayList;


public class Notificaltion2Fragment extends Fragment {

    RecyclerView recyclerView;
    ArrayList<NotificationModel> list;


    public Notificaltion2Fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        View view= inflater.inflate(R.layout.fragment_notificaltion2, container, false);
        recyclerView = view.findViewById(R.id.notification2RV);
        list = new ArrayList<>();
        list.add(new NotificationModel(R.drawable.ic_launcher_foreground,"<b>Tamim </> Liked your post","1 hour ago"));
        list.add(new NotificationModel(R.drawable.ic_launcher_foreground,"<b>Tamim </> Liked your post","1 hour ago"));
        list.add(new NotificationModel(R.drawable.ic_launcher_foreground,"<b>Tamim </> Liked your post","1 hour ago"));
        list.add(new NotificationModel(R.drawable.ic_launcher_foreground,"<b>Tamim </> Liked your post","1 hour ago"));
        list.add(new NotificationModel(R.drawable.ic_launcher_foreground,"<b>Tamim </> Liked your post","1 hour ago"));
        list.add(new NotificationModel(R.drawable.ic_launcher_foreground,"<b>Tamim </> Liked your post","1 hour ago"));
        NotificationAdapter notificationAdapter = new NotificationAdapter(list,getContext());
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(notificationAdapter);
        return  view;

    }
}