package com.example.cleanish.model;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String email;
    private String password;
    private String role;

    //String Id
    private List<String> locationsOwned;
    private List<String> locationsVolunteered;

    public User() {
        this.locationsOwned = new ArrayList<>();
        this.locationsVolunteered = new ArrayList<>();
    }

    public User(String email) {
        this.email = email;
        this.locationsOwned = new ArrayList<>();
        this.locationsVolunteered = new ArrayList<>();
    }

    public User(String email, String password, String role) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.locationsOwned = new ArrayList<>();
        this.locationsVolunteered = new ArrayList<>();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<String> getLocationsOwned() {
        return locationsOwned;
    }

    public List<String> getLocationsVolunteered() {
        return locationsVolunteered;
    }

}