package com.example.fusion0_lottery;

/**
 * model class that displays the entrant's name, join date, and status in all waiting lists
 * name = entrant's name tied to their account
 * joinDate = when the user joined the waiting list
 * status = pending/selected depending on whether they are chosen or if the event is still ongoing
 */
public class WaitingListEntrants {
    public String name;
    public String joinDate;
    public String status;

    public WaitingListEntrants(String name, String joinDate, String status) {
        this.name = name;
        this.joinDate = joinDate;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setWLName(String name) {
        this.name = name;
    }

    public String getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(String joinDate) {
        this.joinDate = joinDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
