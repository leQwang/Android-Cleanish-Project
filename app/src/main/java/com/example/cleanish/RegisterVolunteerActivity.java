package com.example.cleanish;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.reflect.TypeToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class RegisterVolunteerActivity extends AppCompatActivity {

    Button registerVolunteerButton, registerVolunteerBackButton;
    TextView locationNameTextView, locationOwnerTextView, durationTextView, eventDateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_volunteer);

        locationNameTextView = findViewById(R.id.registerVolunteerLocationName);
        locationOwnerTextView = findViewById(R.id.registerVolunteerLocationOwner);
        durationTextView = findViewById(R.id.registerVolunteerDuration);
        eventDateTextView = findViewById(R.id.registerVolunteerEventDate);

        Intent intentBack = new Intent(RegisterVolunteerActivity.this, MainActivity.class);
        intentBack.putExtra("fragment", "map");

        Intent intent = getIntent();
        String locationId = intent.getStringExtra("locationID");
        String locationName = intent.getStringExtra("locationName"); //This is the owner id
        String locationOwner = intent.getStringExtra("locationOwner");
        String eventDate = intent.getStringExtra("eventDate");
        String duration = intent.getStringExtra("duration");

//        Toast.makeText(RegisterVolunteerActivity.this, "Location ID: " + locationId, Toast.LENGTH_SHORT).show();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ownerRef = db.collection("Users").document(locationOwner);

        //TextView ---------------------------------------------------------------------------------
        ownerRef.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                String ownerEmail = doc.getString("email");
                String[] parts = ownerEmail.split("@");
                String username = parts[0];
                locationOwnerTextView.setText(username);
            } else {
                Log.d(TAG, "Error getting owner:", task.getException());
            }
        });
        locationNameTextView.setText(locationName);
        eventDateTextView.setText(eventDate);
        durationTextView.setText(duration + " day");


        //Button -----------------------------------------------------------------------------------
        registerVolunteerButton = findViewById(R.id.registerVolunteerButton);
        registerVolunteerBackButton = findViewById(R.id.registerVolunteerBackButton);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        String userId = user.getUid();
        DocumentReference volunteerRef = db.collection("Users").document(userId);

//        Toast.makeText(RegisterVolunteerActivity.this, "UserId: " + userId + ", OwnerId: " + locationOwner, Toast.LENGTH_SHORT).show();
        //Check if the user is the owner of the location
        if(userId.equals(locationOwner)){
            Toast.makeText(RegisterVolunteerActivity.this, "You are the owner of this location", Toast.LENGTH_SHORT).show();
            registerVolunteerButton.setVisibility(View.INVISIBLE);
        }

        //Check if user have already registered to the location
        volunteerRef.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();

                ArrayList volunteeredLocationList = (ArrayList) doc.get("locationsVolunteered");
                if(volunteeredLocationList != null && volunteeredLocationList.contains(locationId)) {
                    registerVolunteerButton.setVisibility(View.INVISIBLE);
                    Toast.makeText(RegisterVolunteerActivity.this, "You have already registered to this location", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(RegisterVolunteerActivity.this, "Error fetching user information", Toast.LENGTH_SHORT).show();
            }
        });

        registerVolunteerButton.setOnClickListener(view -> {

            DocumentReference locationRef = db.collection("Locations").document(locationId);
            locationRef.update("volunteers", FieldValue.arrayUnion(userId))
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(RegisterVolunteerActivity.this, "Register Successfully to Location", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(RegisterVolunteerActivity.this, "Register Failed to Location", Toast.LENGTH_SHORT).show();
                    });

            volunteerRef.update("locationsVolunteered", FieldValue.arrayUnion(locationId))
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(RegisterVolunteerActivity.this, "Register Successfully to current User", Toast.LENGTH_SHORT).show();
                        startActivity(intentBack);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(RegisterVolunteerActivity.this, "Register Failed to current User", Toast.LENGTH_SHORT).show();
                    });

        });


        registerVolunteerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(intentBack);
            }
        });
    }
}