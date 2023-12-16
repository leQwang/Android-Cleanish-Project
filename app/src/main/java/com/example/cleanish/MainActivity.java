package com.example.cleanish;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.cleanish.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    String intentDefaultFragment;

    private int navHome, navMap, navFilter, navProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        intentDefaultFragment = intent.getStringExtra("fragment");
        String purpose = intent.getStringExtra("purpose");
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        navHome = R.id.navHome;
        navMap = R.id.navMap;
        navFilter = R.id.navFilter;
        navProfile = R.id.navProfile;

        if(intentDefaultFragment.equals("home")){
//            2131362109
            binding.bottomNav.setSelectedItemId(Integer.valueOf(navHome)); //Use the id of the item to set selected
            replaceFragment(new HomeFragment());
        } else if (intentDefaultFragment.equals("map") && purpose == null) {
//            2131362110
            binding.bottomNav.setSelectedItemId(Integer.valueOf(navMap));
            replaceFragment(new MapsFragment());
        }else if (intentDefaultFragment.equals("map")) {
//            2131362110
            binding.bottomNav.setSelectedItemId(Integer.valueOf(navMap));
//            replaceFragment(new MapsFragment());
                Bundle bundle = new Bundle();
                String lat = intent.getStringExtra("latitude");
                String lng = intent.getStringExtra("longitude");

                double latitude = 0.0;
                double longitude = 0.0;

                try {
                    latitude = Double.parseDouble(lat);
                    longitude = Double.parseDouble(lng);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                bundle.putDouble("lat", latitude);
                bundle.putDouble("lng", longitude);

                MapsFragment mapsFragment = new MapsFragment();
                mapsFragment.setArguments(bundle);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frameLayout, mapsFragment)
                        .commit();

        }else if (intentDefaultFragment.equals("filter")) {
//            2131362108
            binding.bottomNav.setSelectedItemId(Integer.valueOf(navFilter));
            replaceFragment(new FilterFragment());
        }else {
//            2131362111
            binding.bottomNav.setSelectedItemId(Integer.valueOf(navProfile));
            replaceFragment(new ProfileFragment());
        }

        binding.bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == navHome) {
                Log.d("item Id Home", String.valueOf(R.id.navHome));
                replaceFragment(new HomeFragment());

            } else if (item.getItemId() == navMap) {
                Log.d("item Id Map", String.valueOf(R.id.navMap));
                replaceFragment(new MapsFragment());

            } else if (item.getItemId() == navFilter) {
                Log.d("item Id Filter", String.valueOf(R.id.navFilter));
                replaceFragment(new FilterFragment());

            } else if (item.getItemId() == navProfile) {
                Log.d("item Id Profile", String.valueOf(R.id.navProfile));
                replaceFragment(new ProfileFragment());
            }
            return true;
        });
    }

//    @Override
//    protected void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
//        // Save the initial fragment in case of configuration changes
//        outState.putString("initialFragment", intentDefaultFragment);
//    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }
}