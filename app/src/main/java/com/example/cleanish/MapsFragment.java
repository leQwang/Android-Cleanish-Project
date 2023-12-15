package com.example.cleanish;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.cleanish.databinding.ActivityMainBinding;
import com.example.cleanish.model.Location;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.maps.android.SphericalUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MapsFragment extends Fragment {
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final long UPDATE_INTERVAL = 20*1000 ;
    private static final long FASTEST_INTERVAL = 10*1000 ;
    protected FusedLocationProviderClient client;
    protected LocationRequest mLocationRequest;
    private GoogleMap mMap;
    private LatLng currentUserLocation;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {


        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
//            LatLng sydney = new LatLng(-34, 151);
//            googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//            googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
            requestPermission();
            client = LocationServices.getFusedLocationProviderClient(getActivity());
            Log.d(TAG, "Current client: " + client);
            mMap = googleMap;

            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){

                @Override
                public void onMapClick(LatLng latLng) {
                    Intent intent = new Intent(requireContext(), MapsAddLocation.class);

                    intent.putExtra("latitude", String.valueOf(latLng.latitude));
                    intent.putExtra("longitude", String.valueOf(latLng.longitude));
                    startActivity(intent);
                }
            });
            startLocationUpdate();


            new LoadLocationsTask().execute();
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        new LoadLocationsTask().execute();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
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

    private void requestPermission(){
        Log.d(TAG, "Requesting Permission");
        ActivityCompat.requestPermissions(getActivity(), new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
    }

    @SuppressLint({"MissingPermission", "RestrictedApi"})
    private void startLocationUpdate(){
        Log.d(TAG, "Start Location Update");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        client.requestLocationUpdates(mLocationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult){
                super.onLocationResult(locationResult);
                android.location.Location location = locationResult.getLastLocation();
                currentUserLocation = new LatLng(location.getLatitude(), location.getLongitude());
                Marker marker = null;

                marker = mMap.addMarker(new MarkerOptions().position(currentUserLocation).title("Marker in Current Location"));

                if(marker != null) {
                    marker.setTag(0);  // Use the index i as a tag to identify the marker
                }
                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentUserLocation));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUserLocation, 12));

                // Add a circle with a radius of 500 kilometers

                mMap.setMyLocationEnabled(true);

                //LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//                Toast.makeText(getActivity(), "(" + location.getLatitude() + ","
//                        + location.getLongitude() +")", Toast.LENGTH_SHORT).show();
            }
        }, null);
        Log.d(TAG, "After request Location Updates");

    }

    private class LoadLocationsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            CollectionReference collectionRef = FirebaseFirestore.getInstance().collection("Locations");

            collectionRef.get()
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            List<Location> locations = task.getResult().toObjects(Location.class);

                            Log.d(TAG, "Location size begin" + locations.size());


                            for (int i = 1; i < locations.size()+1; i++) {
                                Log.d(TAG, "index for " + i);
                                Log.d(TAG, "Location size for" + locations.size());
                                Location loc = locations.get(i-1);

//                                Toast.makeText(getContext(), loc.getLocationName(), Toast.LENGTH_SHORT).show();

                                double lat = Double.parseDouble(loc.getLatitude());
                                double lng = Double.parseDouble(loc.getLongitude());

                                LatLng position = new LatLng(lat, lng);

                                // Calculate distance
//                                double distance = SphericalUtil.computeDistanceBetween(
//                                        currentUserLocation, position);

//                                Separate into three cases

                                Marker marker = null;

//                                1. The admin can view all the location


//                                2. The volunteer can only view the location in the current 500 radius area
//                                if(distance < 500000) {
                                    // show marker within 500km
                                    marker = mMap.addMarker(new MarkerOptions()
                                            .position(position)
                                            .icon(bitmapDescriptorFromVector(getContext(), R.drawable.baseline_volunteer_activism_24))
                                            .snippet(loc.getLocationName())
                                            .title(loc.getLocationName()));
//                                }

//                                3. The Owner can view all the location belongs to the owner

                                // Add click listener to the marker
                                if(marker != null) {
                                    marker.setTag(i);  // Use the index i as a tag to identify the marker
                                }
                            }

                            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                @Override
                                public boolean onMarkerClick(Marker marker) {
                                    int index = (int) marker.getTag();
                                    if(index == 0 ) {
                                        return true;
                                    }
                                    DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(index-1);
                                    String locationId = documentSnapshot.getId();

                                    Log.d(TAG, "index  " + index);
                                    Log.d(TAG, "Location size " + locations.size());

                                    Location clickedLoc = locations.get(index-1);

                                    // Handle the click event, for example, start a new activity
                                    if (index != -1) {

                                            // Start a new activity with information from the clicked location (loc)
                                            Intent intentNew = new Intent(getContext(), RegisterVolunteerActivity.class);
                                            intentNew.putExtra("locationID", locationId);
                                            Log.d(TAG, "Location ID " + locationId);
                                            intentNew.putExtra("locationName", clickedLoc.getLocationName());
                                            intentNew.putExtra("locationOwner", clickedLoc.getLocationOwnerId());
                                            intentNew.putExtra("duration", String.valueOf(clickedLoc.getDuration()));
                                            intentNew.putExtra("isFinished", clickedLoc.getIsFinished());
                                            Log.d(TAG, "is finished " + String.valueOf(clickedLoc.getIsFinished()));

                                            Date eventDate = clickedLoc.getEventDate(); // Assuming loc.getEventDate() returns a Date object
                                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                            String formattedEventDate = dateFormat.format(eventDate);
                                            intentNew.putExtra("eventDate", formattedEventDate);

                                            intentNew.putExtra("from", "map");

                                            startActivity(intentNew);

                                        return true;
                                    }

                                    return false;
                                }
                            });

                        }
                    });

            return null;
        }

        private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
            Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
            vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(),
                    vectorDrawable.getIntrinsicHeight());
            Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth()
                    , vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.draw(canvas);
            return BitmapDescriptorFactory.fromBitmap(bitmap);
        }

    }
}