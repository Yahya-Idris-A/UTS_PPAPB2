package com.example.qiblatfinder2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class MainActivity extends AppCompatActivity {
    public static final String PERMISSION_GRANTED_ACTION = "com.example.qiblatfinder2.PERMISSION_GRANTED";
    public static final String UPDATE_LOCATION_ACTION = "com.example.qiblatfinder2.UPDATE_LOCATION";
    private final String MAPS_FRAGMENT = "TAG_MAPS";
    private final String COMPASS_FRAGMENT = "TAG_COMPASS";
    private final String INFO_FRAGMENT = "TAG_INFO";

    BottomNavigationView navigation;
    static Snackbar snackBar;
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

        enableLocation();

    }
    private void enableLocation() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }else{
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    showSnackbar();
                    Intent intent = new Intent(PERMISSION_GRANTED_ACTION);
                    sendBroadcast(intent);
                }
            } else {
                showAlertDialog();
            }
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.permission_alert)
                .setMessage(R.string.permission_alert_message)
                .setPositiveButton(R.string.permission_alert_close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Close the app
                        moveTaskToBack(true);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                    }
                })
                .setCancelable(false)
                .show();
    }
    public static void hideSnackBar() {
        if (snackBar != null) {
            snackBar.dismiss();
            snackBar = null;
        }
    }

    private void showSnackbar() {
        snackBar = Snackbar.make(findViewById(R.id.mainActivity), R.string.snackbar_getting_loc, Snackbar.LENGTH_INDEFINITE);
        snackBar.setAnchorView(navigation);
        snackBar.show();
    }

}