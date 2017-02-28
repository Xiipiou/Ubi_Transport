package com.dumont.alexis.ubitransport;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivityFragment extends Fragment {
    public static final String TAG = MainActivityFragment.class.getName();
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;

    private LocationManager locationManager;
    private View rootView;
    private TextView textViewSpeed;
    private ImageView imageViewIcon;
    private ArrayList speeds = new ArrayList();
    private boolean hasMove = false;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindView();
    }

    @Override
    public void onStart() {
        super.onStart();
        requestLocationPermission();
    }

    private void bindView() {
        textViewSpeed = (TextView) rootView.findViewById(R.id.tv_speed);
        imageViewIcon = (ImageView) rootView.findViewById(R.id.im_speedometre);
    }

    private void initLocationManager() {
        locationManager = (LocationManager) getActivity().getSystemService(getContext().LOCATION_SERVICE);
        updateView(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if(!hasMove){
                        hasMove = true;
                    }
                    updateView(location);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) throws SecurityException {
                    updateView(locationManager.getLastKnownLocation(provider));
                }

                @Override
                public void onProviderDisabled(String provider) throws  SecurityException {
                    updateView(locationManager.getLastKnownLocation(null));
                }
            });
            return;
        }
    }

    private void updateView(Location lastLocation){
        if(isAdded() && hasMove){
            if(lastLocation != null && lastLocation.getSpeed() != 0.0){
                imageViewIcon.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.speedometer));
                double speed = lastLocation.getSpeed()*3.6;
                speeds.add(speed);
                textViewSpeed.setText(String.format(getString(R.string.actual_speed) , speed));
            }else{
                if(!speeds.isEmpty()){
                    imageViewIcon.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.distance));
                    double averageSpeed = getAverageSpeed();
                    textViewSpeed.setText(String.format(getString(R.string.average_speed) , averageSpeed));
                }
            }
        }
    }

    private double getAverageSpeed(){
        double totalSpeed = 0;
        for(int i=0;i<speeds.size();i++){
            totalSpeed += (double)speeds.get(i);
        }
        double averageSpeed = totalSpeed / speeds.size();
        speeds.clear();
        return averageSpeed;
    }

    private void requestLocationPermission(){
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                showExplication();
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }else{
            initLocationManager();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initLocationManager();
                } else {
                    showExplication();
                }
            }

        }
    }

    private void showExplication(){
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.location_dialog_title)
                .setMessage(R.string.location_dialog_information)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_REQUEST_LOCATION);
                    }
                })
                .setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                }).create().show();
    }
}