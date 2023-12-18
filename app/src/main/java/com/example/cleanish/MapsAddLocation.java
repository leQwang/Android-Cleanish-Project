package com.example.cleanish;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
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
import java.util.Calendar;
import java.util.Date;

public class MapsAddLocation extends AppCompatActivity {

    EditText locationNameEditText, eventDateEditText, durationEditText, latitudeEditText, longitudeEditText;
    Button eventDatePickerButton,addlocationButton, backButton;
    String latitude, longitude;

    private Date dateAnd;
    private Calendar calendarBetween, calendarAnd;
    private DatePickerDialog datePickerDialogAnd;


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
        durationEditText = findViewById(R.id.addLocationDuration);
        latitudeEditText = findViewById(R.id.addLocationLatitude);
        longitudeEditText = findViewById(R.id.addLocationLongitude);

        latitudeEditText.setText(String.valueOf(latitude));
        longitudeEditText.setText(String.valueOf(longitude));


        eventDatePickerButton = findViewById(R.id.addLocationEventDatePicker);
        initDatePicker();
        eventDatePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialogAnd.show();
            }
        });

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

//                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                SimpleDateFormat formatterHour = new SimpleDateFormat("dd/MM/yyyy hh:mm");
//                String dateString = eventDateEditText.getText().toString();

                Date currentDateUsingDate = new Date();
                if (dateAnd.before(currentDateUsingDate)){
                    eventDateEditText.setError("Can not set date in the past");
                    isValid = false;
                }

//                Date eventDate = null;
//                try {
//                    eventDate = formatter.parse(dateString);
//
////                    Toast.makeText(MapsAddLocation.this, eventDate.toString(), Toast.LENGTH_SHORT).show();
////                    Log.d(TAG, "Date Set: " + (eventDate != null ? eventDate.toString() : "null"));
//                } catch (ParseException e) {
//                    eventDateEditText.setError("Incorrect Date format");
//                    e.printStackTrace();
//                    isValid = false;
//                }

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
                    Log.d(TAG, "Date And " + dateAnd);
                    location.setEventDate(dateAnd);
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
                                            Log.d(TAG, "Added to owner location owned detail");

                                            String formattedDateCreated = formatterHour.format(createdDate);
                                            String newNotification = "Location Created: " + locationName + " at " + formattedDateCreated;
                                            ownerRef.update("notifications", FieldValue.arrayUnion(newNotification))
                                                    .addOnSuccessListener(aVoidNew -> {
                                                        Toast.makeText(MapsAddLocation.this, "Location Successfully Created", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(MapsAddLocation.this, MainActivity.class);
                                                        intent.putExtra("fragment", "map");
                                                        startActivity(intent);
                                                        finish();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(MapsAddLocation.this, "Failed adding notification", Toast.LENGTH_SHORT).show();
                                                    });

                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(MapsAddLocation.this, "Failed adding to owner detail", Toast.LENGTH_SHORT).show();
                                        });
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

    public void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) { //month 0-11
                calendarAnd.set(year, month, day);
//                calendarAnd.set(year, month, day);
                dateAnd = calendarAnd.getTime();
                eventDatePickerButton.setText(makeDateString(day, month, year));
                Log.d(TAG, makeDateString(day, month, year));

            }
        };
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        calendarAnd = Calendar.getInstance();
        calendarAnd.set(year, month, day);
        dateAnd = calendarAnd.getTime();
        eventDatePickerButton.setText(makeDateString(day, month, year));

        int style = AlertDialog.THEME_HOLO_LIGHT;
        datePickerDialogAnd = new DatePickerDialog(MapsAddLocation.this, style, dateSetListener, year, month, day);
    }

    public String makeDateString(int day, int month, int year){
        return getMonthFormat(month) + " " + day + " " + year;
    }

    public String getMonthFormat(int month){
        if (month == 0)
            return "JAN";
        else if (month == 1)
            return "FEB";
        else if (month == 2)
            return "MAR";
        else if (month == 3)
            return "APR";
        else if (month == 4)
            return "MAY";
        else if (month == 5)
            return "JUN";
        else if (month == 6)
            return "JUL";
        else if (month == 7)
            return "AUG";
        else if (month == 8)
            return "SEP";
        else if (month == 9)
            return "OCT";
        else if (month == 10)
            return "NOV";
        else if (month == 11)
            return "DEC";
        return "";
    };
}