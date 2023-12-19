package com.example.cleanish;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cleanish.model.User;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.reflect.TypeToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterVolunteerActivity extends AppCompatActivity {

    private Button registerVolunteerButton, registerVolunteerBackButton, removeVolunteerButton, finishedLocationButton, getPathLocationButton, saveUpdateLocationButton;
    private TextView messageTextView, locationOwnerTextView;
    private EditText locationNameTextView, durationTextView, eventDateTextView;

    private LinearLayout linearLayoutAmountTrash;
    private EditText amountOfTrashEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_volunteer);

        linearLayoutAmountTrash = findViewById(R.id.linearLayoutAmountTrash);

        amountOfTrashEditText = findViewById(R.id.amountTrashEditText);

        locationNameTextView = findViewById(R.id.registerVolunteerLocationName);
        locationOwnerTextView = findViewById(R.id.registerVolunteerLocationOwner);
        durationTextView = findViewById(R.id.registerVolunteerDuration);
        eventDateTextView = findViewById(R.id.registerVolunteerEventDate);
        messageTextView = findViewById(R.id.registerVolunteerMessage);

        Intent intent = getIntent();
        String locationId = intent.getStringExtra("locationID");

        String locationName = intent.getStringExtra("locationName"); //This is the owner id
        String locationOwner = intent.getStringExtra("locationOwner");
        String eventDate = intent.getStringExtra("eventDate");
        String duration = intent.getStringExtra("duration");

        String from = intent.getStringExtra("from");
        boolean isFinished = intent.getBooleanExtra("isFinished", false);

        Intent intentBack = new Intent(RegisterVolunteerActivity.this, MainActivity.class);
        intentBack.putExtra("fragment", from);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        String userId = user.getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference locationRef = db.collection("Locations").document(locationId);
        CollectionReference usersRef = db.collection("Users");
        DocumentReference volunteerRef = db.collection("Users").document(userId);


//        locationRef.get().addOnCompleteListener(task -> {
//            DocumentSnapshot doc = task.getResult();
//            isFinished = doc.getBoolean("isFinished");
//        });


        //Set TextView for Location Detail ---------------------------------------------------------------------------------
        DocumentReference ownerRef = db.collection("Users").document(locationOwner);
        ownerRef.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                String ownerEmail = doc.getString("email");
                String[] parts = ownerEmail.split("@");
                String username = parts[0];
                locationOwnerTextView.setText(username);
            } else {
                Toast.makeText(RegisterVolunteerActivity.this, "Error getting owner", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Error getting owner", task.getException());
            }
        });
        locationNameTextView.setText(locationName);
        eventDateTextView.setText(eventDate);
        durationTextView.setText(duration);


        //Button for Register -----------------------------------------------------------------------------------
        registerVolunteerButton = findViewById(R.id.registerVolunteerButton);
        removeVolunteerButton = findViewById(R.id.removeVolunteerButton);
        registerVolunteerBackButton = findViewById(R.id.registerVolunteerBackButton);
        finishedLocationButton = findViewById(R.id.finishedLocationButton);
        getPathLocationButton = findViewById(R.id.getPathVolunteerButton);

        isDisplayRemove(false,false, isFinished,false, "Register to become a volunteer");

        Date currentDate = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm");
        String currentDateTimeString = formatter.format(currentDate);

        volunteerRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        User user = document.toObject(User.class);
                        String role = user.getRole();
                        Log.d(TAG, "Got role: " + role);

                        if(isFinished){
                            isDisplayRemove(false, false, isFinished,false, "The Event has finished");
                        }else {
                            //        Check if the user is the owner of the location or an admin----------------------------------
                            if(userId.equals(locationOwner) || role.equals("Admin")){
                                isDisplayRemove(true,false, isFinished,false, "You are the owner of this location");

                                removeVolunteerButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
//                          Remove the location from the Owner location list
                                        ownerRef.update("locationsOwned", FieldValue.arrayRemove(locationId))
                                                .addOnSuccessListener(aVoid -> {
                                                    ownerRef.update("notifications", FieldValue.arrayUnion("Location Removed: " + locationName + " at " + currentDateTimeString))
                                                            .addOnSuccessListener(aVoidNew -> {
                                                                Log.d(TAG, "Removed location id Successfully from the user");
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Log.d(TAG, "Removed location id Failed from the user");
                                                            });
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e(TAG, "Removed location id Failed from the user");
                                                });
//                            Remove location from the collection
                                        locationRef.delete()
//                            Remove the location from the Volunteer Location list
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        usersRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                                                if(task.isSuccessful()) {
                                                                    for(DocumentSnapshot doc : task.getResult()) {

                                                                        // Get array for this user
                                                                        ArrayList locations = (ArrayList) doc.get("locationsVolunteered");

                                                                        // Remove locationId if exists
                                                                        if(locations != null && locations.contains(locationId)) {

                                                                            ArrayList updatedLocations = new ArrayList();
                                                                            updatedLocations.addAll(locations);
                                                                            updatedLocations.remove(locationId);

                                                                            Map<String, Object> updatesVolunteer = new HashMap<>();
                                                                            updatesVolunteer.put("locationsVolunteered", updatedLocations);
                                                                            updatesVolunteer.put("notifications", FieldValue.arrayUnion("Location Removed: " + locationName + " at " + currentDateTimeString));

                                                                            doc.getReference().update(updatesVolunteer);
                                                                        }
                                                                    }

                                                                }
                                                            }
                                                        });
                                                        Log.d(TAG, "Location deleted successfully!");
                                                        Toast.makeText(RegisterVolunteerActivity.this, "Remove Location Successfully", Toast.LENGTH_SHORT).show();
                                                        startActivity(intentBack);
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w(TAG, "Error deleting document", e);
                                                        Toast.makeText(RegisterVolunteerActivity.this, "Remove Location Failed", Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                    }
                                });
                            }

