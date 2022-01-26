package tn.rabini.petadoption;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.progressindicator.CircularProgressIndicator;

public class MapFragment extends DialogFragment {

    private final boolean edit;
    private GoogleMap map;
    private double lat, lng, cLat = 0, cLng = 0;
    private SupportMapFragment mapFragment;
    private MarkerOptions markerOptions;
    private LocationManager locationManager;
    private Button currentLocationButton;
    private Marker marker;
    private CircularProgressIndicator spinner;
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                mapFragment.getMapAsync(googleMap -> {
                    lat = location.getLatitude();
                    lng = location.getLongitude();
                    cLat = lat;
                    cLng = lng;
                    locationManager.removeUpdates(locationListener);
                    map = googleMap;
                    if (marker != null) {
                        marker.remove();
                    }
                    LatLng myLocation = new LatLng(lat, lng);
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
                    markerOptions = new MarkerOptions().position(myLocation).draggable(true);
                    marker = map.addMarker(markerOptions);
                    dragMarkerListener();
                    spinner.setVisibility(View.GONE);
                    currentLocationButton.setVisibility(View.VISIBLE);
                });
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    public MapFragment(boolean edit, double lat, double lng) {
        this.edit = edit;
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        spinner = v.findViewById(R.id.spinner);
        Button okButton = v.findViewById(R.id.okButton);
        Button cancelButton = v.findViewById(R.id.cancelButton);
        currentLocationButton = v.findViewById(R.id.currentButton);
        currentLocationButton.setOnClickListener(view -> {
            if (ActivityCompat.checkSelfPermission(
                    requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                getCurrentLocation();
            } else {
                AlertDialog alertDialog = new AlertDialog.Builder(requireContext()).create();
                alertDialog.setTitle("No GPS...");
                alertDialog.setMessage("Please make sure GPS is enabled and permission is granted.");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        (dialog, which) -> dialog.dismiss());
                alertDialog.show();
            }
        });
        okButton.setOnClickListener(view -> {
            this.dismiss();
            setValues();
        });
        cancelButton.setOnClickListener(view -> {
            this.dismiss();
            setValues();
        });

        mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (edit) {
            //            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
            assert mapFragment != null;
            mapFragment.getMapAsync(googleMap -> {
                map = googleMap;
                LatLng Tunisia = new LatLng(lat, lng);
                markerOptions = new MarkerOptions().position(Tunisia).draggable(true);
                marker = map.addMarker(markerOptions);
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(Tunisia, 15));
                dragMarkerListener();
                spinner.setVisibility(View.GONE);
                currentLocationButton.setVisibility(View.VISIBLE);
            });
        } else {
            if (ActivityCompat.checkSelfPermission(
                    requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                getCurrentLocation();
            } else {
                mapFragment.getMapAsync(googleMap -> {
                    map = googleMap;
                    LatLng Tunisia = new LatLng(lat, lng);
                    markerOptions = new MarkerOptions().position(Tunisia).draggable(true);
                    marker = map.addMarker(markerOptions);
                    map.animateCamera(CameraUpdateFactory.newLatLng(Tunisia));
                    dragMarkerListener();
                    spinner.setVisibility(View.GONE);
                    currentLocationButton.setVisibility(View.VISIBLE);
                });
            }
        }

        return v;
    }

    private void dragMarkerListener() {
        map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng latLng = marker.getPosition();
                lat = latLng.latitude;
                lng = latLng.longitude;
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                markerOptions.position(latLng);
            }
        });
    }

    private void getCurrentLocation() {
        if (cLat == 0) {
            if (ActivityCompat.checkSelfPermission(
                    requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                spinner.setVisibility(View.VISIBLE);
                currentLocationButton.setVisibility(View.GONE);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            }
        } else {
            mapFragment.getMapAsync(googleMap -> {
                if (marker != null) {
                    marker.remove();
                }
                LatLng myLocation = new LatLng(cLat, cLng);
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
                markerOptions = new MarkerOptions().position(myLocation).draggable(true);
                marker = map.addMarker(markerOptions);
                dragMarkerListener();
            });
        }

    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public void setValues() {
        Bundle result = new Bundle();
        result.putString("latValue", String.valueOf(lat));
        result.putString("lngValue", String.valueOf(lng));
        getParentFragmentManager().setFragmentResult("MAPPED", result);
    }
}