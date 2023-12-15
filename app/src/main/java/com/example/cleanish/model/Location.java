package com.example.cleanish.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Location {

    private String locationName;
    private String locationOwnerId;
    private Date eventDate;
    private Date dateCreated;
    private int duration;
    //String Id
    private List<String> volunteers;
    private String latitude;
    private String longitude;
    private int amountTrashCollected;
    private List<List> notifications;

    private boolean isFinished;



    public Location() {
        this.volunteers = new ArrayList<>();
        this.notifications = new ArrayList<>();
        this.amountTrashCollected = new Integer(0);
        this.isFinished = false;
    }

    public Location(String locationName, String locationOwnerId, Date eventDate, int duration,
                    String latitude, String longitude, Date dateCreated) {

        this.locationName = locationName;
        this.locationOwnerId = locationOwnerId;
        this.eventDate = eventDate;
        this.dateCreated = dateCreated;
        this.duration = duration;
        this.latitude = latitude;
        this.longitude = longitude;
        this.volunteers = new ArrayList<>();
        this.notifications = new ArrayList<>();
        this.amountTrashCollected = new Integer(0);
        this.isFinished = false;
    }

    public int getAmountTrashCollected() {
        return amountTrashCollected;
    }

    public void setAmountTrashCollected(int amountTrashCollected) {
        this.amountTrashCollected = amountTrashCollected;
    }

    public List<List> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<List> notifications) {
        this.notifications = notifications;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getLocationOwnerId() {
        return locationOwnerId;
    }

    public void setLocationOwnerId(String locationOwnerId) {
        this.locationOwnerId = locationOwnerId;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public List<String> getVolunteers() {
        return volunteers;
    }

    public void setVolunteers(List<String> volunteers) {
        this.volunteers = volunteers;
    }

    public void addVolunteer(String volunteerID) {
        volunteers.add(volunteerID);
    }

    public void removeVolunteer(String volunteerID) {
        volunteers.remove(volunteerID);
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public boolean getIsFinished() {
        return isFinished;
    }

    public void setIsFinished(boolean finished) {
        this.isFinished = finished;
    }
}