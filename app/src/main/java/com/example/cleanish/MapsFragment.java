package com.example.cleanish;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapsFragment extends Fragment {
    private GoogleMap mMap;

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
            mMap = googleMap;

            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){

                @Override
                public void onMapClick(LatLng latLng) {
                    Intent intent = new Intent(getContext(), MapsAddLocation.class);

                    intent.putExtra("latitude", String.valueOf(latLng.latitude));
                    intent.putExtra("longitude", String.valueOf(latLng.longitude));
                    startActivity(intent);
                }
            });
            new LoadLocationsTask().execute();
        }
    };

//    @Override
//    protected void onResume(){
//        super.onResume();
//        new LoadLocationsTask().execute();
//    }

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

    private class LoadLocationsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            CollectionReference collectionRef = FirebaseFirestore.getInstance().collection("Locations");

            collectionRef.get()
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            List<Location> locations = task.getResult().toObjects(Location.class);


                            for (int i = 0; i < locations.size(); i++) {
                                Location loc = locations.get(i);

//                                Toast.makeText(getContext(), loc.getLocationName(), Toast.LENGTH_SHORT).show();

                                double lat = Double.parseDouble(loc.getLatitude());
                                double lng = Double.parseDouble(loc.getLongitude());

                                LatLng position = new LatLng(lat, lng);
                                Marker marker = mMap.addMarker(new MarkerOptions()
                                        .position(position)
                                        .icon(bitmapDescriptorFromVector(getContext(), R.drawable.baseline_volunteer_activism_24))
                                        .snippet(loc.getLocationName())
                                        .title(loc.getLocationName()));

                                // Check if it's the last location in the list
                                if (i == 0) {
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
                                }

                                // Add click listener to the marker
                                marker.setTag(i);  // Use the index i as a tag to identify the marker

                                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                    @Override
                                    public boolean onMarkerClick(Marker marker) {
                                        int index = (int) marker.getTag();
                                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(index);
                                        String locationId = documentSnapshot.getId();

                                        Location clickedLoc = locations.get(index);

                                        // Handle the click event, for example, start a new activity
                                        if (index != -1) {
                                            // Start a new activity with information from the clicked location (loc)
                                            Intent intentNew = new Intent(getContext(), RegisterVolunteerActivity.class);
                                            intentNew.putExtra("locationID", locationId);
                                            intentNew.putExtra("locationName", clickedLoc.getLocationName());
                                            intentNew.putExtra("locationOwner", clickedLoc.getLocationOwnerId());
                                            intentNew.putExtra("duration", String.valueOf(clickedLoc.getDuration()));

                                            Date eventDate = clickedLoc.getEventDate(); // Assuming loc.getEventDate() returns a Date object
                                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                            String formattedEventDate = dateFormat.format(eventDate);
                                            intentNew.putExtra("eventDate", formattedEventDate);

                                            startActivity(intentNew);
                                            return true;
                                        }

                                        return false;
                                    }
                                });
                            }
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