//                            Admin can also edit the location detail
                            if(role.equals("Admin")){
                                isDisplayRemove(true,true, isFinished,true, "You have already registered to this location");
                                saveUpdateLocationButton = findViewById(R.id.savedLocationButton);
                                saveUpdateLocationButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Log.d(TAG, "Save button pressed");
                                    }
                                });
                            }

//        Check if user have already registered to the location (anyone can register and unregister from a location)----------------------------
                            volunteerRef.get().addOnCompleteListener(taskNew -> {
                                if(task.isSuccessful()) {
                                    DocumentSnapshot doc = task.getResult();

                                    ArrayList volunteeredLocationList = (ArrayList) doc.get("locationsVolunteered");
                                    if(volunteeredLocationList != null && volunteeredLocationList.contains(locationId)) {
                                        isDisplayRemove(true,true, isFinished,false, "You have already registered to this location");


//                          Button for User Remove Registration ------------------------------------------
                                        removeVolunteerButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
//                          Remove user id from the location detail ------------------------------------------
                                                locationRef.update("volunteers", FieldValue.arrayRemove(userId))
                                                        .addOnSuccessListener(aVoid -> {
//                                        Toast.makeText(RegisterVolunteerActivity.this, "Removed Successfully to Location", Toast.LENGTH_SHORT).show();
                                                            Log.d(TAG, "Removed Successfully from the location detail");

                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(RegisterVolunteerActivity.this, "Register Failed to Location", Toast.LENGTH_SHORT).show();
                                                        });
//                          Remove the location from the current User detail (volunteer location list)
                                                volunteerRef.update("locationsVolunteered", FieldValue.arrayRemove(locationId))
                                                        .addOnSuccessListener(aVoid -> {
                                                            Log.d(TAG, "Removed Successfully from the user");
                                                            Toast.makeText(RegisterVolunteerActivity.this, "Unregistered successfully", Toast.LENGTH_SHORT).show();
                                                            volunteerRef.update("notifications", FieldValue.arrayUnion("Location Unregistered: " + locationName + " at " + currentDateTimeString))
                                                                    .addOnSuccessListener(aVoidNew -> {
                                                                        Log.d(TAG, "Notification updated Unregistered successfully");
                                                                    })
                                                                    .addOnFailureListener(e -> {
                                                                        Log.d(TAG, "Notification updated Unregistered successfully");
                                                                    });

                                                            startActivity(intentBack);
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(RegisterVolunteerActivity.this, "Unregistered Failed to current User", Toast.LENGTH_SHORT).show();
                                                        });
                                            }
                                        });
                                    }
                                } else {
                                    Toast.makeText(RegisterVolunteerActivity.this, "Error fetching user information", Toast.LENGTH_SHORT).show();
                                }
                            });

                            registerVolunteerButton.setOnClickListener(view -> {
//                      Update the location detail ------------------------------------------
                                locationRef.update("volunteers", FieldValue.arrayUnion(userId))
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG,"Register Successfully to Location");
//                                            Toast.makeText(RegisterVolunteerActivity.this, "Register Successfully to Location", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.d(TAG,"Register Failed to Location");
//                                            Toast.makeText(RegisterVolunteerActivity.this, "Register Failed to Location", Toast.LENGTH_SHORT).show();
                                        });
//                      Update the current User detail (volunteer location list) ------------------------------------------
                                volunteerRef.update("locationsVolunteered", FieldValue.arrayUnion(locationId))
                                        .addOnSuccessListener(aVoid -> {
                                            volunteerRef.update("notifications", FieldValue.arrayUnion("Location Registered: " + locationName + " at " + currentDateTimeString))
                                                    .addOnSuccessListener(aVoidNew -> {
                                                        Log.d(TAG, "Notification updated Registered successfully");
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.d(TAG, "Notification updated Registered successfully");
                                                    });
                                            Toast.makeText(RegisterVolunteerActivity.this, "Register Successfully", Toast.LENGTH_SHORT).show();
                                            startActivity(intentBack);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(RegisterVolunteerActivity.this, "Register Failed", Toast.LENGTH_SHORT).show();
                                        });
                            });


