package com.example.fusion0_lottery;

import java.util.List;
import java.util.Map;

/**
 * Event model class for storing event information in Firebase Firestore
 */
public class Event {
    private String eventId;
    private String eventName;
    private String interests;
    private String description;
    private String startDate;
    private String endDate;
    private String time;
    private double price;
    private String location;
    private String posterUrl;
    private Integer maxEntrants; // Optional, can be null
    private String registrationStart;
    private String registrationEnd;
    private String qrCodeUrl; // URL to the generated QR code

    private Integer waitingListCount;
    private Integer userSelectedCount;
    private Integer userEnrolledCount;
    private Integer numberOfWinners;
    private String posterImage;
    private String organizerName;
    private List<String> waitingList;
    private List<Map<String, Object>> winnersList;

    // Empty constructor required for Firebase
    public Event() {
    }

    // Constructor for creating new events
    public Event(String eventName, String interests, String description, String startDate, String endDate,
                 String time, double price, String location, String registrationStart,
                 String registrationEnd, Integer maxEntrants, Integer waitingListCount,
                 Integer userSelectedCount, Integer userEnrolledCount, Integer numberOfWinners) {
        this.eventName = eventName;
        this.interests = interests;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.time = time;
        this.price = price;
        this.location = location;
        this.registrationStart = registrationStart;
        this.registrationEnd = registrationEnd;
        this.maxEntrants = maxEntrants;
        this.waitingListCount = waitingListCount;
        this.userSelectedCount = userSelectedCount;
        this.userEnrolledCount = userEnrolledCount;
        this.numberOfWinners = numberOfWinners;
    }

    public Event(String eventName, String description, String startDate, String endDate, String time, double price, String location, String registrationStart, String registrationEnd, Integer maxEntrants, int i, int i1, int i2) {
    }

    // Getters and Setters
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getInterests() {
        return interests;
    }

    public void setInterests(String interests) {
        this.interests = interests;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public Integer getMaxEntrants() {
        return maxEntrants;
    }

    public void setMaxEntrants(Integer maxEntrants) {
        this.maxEntrants = maxEntrants;
    }

    public String getRegistrationStart() {
        return registrationStart;
    }

    public void setRegistrationStart(String registrationStart) {
        this.registrationStart = registrationStart;
    }

    public String getRegistrationEnd() {
        return registrationEnd;
    }

    public void setRegistrationEnd(String registrationEnd) {
        this.registrationEnd = registrationEnd;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    public Integer getWaitingListCount() {
        return waitingListCount;
    }

    public void setWaitingListCount(Integer waitingListCount) {
        this.waitingListCount = waitingListCount;
    }

    public Integer getUserSelectedCount() {
        return userSelectedCount;
    }

    public void setUserSelectedCount(Integer userSelectedCount) {
        this.userSelectedCount = userSelectedCount;
    }

    public Integer getUserEnrolledCount() {
        return userEnrolledCount;
    }

    public void setUserEnrolledCount(Integer userEnrolledCount) {
        this.userEnrolledCount = userEnrolledCount;
    }

    public Integer getNumberOfWinners() {
        return numberOfWinners;
    }

    public void setNumberOfWinners(Integer numberOfWinners) {
        this.numberOfWinners = numberOfWinners;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public String getPosterImage() {
        return posterImage;
    }

    public void setPosterImage(String posterImage) {
        this.posterImage = posterImage;
    }

    public List<String> getWaitingList() {
        return waitingList;
    }

    public void setWaitingList(List<String> waitingList) {
        this.waitingList = waitingList;
    }

    public List<Map<String, Object>> getWinnersList() {
        return winnersList;
    }

    public void setWinnersList(List<Map<String, Object>> winnersList) {
        this.winnersList = winnersList;
    }
}