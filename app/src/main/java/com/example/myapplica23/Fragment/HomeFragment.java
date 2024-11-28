package com.example.myapplica23.Fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapplica23.Adapter.StoryAdapter;
import com.example.myapplica23.Model.StoryModel;
import com.example.myapplica23.R;

import java.util.ArrayList;


public class HomeFragment extends Fragment {


 RecyclerView storyRV;
 ArrayList<StoryModel> list;
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

        return  view;
    }
}