//            Finished Button -------------------------------------------------------------------
                            finishedLocationButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    boolean isValid = true;
                                    int amountTrashCollected = 0;
                                    try {
                                        amountTrashCollected = Integer.parseInt(amountOfTrashEditText.getText().toString().trim());
                                    } catch (NumberFormatException e) {
                                        amountOfTrashEditText.setError("Invalid value Duration");
                                        isValid = false;
                                    }
                                    if (amountTrashCollected < 0) {
                                        amountOfTrashEditText.setError("Duration must be > 0");
                                        isValid = false;
                                    }

                                    if(isValid){
                                        Map<String, Object> updates = new HashMap<>();
                                        updates.put("amountTrashCollected", amountTrashCollected);
                                        updates.put("isFinished", true);

                                        int finalAmountTrashCollected = amountTrashCollected;
                                        locationRef.update(updates)
                                                .addOnSuccessListener(aVoid -> {
                                                    ownerRef.update("notifications", FieldValue.arrayUnion("Event Ended: " + locationName  + " at " + currentDateTimeString +  ", collected " + finalAmountTrashCollected + "kg"))
                                                            .addOnSuccessListener(aVoidNew -> {
                                                                Log.d(TAG, "Event Ended notification Owner Successfully");
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Log.e(TAG, "Event Ended notification Owner Failed");
                                                            });


                                                    usersRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                                            if(task.isSuccessful()) {
                                                                for(DocumentSnapshot doc : task.getResult()) {
                                                                    // Get array for this user
                                                                    ArrayList locations = (ArrayList) doc.get("locationsVolunteered");

                                                                    // Remove locationId if exists
                                                                    if(locations != null && locations.contains(locationId)) {

                                                                        Map<String, Object> updatesVolunteer = new HashMap<>();
                                                                        updatesVolunteer.put("notifications", FieldValue.arrayUnion("Event Ended: " + locationName  + " at " + currentDateTimeString +  ", collected " + finalAmountTrashCollected + "kg"));

                                                                        doc.getReference().update(updatesVolunteer);
                                                                    }

                                                                }

                                                            }
                                                        }
                                                    });


                                                    Toast.makeText(RegisterVolunteerActivity.this, "Update finished successfully", Toast.LENGTH_SHORT).show();
                                                    Log.d(TAG, "Update successful!");
                                                    startActivity(intentBack);
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(RegisterVolunteerActivity.this, "Update finished failed", Toast.LENGTH_SHORT).show();
                                                    Log.e(TAG, "Update failed", e);
                                                });
                                    }
                                }
                            });

                            getPathLocationButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    locationRef.get().addOnSuccessListener(documentSnapshot -> {
                                        String lat = documentSnapshot.getString("latitude");
                                        String lng = documentSnapshot.getString("longitude");

//                        double latitude = 0.0;
//                        double longitude = 0.0;
//
//                        try {
//                            latitude = Double.parseDouble(lat);
//                            longitude = Double.parseDouble(lng);
//                        } catch (NumberFormatException e) {
//                            Log.e(TAG, "Invalid Parsing to Double");
//                            e.printStackTrace();
//                        }

                                        Intent intentMap = new Intent(RegisterVolunteerActivity.this, MainActivity.class);
                                        intentMap.putExtra("fragment", "map");
                                        intentMap.putExtra("purpose", "getPath");
                                        intentMap.putExtra("latitude", lat);
                                        intentMap.putExtra("longitude", lng);
                                        startActivity(intentMap);

                                    });
                                }
                            });

                        }
                    }
                }
            }
        });



        //        Back Button ------------------------------------------------
            registerVolunteerBackButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(intentBack);
                }
            });
        }





//    Support function display text
    private void isDisplayRemove(boolean bool, boolean isVolunteer, boolean isFinished, boolean isAdmin, String text){ //true to display text notifying the owner and registered user
        if(isAdmin){

        }

        if(!isFinished){
            if(bool){
                if(isVolunteer){
                    removeVolunteerButton.setText("Unregister");
                    finishedLocationButton.setVisibility(View.GONE);
                    linearLayoutAmountTrash.setVisibility(View.GONE);
                }else{
                    removeVolunteerButton.setText("Remove");
                    linearLayoutAmountTrash.setVisibility(View.VISIBLE);
                    finishedLocationButton.setVisibility(View.VISIBLE);
                }
//            messageTextView.setVisibility(View.VISIBLE);
                messageTextView.setText(text);
                removeVolunteerButton.setVisibility(View.VISIBLE);
                registerVolunteerButton.setVisibility(View.GONE);
            }else{
//            messageTextView.setVisibility     (View.INVISIBLE);
                linearLayoutAmountTrash.setVisibility(View.GONE);
                finishedLocationButton.setVisibility(View.GONE);
                messageTextView.setText(text);
                removeVolunteerButton.setVisibility(View.GONE);
                registerVolunteerButton.setVisibility(View.VISIBLE);
            }
        }else{
            getPathLocationButton.setVisibility(View.GONE);
            linearLayoutAmountTrash.setVisibility(View.GONE);
            finishedLocationButton.setVisibility(View.GONE);
            messageTextView.setText(text);
            removeVolunteerButton.setVisibility(View.GONE);
            registerVolunteerButton.setVisibility(View.GONE);
        }

    }
}