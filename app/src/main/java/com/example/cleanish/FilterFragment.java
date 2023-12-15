package com.example.cleanish;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.cleanish.model.Location;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.badge.BadgeUtils;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FilterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FilterFragment extends Fragment {
    private DatePickerDialog datePickerDialogBetween, datePickerDialogAnd;
    private Button betweenButton, andButton;

    private Date dateBetween, dateAnd;
    private Calendar calendarBetween, calendarAnd;
    private boolean isSearch, isFilterDate, isFilterDateStart, isFilterDateEnd;
    private SearchView searchView;
    private ListView listView;
    private ArrayList<Location> locationArrayList;
    Map<String, String> locationsMap = new HashMap<>();


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FilterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FilterFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FilterFragment newInstance(String param1, String param2) {
        FilterFragment fragment = new FilterFragment();
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

        isSearch = false;
//        isFilterDate = false;
        isFilterDateStart = false;
        isFilterDateEnd = false;

        initDatePickerBetween();
        initDatePickerAnd();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter, container, false);

//        Fetch firestore data



//      Search----------------------------------------------------------------------------------------
        listView = (ListView) view.findViewById(R.id.itemListView);
        searchView = (SearchView) view.findViewById(R.id.itemListSearchView);
        initSearchWidgets();

//      Date----------------------------------------------------------------------------------------
        betweenButton = view.findViewById(R.id.datePickerFilterBetween);
        betweenButton.setText("");
        betweenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialogBetween.show();
            }
        });

        andButton = view.findViewById(R.id.datePickerFilterAnd);
        andButton.setText("");
        andButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialogAnd.show();
            }
        });

        return view;
    }

//    Search Widget---------------------------------------------------------------------------------
    private void initSearchWidgets(){
        CollectionReference collectionRef = FirebaseFirestore.getInstance().collection("Locations");
        collectionRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        locationArrayList = new ArrayList<>(task.getResult().toObjects(Location.class));

                        for(DocumentSnapshot doc : task.getResult()) {
                            Location location = doc.toObject(Location.class);
                            String id = doc.getId();
                            locationsMap.put(location.getLocationName(), id);
                        }

                        updateFilter("");

                    }
                });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                // Handle the submission of the query
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
//                Log.d(TAG, "String Search = " + s);
                if (s.isEmpty()){
                    isSearch = false;
                }else {
                    isSearch = true;
                }

                updateFilter(s);


                return false;
            }
        });
    }

    private void updateFilter(String s){

        ArrayList<Location> locationItemList = new ArrayList<>();
        ArrayList<String> locationItemName = new ArrayList<>();

        for(Location location : locationArrayList) {
            Log.d(TAG, "Date start " + dateBetween + ", date end " + dateAnd + ", isSearch " + isSearch + ", is Filter " + isFilterDate + ", is Filter Start " + isFilterDateStart + ", is Filter End " + isFilterDateEnd );
            if(isFilterDate && isSearch){
                if(isFilterDateStart && isFilterDateEnd){
                    Log.d(TAG, "Event date " + location.getEventDate() + ", start between " + dateBetween + " and " + dateAnd);
                    if(location.getEventDate().after(dateBetween) && location.getEventDate().before(dateAnd)){
                        if(location.getLocationName().toLowerCase().contains(s.toLowerCase())) {
                            locationItemList.add(location);
                            locationItemName.add(location.getLocationName());
                        }
                    }else{
                        Toast.makeText(getContext(), "No Match result found", Toast.LENGTH_SHORT).show();
                    }

                }else {
                    Toast.makeText(getContext(), "Please finish filling in the date", Toast.LENGTH_SHORT).show();
                }

            }else if(isSearch){
                if(location.getLocationName().toLowerCase().contains(s.toLowerCase())) {
                    locationItemList.add(location);
                    locationItemName.add(location.getLocationName());
                }
            } else if (isFilterDate) {
                if(isFilterDateStart && isFilterDateEnd){
                    Log.d(TAG, "Event date " + location.getEventDate() + ", start between " + dateBetween + " and " + dateAnd);
                    if(location.getEventDate().after(dateBetween) && location.getEventDate().before(dateAnd)){
                            locationItemList.add(location);
                            locationItemName.add(location.getLocationName());
                    }else{
                        Toast.makeText(getContext(), "No Match result found", Toast.LENGTH_SHORT).show();
                    }

                }else {
                    Toast.makeText(getContext(), "Please finish filling in the date", Toast.LENGTH_SHORT).show();
                }
            }else {
                locationItemList.add(location);
                locationItemName.add(location.getLocationName());
            }

        }

        ListAdapter adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, locationItemName);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id){

                Location selectedLocation = locationItemList.get(position);

                Intent intent = new Intent(getContext(), RegisterVolunteerActivity.class);

                String locationId = locationsMap.get(selectedLocation.getLocationName());
                Log.d(TAG, "Location ID " + locationId + ", selected Location get name " + selectedLocation.getLocationName() + ", locationsMap: " + locationsMap.toString());

                intent.putExtra("locationID", locationId);

                intent.putExtra("locationName", selectedLocation.getLocationName());
                intent.putExtra("locationOwner", selectedLocation.getLocationOwnerId());
                intent.putExtra("duration", String.valueOf(selectedLocation.getDuration()));
                intent.putExtra("isFinished", selectedLocation.getIsFinished());

                Date eventDate = selectedLocation.getEventDate(); // Assuming loc.getEventDate() returns a Date object
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                String formattedEventDate = dateFormat.format(eventDate);
                intent.putExtra("eventDate", formattedEventDate);

                intent.putExtra("from", "filter");

                startActivity(intent);
            }
        });
    }

