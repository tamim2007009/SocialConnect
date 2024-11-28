package com.example.myapplica23.Fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapplica23.Adapter.DashboardAdapter;
import com.example.myapplica23.Adapter.StoryAdapter;
import com.example.myapplica23.Model.DashboardModel;
import com.example.myapplica23.Model.StoryModel;
import com.example.myapplica23.R;

import java.util.ArrayList;


public class HomeFragment extends Fragment {

// for story
 RecyclerView storyRV;
 ArrayList<StoryModel> list;

 // for dashboard
    RecyclerView dashboardRV;
    ArrayList<DashboardModel> dashboardList;



    public HomeFragment() {

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        storyRV= view.findViewById(R.id.storyRV);
        list = new ArrayList<>();

        list.add(new StoryModel(R.drawable.story1, R.drawable.ic_back_svgrepo_com, R.drawable.ic_launcher_foreground, "John"));
        list.add(new StoryModel(R.drawable.story2, R.drawable.ic_back_svgrepo_com, R.drawable.ic_launcher_foreground, "John"));
        list.add(new StoryModel(R.drawable.story3, R.drawable.ic_back_svgrepo_com, R.drawable.ic_launcher_foreground, "John"));
        list.add(new StoryModel(R.drawable.cover1, R.drawable.ic_back_svgrepo_com, R.drawable.ic_launcher_foreground, "John"));


        StoryAdapter adapter=new StoryAdapter(list, getContext());
        LinearLayoutManager layoutManager=new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        storyRV.setLayoutManager(layoutManager);
        storyRV.setNestedScrollingEnabled(false);
        storyRV.setAdapter(adapter);


        // dashboard
        dashboardRV= view.findViewById(R.id.dashboardRV);
        dashboardList = new ArrayList<>();
        dashboardList.add(new DashboardModel(R.drawable.profile, R.drawable.post1, R.drawable.ic_more_svgrepo_com, "John",
                "This is a post", "100", "50", "10"));
        dashboardList.add(new DashboardModel(R.drawable.profile, R.drawable.post2, R.drawable.ic_more_svgrepo_com, "John",
                "This is a post", "100", "50", "10"));

        dashboardList.add(new DashboardModel(R.drawable.profile, R.drawable.post3, R.drawable.ic_more_svgrepo_com, "John",
                "This is a post", "100", "50", "10"));

        dashboardList.add(new DashboardModel(R.drawable.profile, R.drawable.post1, R.drawable.ic_more_svgrepo_com, "John",
                "This is a post", "100", "50", "10"));

        dashboardList.add(new DashboardModel(R.drawable.profile, R.drawable.post3, R.drawable.ic_more_svgrepo_com, "John",
                "This is a post", "100", "50", "10"));

        DashboardAdapter dashboardAdapter = new DashboardAdapter(dashboardList, getContext());
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(getContext());
        dashboardRV.setLayoutManager(layoutManager1);
        dashboardRV.setNestedScrollingEnabled(false);
        dashboardRV.setAdapter(dashboardAdapter);

        return  view;
    }
}