package com.example.myapplica23.Fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapplica23.Adapter.FriendAdapter;
import com.example.myapplica23.Model.FriendModel;
import com.example.myapplica23.R;

import java.util.ArrayList;


public class ProfileFragment extends Fragment {

   RecyclerView recyclerView;
    ArrayList<FriendModel> list;

    public ProfileFragment() {

    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_profile, container, false);
       recyclerView=view.findViewById(R.id.friendRecyclerView);

        if (recyclerView == null) {
            Log.e("ProfileFragment", "RecyclerView is null");
        }
        else{
            Log.e("ProfileFragment", "RecyclerView is not null");
        }
         list=new ArrayList<>();
        list.add(new FriendModel(R.drawable.profile));
        list.add(new FriendModel(R.drawable.profile2));
        list.add(new FriendModel(R.drawable.profile2));
        list.add(new FriendModel(R.drawable.profile2));
        list.add(new FriendModel(R.drawable.profile2));
        list.add(new FriendModel(R.drawable.profile2));

        FriendAdapter adapter=new FriendAdapter(list,getContext());
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
          recyclerView.setLayoutManager(linearLayoutManager);
          recyclerView.setAdapter(adapter);

        return  view;

    }
}