//    Handle Button---------------------------------------------------------------------------------
//    Between -----------------------------------------------
    public void initDatePickerBetween() {
        DatePickerDialog.OnDateSetListener dateSetListenerBetween = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) { //month 0-11
                isFilterDate = true;
                isFilterDateStart = true;
                calendarBetween.set(year, month, day);

                if (calendarBetween.getTime().after(dateAnd)) {
                    Calendar temp = calendarBetween;
                    calendarBetween = calendarAnd;
                    calendarAnd = temp;

                    updateDialog();
                } else {
                    calendarBetween.set(year, month, day);
                    dateBetween = calendarBetween.getTime();
                    betweenButton.setText(makeDateString(day, month, year));
//                    Toast.makeText(getContext(), "Else Selected Date between " + dateBetween + " and " + dateAnd, Toast.LENGTH_SHORT).show();
                }

                updateFilter("");
            }
        };
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        calendarBetween = Calendar.getInstance();
        calendarBetween.set(year, month, day);
        dateBetween = calendarBetween.getTime();

        int style = AlertDialog.THEME_HOLO_LIGHT;
        datePickerDialogBetween = new DatePickerDialog(requireContext(), style, dateSetListenerBetween, year, month, day);
    }


//    And ---------------------------------------------

    public void initDatePickerAnd() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) { //month 0-11
                isFilterDate = true;
                isFilterDateEnd = true;
                calendarAnd.set(year, month, day);

                if (calendarAnd.getTime().before(dateBetween)) {
                    Calendar temp = calendarAnd;
                    calendarAnd = calendarBetween;
                    calendarBetween = temp;

//                    Log.d(TAG, "Calender " + calendarBetween.toString());
//                    Log.d(TAG, "Calender " + calendarAnd.toString());

                    updateDialog();
                } else {
                    calendarAnd.set(year, month, day);
                    dateAnd = calendarAnd.getTime();
                    andButton.setText(makeDateString(day, month, year));
//                    Toast.makeText(getContext(), "Else Selected Date between " + dateBetween + " and " + dateAnd, Toast.LENGTH_SHORT).show();
                }

                updateFilter("");

            }
        };
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        calendarAnd = Calendar.getInstance();
        calendarAnd.set(year, month, day);
        dateAnd = calendarAnd.getTime();

        int style = AlertDialog.THEME_HOLO_LIGHT;
        datePickerDialogAnd = new DatePickerDialog(requireContext(), style, dateSetListener, year, month, day);
    }

    private void updateDialog(){
        String dateAndText = makeDateString(calendarAnd.get(Calendar.DAY_OF_MONTH), calendarAnd.get(Calendar.MONTH), calendarAnd.get(Calendar.YEAR));
        String dateBetweenText = makeDateString(calendarBetween.get(Calendar.DAY_OF_MONTH), calendarBetween.get(Calendar.MONTH), calendarBetween.get(Calendar.YEAR));

        dateAnd = calendarAnd.getTime();
        dateBetween = calendarBetween.getTime();

        andButton.setText(dateAndText);
        betweenButton.setText(dateBetweenText);

        datePickerDialogAnd.updateDate(calendarAnd.get(Calendar.YEAR), calendarAnd.get(Calendar.MONTH), calendarAnd.get(Calendar.DAY_OF_MONTH));
        datePickerDialogBetween.updateDate(calendarBetween.get(Calendar.YEAR), calendarBetween.get(Calendar.MONTH), calendarBetween.get(Calendar.DAY_OF_MONTH));

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