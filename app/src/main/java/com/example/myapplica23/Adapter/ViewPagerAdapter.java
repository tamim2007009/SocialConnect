package com.example.myapplica23.Adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.myapplica23.Fragment.Notificaltion2Fragment;
import com.example.myapplica23.Fragment.RequestFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    public ViewPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0: return new Notificaltion2Fragment();
            case 1: return new RequestFragment();
            default: return new Notificaltion2Fragment();
        }

    }

    @Override
    public int getCount() {
        return 2 ;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        String title = null;
        if (position == 0){
            title = "NOTIFICATION";}
        else if (position == 1){
            title = "REQUEST";
        }
        return title;
    }
}
