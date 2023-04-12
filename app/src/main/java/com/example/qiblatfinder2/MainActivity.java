package com.example.qiblatfinder2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    MapsFragment mapsFragment = new MapsFragment();
    CompassFragment compassFragment = new CompassFragment();
    InfoFragment infoFragment = new InfoFragment();
    CustomFragment customFragment = new CustomFragment();
    LanguageFragment languageFragment = new LanguageFragment();
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        enableMyLocation();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, mapsFragment).commit();
        bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.maps:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, mapsFragment).commit();
                        return true;
                    case R.id.compass:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, compassFragment).commit();
                        return true;
                    case R.id.info:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, infoFragment).commit();
                        return true;
                    case R.id.language:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, languageFragment).commit();
                        return true;
                }
                return false;
            }
        });
    }

}