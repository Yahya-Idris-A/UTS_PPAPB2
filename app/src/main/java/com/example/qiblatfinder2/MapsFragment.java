package com.example.qiblatfinder2;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.Locale;

public class MapsFragment extends Fragment implements CompasSensor.OnSensorChangedListener {


    public static GoogleMap mMap;
    LocationAccess locationAccess;

    public static final LatLng mecca = new LatLng(21.422542, 39.826139);
    private Marker currentMarker;
    private Polyline polyline;
    private Location myLocation;

    private CompasSensor compasSensor;

    private Button resetCameraBtn;
    private boolean isCameraLocked = true;

    private final BroadcastReceiver permissionBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MainActivity.PERMISSION_GRANTED_ACTION)) {
                getMyLocation();
                requireActivity().unregisterReceiver(permissionBroadcastReceiver);
            }
        }
    };

    private final BroadcastReceiver updateLocationBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MainActivity.UPDATE_LOCATION_ACTION)) {
                getMyLocation();
                new Handler().postDelayed(() -> getMyLocation(), 100);
            }
        }
    };

    private final OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            googleMap.addMarker(new MarkerOptions()
                    .position(mecca)
                    .anchor(0.5f, 0.5f));

            mMap.setOnCameraMoveStartedListener(reason -> {
                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE && isCameraLocked) {
                    isCameraLocked = false;
                    resetCameraBtn.setVisibility(View.VISIBLE);
                }
            });
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        resetCameraBtn = view.findViewById(R.id.reset_button);
        resetCameraBtn.setOnClickListener(view1 -> {
            isCameraLocked = true;
            resetCameraBtn.setVisibility(View.INVISIBLE);
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        locationAccess = LocationAccess.getInstance(getContext());
        compasSensor = new CompasSensor((SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE), this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getMyLocation();
        requireActivity().registerReceiver(permissionBroadcastReceiver, new IntentFilter(MainActivity.PERMISSION_GRANTED_ACTION));
        requireActivity().registerReceiver(updateLocationBroadcastReceiver, new IntentFilter(MainActivity.UPDATE_LOCATION_ACTION));
        compasSensor.registerSensor();

        if (!isCameraLocked) {
            resetCameraBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        requireActivity().unregisterReceiver(permissionBroadcastReceiver);
        requireActivity().unregisterReceiver(updateLocationBroadcastReceiver);
        compasSensor.unregisterSensor();
    }

    private void getMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationAccess.getCurrentLocation(getContext(), new LocationAccess.LocationCallback() {
                @Override
                public void onLocationResult(Location location) {
                    myLocation = location;
                    if (currentMarker != null && polyline != null) {
                        currentMarker.remove();
                        polyline.remove();
                    }
                    addCurrentMarker();
                    MainActivity.hideSnackBar();
                }
                @Override
                public void onLocationError(String errorMessage) {
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void addCurrentMarker() {
        if (mMap != null && myLocation != null) {
            LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

            MarkerOptions newMarker = new MarkerOptions()
                    .position(latLng)
                    .anchor(0.5f, 0.5f);
            currentMarker = mMap.addMarker(newMarker);
            drawLine(latLng);
        }
    }

    private void drawLine(LatLng myLoc) {
        PolylineOptions polylineOptions = new PolylineOptions()
                .add(mecca, myLoc)
                .width(6)
                .color(getContext().getColor(R.color.purple_700));
        polyline = mMap.addPolyline(polylineOptions);
    }

    private BitmapDescriptor bitmapFromVector(int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(getContext(), vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onSensorChanged(float azimuth) {
        if (isCameraLocked) {
            if (mMap != null && myLocation != null) {
                CameraPosition currentCameraPosition = mMap.getCameraPosition();
                CameraPosition newCameraPosition = new CameraPosition.Builder(currentCameraPosition)
                        .target(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()))
                        .bearing(azimuth)
                        .zoom(mMap.getCameraPosition().zoom)
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition), 100, null);
            }
        }
    }
}