package com.example.fusion0_lottery;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the LotteryWinners model class.
 *
 * <p>This test class validates the functionality of the LotteryWinners class,
 * including constructor initialization, getter/setter methods, and data integrity.</p>
 *
 * <p>Test Coverage:</p>
 * <ul>
 *   <li>Default constructor creates empty object</li>
 *   <li>Single-parameter constructor sets name and default status</li>
 *   <li>Two-parameter constructor sets name and custom status</li>
 *   <li>All getter methods return correct values</li>
 *   <li>All setter methods update values correctly</li>
 *   <li>Null and empty string handling</li>
 * </ul>
 *
 * @see LotteryWinners
 * @version 1.0
 * @since 2024-11-30
 */
public class LotteryWinnersTest {

    private LotteryWinners winner;

    /**
     * Sets up test fixture before each test method.
     * Creates a fresh LotteryWinners instance for each test.
     */
    @Before
    public void setUp() {
        winner = new LotteryWinners();
    }

    /**
     * Tests the default constructor creates an object with null values.
     */
    @Test
    public void testDefaultConstructor() {
        assertNull("Default name should be null", winner.getName());
        assertNull("Default joinDate should be null", winner.getJoinDate());
        assertNull("Default status should be null", winner.getStatus());
    }

    /**
     * Tests the single-parameter constructor sets name and default status.
     */
    @Test
    public void testConstructorWithName() {
        LotteryWinners namedWinner = new LotteryWinners("John Doe");

        assertEquals("Name should be set correctly", "John Doe", namedWinner.getName());
        assertEquals("Status should default to Pending", "Pending", namedWinner.getStatus());
        assertNull("Join date should be null by default", namedWinner.getJoinDate());
    }

    /**
     * Tests the two-parameter constructor sets name and custom status.
     */
    @Test
    public void testConstructorWithNameAndStatus() {
        LotteryWinners acceptedWinner = new LotteryWinners("Jane Smith", "Accepted");

        assertEquals("Name should be set correctly", "Jane Smith", acceptedWinner.getName());
        assertEquals("Status should be set to Accepted", "Accepted", acceptedWinner.getStatus());
        assertNull("Join date should be null by default", acceptedWinner.getJoinDate());
    }

    /**
     * Tests getName returns the correct name.
     */
    @Test
    public void testGetName() {
        winner.setName("Alice Johnson");
        assertEquals("getName should return the set name", "Alice Johnson", winner.getName());
    }

    /**
     * Tests setName updates the name correctly.
     */
    @Test
    public void testSetName() {
        String testName = "Bob Williams";
        winner.setName(testName);
        assertEquals("setName should update the name", testName, winner.getName());
    }

    /**
     * Tests setName handles null values.
     */
    @Test
    public void testSetNameWithNull() {
        winner.setName(null);
        assertNull("setName should accept null values", winner.getName());
    }

    /**
     * Tests setName handles empty strings.
     */
    @Test
    public void testSetNameWithEmptyString() {
        winner.setName("");
        assertEquals("setName should accept empty strings", "", winner.getName());
    }

    /**
     * Tests getJoinDate returns the correct join date.
     */
    @Test
    public void testGetJoinDate() {
        winner.joinDate = "2024-11-30";
        assertEquals("getJoinDate should return the join date", "2024-11-30", winner.getJoinDate());
    }

    /**
     * Tests setJoinDate updates the join date correctly.
     */
    @Test
    public void testSetJoinDate() {
        String testDate = "2024-12-01";
        winner.setJoinDate(testDate);
        assertEquals("setJoinDate should update the join date", testDate, winner.getJoinDate());
        assertEquals("Public field should also be updated", testDate, winner.joinDate);
    }

    /**
     * Tests join date can be set via public field directly.
     */
    @Test
    public void testDirectJoinDateAccess() {
        winner.joinDate = "2024-11-15";
        assertEquals("Direct field access should work", "2024-11-15", winner.joinDate);
        assertEquals("Getter should return same value", "2024-11-15", winner.getJoinDate());
    }

    /**
     * Tests getStatus returns the correct status.
     */
    @Test
    public void testGetStatus() {
        winner.setStatus("Pending");
        assertEquals("getStatus should return the status", "Pending", winner.getStatus());
    }

    /**
     * Tests setStatus updates status to "Accepted".
     */
    @Test
    public void testSetStatusAccepted() {
        winner.setStatus("Accepted");
        assertEquals("setStatus should update to Accepted", "Accepted", winner.getStatus());
    }

