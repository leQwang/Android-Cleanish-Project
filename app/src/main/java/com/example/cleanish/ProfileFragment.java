package com.example.cleanish;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        Button logoutButton = view.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(requireContext(), LoginActivity.class);
                startActivity(intent);
                requireActivity().finish();
            }
        });

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        String uid = user.getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("Users").document(uid);
//        CollectionReference locationRef = db.collection("Locations");

        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        User user = document.toObject(User.class);
                        String email = user.getEmail();
                        String role = user.getRole();
                        TextView profileFragmentEmail = view.findViewById(R.id.profileFragmentEmail);
                        TextView profileFragmentRole = view.findViewById(R.id.profileFragmentRole);

                        profileFragmentEmail.setText(email);
                        profileFragmentRole.setText(role);

                        ListView locationOwnedList = view.findViewById(R.id.profileLocationOwned);
                        ListView locationVolunteeredList = view.findViewById(R.id.profileLocationVolunteered);

                        List<String> ownedLocationIds = user.getLocationsOwned();
                        List<String> volunteeredLocationIds = user.getLocationsVolunteered();

                        ArrayAdapter<String> adapterLocationOwned = new ArrayAdapter<>(
                                requireActivity(),
                                android.R.layout.simple_list_item_1
                        );

                        ArrayAdapter<String> adapterLocationVolunteered = new ArrayAdapter<>(
                                requireActivity(),
                                android.R.layout.simple_list_item_1
                        );

                        locationOwnedList.setAdapter(adapterLocationOwned);
                        locationVolunteeredList.setAdapter(adapterLocationVolunteered);

                        for(String id : ownedLocationIds) {
                            DocumentReference locationRef = db.collection("Locations").document(id);
                            locationRef.get().addOnCompleteListener( taskNew -> {
                                Location location = taskNew.getResult().toObject(Location.class);
                                List<String> volunteerList = location.getVolunteers();
                                String name = location.getLocationName();

                                if(name != null) {
                                    adapterLocationOwned.add(name + " (" + volunteerList.size() + " volunteer)");
                                } else {
                                    Log.e(TAG, "null value");
                                }
                            });
                        }
                        adapterLocationOwned.notifyDataSetChanged();

                        for(String id : volunteeredLocationIds) {
                            DocumentReference locationRef = db.collection("Locations").document(id);
                            locationRef.get().addOnCompleteListener( taskNew -> {
                                Location location = taskNew.getResult().toObject(Location.class);
                                String name = location.getLocationName();

                                if(name != null) {
                                    adapterLocationVolunteered.add(name);
                                } else {
                                    Log.e(TAG, "null value");
                                }
                            });
                        }
                        adapterLocationVolunteered.notifyDataSetChanged();

                    }
                }
            }
        });




        return view;
    }
}