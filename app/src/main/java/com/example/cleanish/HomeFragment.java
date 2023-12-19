package com.example.cleanish;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.cleanish.model.Location;
import com.example.cleanish.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

//    private TextView numberVolunteerTextView, numberLocationTextView;
//    private ListView volunteerListView, locationListView;
    private LinearLayout homeDescription, homeAdmin;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        homeDescription = view.findViewById(R.id.homeDescription);
        homeAdmin = view.findViewById(R.id.homeAdmin);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser userCurrent = auth.getCurrentUser();
        String uid = userCurrent.getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("Users").document(uid);

        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete (@NonNull Task < DocumentSnapshot > task) {
                    if (isAdded()){
                        if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        User user = document.toObject(User.class);
//                        String email = user.getEmail();
                        String role = user.getRole();

                        if (role.equals("Volunteer")) {
                            isAdmin(false);

                        } else {
                            isAdmin(true);
//                            Volunteer listview ---------------------------------------------------
//                            volunteerListView = view.findViewById(R.id.homeVolunteerListView);

                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                            TextView numberVolunteerTextView = view.findViewById(R.id.homeNumberVolunteers);
                            ListView volunteersList = view.findViewById(R.id.homeVolunteerListView);
                            CollectionReference usersRef = db.collection("Users");
                            Query queryUser = usersRef.orderBy("email");
                            List<User> usersList = new ArrayList<>();
                            List<String> emailsList = new ArrayList<>();

                            queryUser.get().addOnCompleteListener(taskA -> {

                                for (DocumentSnapshot doc : taskA.getResult()) {
                                    usersList.add(doc.toObject(User.class));
                                    emailsList.add(doc.toObject(User.class).getEmail());
                                    Log.d(TAG, "User : " + doc.toObject(User.class).getEmail());
                                }

                                Log.d(TAG, "User list: " + usersList.toString());
                                Log.d(TAG, "Email list: " + emailsList.toString());

                                ArrayAdapter adapter = new ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, emailsList);
                                volunteersList.setAdapter(adapter);

                                numberVolunteerTextView.setText(String.valueOf(emailsList.size()));
                            });

//                            Location listview ------------------------------------------------------
                            TextView numberLocationTextView = view.findViewById(R.id.homeNumberLocations);
                            ListView locationListView = view.findViewById(R.id.homeLocationListView);

                            CollectionReference locationRef = db.collection("Locations");
                            Query queryLocation = locationRef.orderBy("locationName");
                            List<Location> locationsList = new ArrayList<>();
                            List<String> locationNamesList = new ArrayList<>();

                            queryLocation.get().addOnCompleteListener(taskB -> {

                                for (DocumentSnapshot doc : taskB.getResult()) {
                                    locationsList.add(doc.toObject(Location.class));
                                    locationNamesList.add(doc.toObject(Location.class).getLocationName());
                                    Log.d(TAG, "User : " + doc.toObject(Location.class).getLocationName());
                                }

                                Log.d(TAG, "locationsList list: " + locationsList.toString());
                                Log.d(TAG, "locationNamesList list: " + locationNamesList.toString());

                                ArrayAdapter adapter = new ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, locationNamesList);
                                locationListView.setAdapter(adapter);

                                numberLocationTextView.setText(String.valueOf(locationNamesList.size()));
                            });


                        }
                    }
                }
            }
            }
        });

        return view;
    }

    private void isAdmin(boolean bool){
        if(bool){
            homeDescription.setVisibility(View.GONE);
            homeAdmin.setVisibility(View.VISIBLE);
        }else{
            homeDescription.setVisibility(View.VISIBLE);
            homeAdmin.setVisibility(View.GONE);
        }
    }
}