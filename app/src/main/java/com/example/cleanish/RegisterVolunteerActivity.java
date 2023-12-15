package com.example.cleanish;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.List;

public class RegisterVolunteerActivity extends AppCompatActivity {

    private Button registerVolunteerButton, registerVolunteerBackButton, removeVolunteerButton, finishedLocationButton;
    private TextView locationNameTextView, locationOwnerTextView, durationTextView, eventDateTextView, messageTextView;
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

        Toast.makeText(RegisterVolunteerActivity.this, String.valueOf(isFinished), Toast.LENGTH_SHORT).show();


        Intent intentBack = new Intent(RegisterVolunteerActivity.this, MainActivity.class);
        intentBack.putExtra("fragment", from);


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference locationRef = db.collection("Locations").document(locationId);


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
        durationTextView.setText(duration + " day");


        //Button for Register -----------------------------------------------------------------------------------
        registerVolunteerButton = findViewById(R.id.registerVolunteerButton);
        removeVolunteerButton = findViewById(R.id.removeVolunteerButton);
        registerVolunteerBackButton = findViewById(R.id.registerVolunteerBackButton);
        finishedLocationButton = findViewById(R.id.finishedLocationButton);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        String userId = user.getUid();
        DocumentReference volunteerRef = db.collection("Users").document(userId);
        isDisplayRemove(false,false, isFinished, "Register to become a volunteer");

        if(isFinished){
            isDisplayRemove(false, false, isFinished, "The Event has finished");
        }else {
            //        Check if the user is the owner of the location----------------------------------
            if(userId.equals(locationOwner)){
                isDisplayRemove(true,false, isFinished, "You are the owner of this location");

                removeVolunteerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                          Remove the location from the Owner location list
                        ownerRef.update("locationsOwned", FieldValue.arrayRemove(locationId))
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Removed location id Successfully from the user");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Removed location id Failed from the user");
                                });
//                          Remove location from the collection
                        locationRef.delete()
//                            Remove the location from the Volunteer Location list
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        CollectionReference usersRef = db.collection("Users");

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

                                                            // Update array
                                                            doc.getReference().update("locationsVolunteered", updatedLocations);

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

//        Check if user have already registered to the location----------------------------
            volunteerRef.get().addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();

                    ArrayList volunteeredLocationList = (ArrayList) doc.get("locationsVolunteered");
                    if(volunteeredLocationList != null && volunteeredLocationList.contains(locationId)) {
                        isDisplayRemove(true,true, isFinished, "You have already registered to this location");
//                    Button for User Remove Registration ------------------------------------------
                        removeVolunteerButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
//                          Remove user id from the location detail
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
                                            Toast.makeText(RegisterVolunteerActivity.this, "Unregistered successfully from current User", Toast.LENGTH_SHORT).show();
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
//          Update the location detail
                locationRef.update("volunteers", FieldValue.arrayUnion(userId))
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG,"Register Successfully to Location");
//                        Toast.makeText(RegisterVolunteerActivity.this, "Register Successfully to Location", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Log.d(TAG,"Register Failed to Location");
//                        Toast.makeText(RegisterVolunteerActivity.this, "Register Failed to Location", Toast.LENGTH_SHORT).show();
                        });
//          Update the current User detail (volunteer location list)
                volunteerRef.update("locationsVolunteered", FieldValue.arrayUnion(locationId))
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(RegisterVolunteerActivity.this, "Register Successfully", Toast.LENGTH_SHORT).show();
                            startActivity(intentBack);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(RegisterVolunteerActivity.this, "Register Failed", Toast.LENGTH_SHORT).show();
                        });
            });


//            Finished Button
//            finishedLocationButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if(amountOfTrashEditText.getNu)
//                }
//            });



        }

        //        Back Button ------------------------------------------------
            registerVolunteerBackButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(intentBack);
                }
            });
        }





//    Support function display text
    private void isDisplayRemove(boolean bool, boolean isVolunteer, boolean isFinished, String text){ //true to display text notifying the owner and registered user
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
            linearLayoutAmountTrash.setVisibility(View.GONE);
            finishedLocationButton.setVisibility(View.GONE);
            messageTextView.setText(text);
            removeVolunteerButton.setVisibility(View.GONE);
            registerVolunteerButton.setVisibility(View.GONE);
        }

    }
}