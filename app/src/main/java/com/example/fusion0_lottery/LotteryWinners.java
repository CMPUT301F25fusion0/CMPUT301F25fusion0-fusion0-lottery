package com.example.fusion0_lottery;

/**
 * Model class representing a lottery winner/selected entrant in the event lottery system.
 *
 * <p>This class encapsulates information about users who have been selected as winners
 * in an event lottery drawing. It tracks the entrant's name, join date, and their
 * current status (Pending, Accepted, or Declined).</p>
 *
 * <p><b>Purpose:</b> This model serves as a data transfer object (DTO) for displaying
 * selected entrants in the UI and managing their acceptance/decline status.</p>
 *
 * <p><b>Design Pattern:</b> Simple POJO (Plain Old Java Object) with getter/setter methods
 * following JavaBean conventions.</p>
 *
 * <p><b>Outstanding Issues:</b> The joinDate field is public but should be private with
 * proper getters/setters for better encapsulation. This should be refactored in future versions.</p>
 *
 * @see FragmentSelectedEntrants
 * @see FragmentWaitingList
 *
 * @version 1.0
 * @since 2024-11-30
 */
public class LotteryWinners {
    /**
     * The name of the lottery winner.
     */
    private String name;

    /**
     * The date when the winner joined the event waiting list.
     * Note: This field is currently public but should be made private in future versions.
     */
    public String joinDate;

    /**
     * The current status of the winner (e.g., "Pending", "Accepted", "Declined").
     */
    private String status;

    /**
     * Default constructor required for Firebase Firestore deserialization.
     */
    public LotteryWinners() {}

    /**
     * Constructs a LotteryWinners object with the specified name and default status.
     *
     * @param name The name of the lottery winner
     */
    public LotteryWinners(String name) {
        this.name = name;
        this.status = "Pending";
    }

    /**
     * Constructs a LotteryWinners object with the specified name and status.
     *
     * @param name The name of the lottery winner
     * @param status The current status of the winner (e.g., "Pending", "Accepted", "Declined")
     */
    public LotteryWinners(String name, String status) {
        this.name = name;
        this.status = status;
    }

    /**
     * Gets the name of the lottery winner.
     *
     * @return The winner's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the lottery winner.
     *
     * @param name The new name for the winner
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the date when the winner joined the waiting list.
     *
     * @return The join date as a string
     */
    public String getJoinDate() {
        return joinDate;
    }

    /**
     * Sets the date when the winner joined the waiting list.
     *
     * @param joinDate The join date as a string
     */
    public void setJoinDate(String joinDate) {
        this.joinDate = joinDate;
    }

    /**
     * Gets the current status of the lottery winner.
     *
     * @return The status (e.g., "Pending", "Accepted", "Declined")
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the current status of the lottery winner.
     *
     * @param status The new status (e.g., "Pending", "Accepted", "Declined")
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
