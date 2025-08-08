package com.example.myapplica23.Adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.myapplica23.Fragment.Notificaltion2Fragment; // Note the typo in your original filename

public class ViewPagerAdapter extends FragmentPagerAdapter {
    public ViewPagerAdapter(@NonNull FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        // You can add more fragments for other tabs here
        if (position == 0) {
            return new Notificaltion2Fragment();
        }
        // Example for a second tab:
        // else {
        //     return new RequestsFragment();
        // }
        return new Notificaltion2Fragment(); // Default
    }

    @Override
    public int getCount() {
        // Return the number of tabs
        return 1;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        // Set the title for each tab
        if (position == 0) {
            return "Notifications";
        }
        // Example for a second tab:
        // else {
        //     return "Requests";
        // }
        return "Notifications"; // Default
    }
}