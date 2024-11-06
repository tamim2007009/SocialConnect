package com.example.myapplica23;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.SurfaceControl;
import android.widget.Toast;

import com.example.myapplica23.Fragment.AddFragment;
import com.example.myapplica23.Fragment.HomeFragment;
import com.example.myapplica23.Fragment.NotificationFragment;
import com.example.myapplica23.Fragment.ProfileFragment;
import com.example.myapplica23.Fragment.SearchFragment;

import com.example.myapplica23.databinding.ActivityMainBinding;
import com.iammert.library.readablebottombar.ReadableBottomBar;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, new HomeFragment());
        transaction.commit();
        binding.readableBottomBar.setOnItemSelectListener(new ReadableBottomBar.ItemSelectListener() {
            @Override
            public void onItemSelected(int i) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                switch (i){
                    case 0:
                        transaction.replace(R.id.container, new HomeFragment());
                        break;
                    case 1:
                      transaction.replace(R.id.container, new NotificationFragment());
                        break;
                    case 2:
                       transaction.replace(R.id.container, new AddFragment());
                        break;
                    case 3:
                      transaction.replace(R.id.container, new SearchFragment());
                        break;
                    case 4:
                     transaction.replace(R.id.container, new ProfileFragment());
                        break;
                }
                transaction.commit();

            }
        });
    }
}