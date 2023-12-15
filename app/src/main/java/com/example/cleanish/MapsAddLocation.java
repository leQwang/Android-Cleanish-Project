package com.example.cleanish;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cleanish.model.Location;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MapsAddLocation extends AppCompatActivity {

    EditText locationNameEditText, eventDateEditText, durationEditText, latitudeEditText, longitudeEditText;
    Button addlocationButton, backButton;
    String latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_add_location);

        Intent intent = getIntent();

//       Initiate Location--------------------------------------------------
        Location location = new Location();
        latitude = intent.getStringExtra("latitude");
        longitude = intent.getStringExtra("longitude");

//       Fetch user id----------------------------------------------------
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        String uid = user.getUid();
        location.setLocationOwnerId(uid);

//       Set up EditText--------------------------------------------------------
        locationNameEditText = findViewById(R.id.addLocationName);
        eventDateEditText = findViewById(R.id.addLocationDate);
        durationEditText = findViewById(R.id.addLocationDuration);
        latitudeEditText = findViewById(R.id.addLocationLatitude);
        longitudeEditText = findViewById(R.id.addLocationLongitude);

        latitudeEditText.setText(String.valueOf(latitude));
        longitudeEditText.setText(String.valueOf(longitude));


//       Set Up Add location Button -----------------------------------------------------------------
        addlocationButton = findViewById(R.id.addLocationButton);
        backButton = findViewById(R.id.addLocationBackButton);

        addlocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isValid = true;

                String locationName = locationNameEditText.getText().toString().trim();
                if(locationName.isEmpty()){
                    locationNameEditText.setError("Location name required");
                    isValid = false;
                }

                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                String dateString = eventDateEditText.getText().toString();

                Date eventDate = null;
                try {
                    eventDate = formatter.parse(dateString);
                    Date currentDateUsingDate = new Date();
                    if (eventDate.before(currentDateUsingDate)){
                        eventDateEditText.setError("Can not set date in the past");
                        isValid = false;
                    }
//                    Toast.makeText(MapsAddLocation.this, eventDate.toString(), Toast.LENGTH_SHORT).show();
//                    Log.d(TAG, "Date Set: " + (eventDate != null ? eventDate.toString() : "null"));
                } catch (ParseException e) {
                    eventDateEditText.setError("Incorrect Date format");
                    e.printStackTrace();
                    isValid = false;
                }

                int duration = 0;
                try {
                    duration = Integer.parseInt(durationEditText.getText().toString().trim());
                } catch (NumberFormatException e) {
                    durationEditText.setError("Invalid value Duration");
                    isValid = false;
                }
                if (duration <= 0) {
                    durationEditText.setError("Duration must be > 0");
                    isValid = false;
                }

                latitude = latitudeEditText.getText().toString().trim();
                if(latitude.isEmpty()){
                    latitudeEditText.setError("Latitude required");
                    isValid = false;
                }


                longitude = longitudeEditText.getText().toString().trim();
                if(longitude.isEmpty()){
                    longitudeEditText.setError("Longitude required");
                    isValid = false;
                }

//                Toast.makeText(MapsAddLocation.this, "Added location: " + locationName + " " + eventDate + " " + duration + " " + latitude + " " + longitude, Toast.LENGTH_SHORT).show();

                if(isValid){
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    final DocumentReference locationRef = db.collection("Locations").document(); // Reference using UID

                    Date createdDate = new Date();
                    location.setLocationName(locationName);
                    location.setEventDate(eventDate);
                    location.setDuration(duration);
                    location.setLatitude(latitude);
                    location.setLongitude(longitude);
                    location.setDateCreated(createdDate);

                    locationRef.set(location)
                            .addOnSuccessListener(unused -> {
                                // Get the document ID generated by Firestore
                                String locationId = locationRef.getId();

                                // Update the owner's "locationsOwned" field with the new location ID
                                DocumentReference ownerRef = db.collection("Users").document(uid);
                                ownerRef.update("locationsOwned", FieldValue.arrayUnion(locationId))
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(MapsAddLocation.this, "Added to owner detail", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(MapsAddLocation.this, "Failed adding to owner detail", Toast.LENGTH_SHORT).show();
                                        });

                                Toast.makeText(MapsAddLocation.this, "Location Successfully Created", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MapsAddLocation.this, MainActivity.class);
                                intent.putExtra("fragment", "map");
                                startActivity(intent);
                            })
                            .addOnFailureListener(e -> {
                                Log.w(TAG, "Error adding location document", e);
                                Toast.makeText(MapsAddLocation.this, "Failed creating Location", Toast.LENGTH_SHORT).show();
                            });

                }
            }
        });


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsAddLocation.this, MainActivity.class);
                intent.putExtra("fragment", "map");
                startActivity(intent);
            }
        });
    }
}