    /**
     * Tests setStatus updates status to "Declined".
     */
    @Test
    public void testSetStatusDeclined() {
        winner.setStatus("Declined");
        assertEquals("setStatus should update to Declined", "Declined", winner.getStatus());
    }

    /**
     * Tests setStatus updates status to "Pending".
     */
    @Test
    public void testSetStatusPending() {
        winner.setStatus("Pending");
        assertEquals("setStatus should update to Pending", "Pending", winner.getStatus());
    }

    /**
     * Tests setStatus handles null values.
     */
    @Test
    public void testSetStatusWithNull() {
        winner.setStatus(null);
        assertNull("setStatus should accept null values", winner.getStatus());
    }

    /**
     * Tests complete object creation and modification.
     */
    @Test
    public void testCompleteWorkflow() {
        // Create winner with name
        LotteryWinners testWinner = new LotteryWinners("Charlie Brown");

        // Verify initial state
        assertEquals("Initial name should be set", "Charlie Brown", testWinner.getName());
        assertEquals("Initial status should be Pending", "Pending", testWinner.getStatus());

        // Update join date
        testWinner.setJoinDate("2024-11-30");
        assertEquals("Join date should be updated", "2024-11-30", testWinner.getJoinDate());

        // Update status to Accepted
        testWinner.setStatus("Accepted");
        assertEquals("Status should be updated to Accepted", "Accepted", testWinner.getStatus());

        // Verify all fields
        assertEquals("Name should remain unchanged", "Charlie Brown", testWinner.getName());
        assertEquals("Join date should remain set", "2024-11-30", testWinner.getJoinDate());
        assertEquals("Status should be Accepted", "Accepted", testWinner.getStatus());
    }

    /**
     * Tests multiple status transitions.
     */
    @Test
    public void testStatusTransitions() {
        LotteryWinners testWinner = new LotteryWinners("David Lee", "Pending");

        // Transition to Accepted
        testWinner.setStatus("Accepted");
        assertEquals("Status should transition to Accepted", "Accepted", testWinner.getStatus());

        // Transition to Declined
        testWinner.setStatus("Declined");
        assertEquals("Status should transition to Declined", "Declined", testWinner.getStatus());

        // Back to Pending
        testWinner.setStatus("Pending");
        assertEquals("Status should transition back to Pending", "Pending", testWinner.getStatus());
    }

    /**
     * Tests object with realistic data.
     */
    @Test
    public void testRealisticData() {
        LotteryWinners realWinner = new LotteryWinners("Sarah Martinez", "Pending");
        realWinner.setJoinDate("2024-11-25");

        assertEquals("Name should match realistic data", "Sarah Martinez", realWinner.getName());
        assertEquals("Join date should match", "2024-11-25", realWinner.getJoinDate());
        assertEquals("Status should be Pending", "Pending", realWinner.getStatus());

        // Simulate user accepting
        realWinner.setStatus("Accepted");
        assertEquals("Status should update to Accepted", "Accepted", realWinner.getStatus());
    }

    /**
     * Tests handling of special characters in names.
     */
    @Test
    public void testSpecialCharactersInName() {
        winner.setName("O'Brien-Smith");
        assertEquals("Should handle special characters", "O'Brien-Smith", winner.getName());

        winner.setName("José García");
        assertEquals("Should handle accented characters", "José García", winner.getName());
    }

    /**
     * Tests date format consistency.
     */
    @Test
    public void testDateFormatConsistency() {
        String[] validDates = {"2024-11-30", "2024-01-01", "2024-12-31"};

        for (String date : validDates) {
            winner.setJoinDate(date);
            assertEquals("Should store date as provided: " + date, date, winner.getJoinDate());
        }
    }

    /**
     * Tests that the object state is independent between instances.
     */
    @Test
    public void testInstanceIndependence() {
        LotteryWinners winner1 = new LotteryWinners("Alice", "Pending");
        LotteryWinners winner2 = new LotteryWinners("Bob", "Accepted");

        assertEquals("Winner1 name should be Alice", "Alice", winner1.getName());
        assertEquals("Winner2 name should be Bob", "Bob", winner2.getName());
        assertEquals("Winner1 status should be Pending", "Pending", winner1.getStatus());
        assertEquals("Winner2 status should be Accepted", "Accepted", winner2.getStatus());

        // Modify winner1
        winner1.setStatus("Declined");

        // Verify winner2 is unaffected
        assertEquals("Winner2 status should still be Accepted", "Accepted", winner2.getStatus());
    }
}
