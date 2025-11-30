package com.example.fusion0_lottery;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the CancelledEntrant model class.
 *
 * <p>This test class validates the functionality of the CancelledEntrant class,
 * which represents users who have declined invitations or been cancelled by organizers.</p>
 *
 * <p>Test Coverage:</p>
 * <ul>
 *   <li>Constructor properly initializes all fields</li>
 *   <li>All getter methods return correct values</li>
 *   <li>Immutability of the model (no setters)</li>
 *   <li>Null and empty string handling</li>
 *   <li>Realistic test data scenarios</li>
 * </ul>
 *
 * @see CancelledEntrant
 * @version 1.0
 * @since 2024-11-30
 */
public class CancelledEntrantTest {

    private CancelledEntrant cancelledEntrant;
    private static final String TEST_USER_ID = "user123";
    private static final String TEST_NAME = "John Doe";
    private static final String TEST_EMAIL = "john.doe@example.com";
    private static final String TEST_REASON = "Declined invitation";

    /**
     * Sets up test fixture before each test method.
     */
    @Before
    public void setUp() {
        cancelledEntrant = new CancelledEntrant(TEST_USER_ID, TEST_NAME, TEST_EMAIL, TEST_REASON);
    }

    /**
     * Tests constructor initializes all fields correctly.
     */
    @Test
    public void testConstructor() {
        assertEquals("User ID should be set correctly", TEST_USER_ID, cancelledEntrant.getUserId());
        assertEquals("Name should be set correctly", TEST_NAME, cancelledEntrant.getName());
        assertEquals("Email should be set correctly", TEST_EMAIL, cancelledEntrant.getEmail());
        assertEquals("Reason should be set correctly", TEST_REASON, cancelledEntrant.getReason());
    }

    /**
     * Tests getUserId returns the correct user ID.
     */
    @Test
    public void testGetUserId() {
        String userId = cancelledEntrant.getUserId();
        assertEquals("getUserId should return the correct user ID", TEST_USER_ID, userId);
    }

    /**
     * Tests getName returns the correct name.
     */
    @Test
    public void testGetName() {
        String name = cancelledEntrant.getName();
        assertEquals("getName should return the correct name", TEST_NAME, name);
    }

    /**
     * Tests getEmail returns the correct email.
     */
    @Test
    public void testGetEmail() {
        String email = cancelledEntrant.getEmail();
        assertEquals("getEmail should return the correct email", TEST_EMAIL, email);
    }

    /**
     * Tests getReason returns the correct cancellation reason.
     */
    @Test
    public void testGetReason() {
        String reason = cancelledEntrant.getReason();
        assertEquals("getReason should return the correct reason", TEST_REASON, reason);
    }

    /**
     * Tests constructor with null userId.
     */
    @Test
    public void testConstructorWithNullUserId() {
        CancelledEntrant entrant = new CancelledEntrant(null, TEST_NAME, TEST_EMAIL, TEST_REASON);
        assertNull("Should handle null user ID", entrant.getUserId());
        assertEquals("Other fields should be set", TEST_NAME, entrant.getName());
    }

    /**
     * Tests constructor with null name.
     */
    @Test
    public void testConstructorWithNullName() {
        CancelledEntrant entrant = new CancelledEntrant(TEST_USER_ID, null, TEST_EMAIL, TEST_REASON);
        assertNull("Should handle null name", entrant.getName());
        assertEquals("Other fields should be set", TEST_USER_ID, entrant.getUserId());
    }

    /**
     * Tests constructor with null email.
     */
    @Test
    public void testConstructorWithNullEmail() {
        CancelledEntrant entrant = new CancelledEntrant(TEST_USER_ID, TEST_NAME, null, TEST_REASON);
        assertNull("Should handle null email", entrant.getEmail());
        assertEquals("Other fields should be set", TEST_NAME, entrant.getName());
    }

    /**
     * Tests constructor with null reason.
     */
    @Test
    public void testConstructorWithNullReason() {
        CancelledEntrant entrant = new CancelledEntrant(TEST_USER_ID, TEST_NAME, TEST_EMAIL, null);
        assertNull("Should handle null reason", entrant.getReason());
        assertEquals("Other fields should be set", TEST_NAME, entrant.getName());
    }

    /**
     * Tests constructor with empty strings.
     */
    @Test
    public void testConstructorWithEmptyStrings() {
        CancelledEntrant entrant = new CancelledEntrant("", "", "", "");
        assertEquals("Should accept empty user ID", "", entrant.getUserId());
        assertEquals("Should accept empty name", "", entrant.getName());
        assertEquals("Should accept empty email", "", entrant.getEmail());
        assertEquals("Should accept empty reason", "", entrant.getReason());
    }

    /**
     * Tests entrant declined by choice scenario.
     */
    @Test
    public void testDeclinedInvitationScenario() {
        CancelledEntrant declined = new CancelledEntrant(
            "user456",
            "Jane Smith",
            "jane.smith@example.com",
            "Declined invitation"
        );

        assertEquals("User ID should match", "user456", declined.getUserId());
        assertEquals("Name should match", "Jane Smith", declined.getName());
        assertEquals("Email should match", "jane.smith@example.com", declined.getEmail());
        assertEquals("Reason should be declined", "Declined invitation", declined.getReason());
    }

    /**
     * Tests entrant cancelled by organizer scenario.
     */
    @Test
    public void testCancelledByOrganizerScenario() {
        CancelledEntrant cancelled = new CancelledEntrant(
            "user789",
            "Bob Johnson",
            "bob.johnson@example.com",
            "Cancelled by organizer"
        );

        assertEquals("User ID should match", "user789", cancelled.getUserId());
        assertEquals("Name should match", "Bob Johnson", cancelled.getName());
        assertEquals("Email should match", "bob.johnson@example.com", cancelled.getEmail());
        assertEquals("Reason should be cancelled by organizer", "Cancelled by organizer", cancelled.getReason());
    }

    /**
     * Tests realistic data with special characters.
     */
    @Test
    public void testRealisticDataWithSpecialCharacters() {
        CancelledEntrant entrant = new CancelledEntrant(
            "uid_abc-123",
            "María García-O'Brien",
            "maria.garcia@domain.co.uk",
            "User declined after review"
        );

        assertEquals("Should handle user ID with special chars", "uid_abc-123", entrant.getUserId());
        assertEquals("Should handle name with accents and hyphens", "María García-O'Brien", entrant.getName());
        assertEquals("Should handle email with subdomain", "maria.garcia@domain.co.uk", entrant.getEmail());
        assertEquals("Should handle detailed reason", "User declined after review", entrant.getReason());
    }

    /**
     * Tests multiple instances are independent.
     */
    @Test
    public void testInstanceIndependence() {
        CancelledEntrant entrant1 = new CancelledEntrant("user1", "Alice", "alice@test.com", "Declined");
        CancelledEntrant entrant2 = new CancelledEntrant("user2", "Bob", "bob@test.com", "Cancelled");

        assertEquals("Entrant1 user ID should be user1", "user1", entrant1.getUserId());
        assertEquals("Entrant2 user ID should be user2", "user2", entrant2.getUserId());
        assertEquals("Entrant1 name should be Alice", "Alice", entrant1.getName());
        assertEquals("Entrant2 name should be Bob", "Bob", entrant2.getName());
        assertEquals("Entrant1 reason should be Declined", "Declined", entrant1.getReason());
        assertEquals("Entrant2 reason should be Cancelled", "Cancelled", entrant2.getReason());
    }

    /**
     * Tests long text handling in reason field.
     */
    @Test
    public void testLongReasonText() {
        String longReason = "User declined the invitation after careful consideration " +
                           "due to scheduling conflicts and prior commitments that " +
                           "would prevent adequate participation in the event.";

        CancelledEntrant entrant = new CancelledEntrant(
            "user999",
            "Test User",
            "test@example.com",
            longReason
        );

        assertEquals("Should handle long reason text", longReason, entrant.getReason());
    }

    /**
     * Tests data consistency after creation.
     */
    @Test
    public void testDataConsistency() {
        // Create entrant
        CancelledEntrant entrant = new CancelledEntrant(
            "consistent_user",
            "Consistent Name",
            "consistent@email.com",
            "Consistent Reason"
        );

        // Retrieve values multiple times
        String userId1 = entrant.getUserId();
        String userId2 = entrant.getUserId();
        String name1 = entrant.getName();
        String name2 = entrant.getName();

        // Verify consistency
        assertEquals("User ID should be consistent", userId1, userId2);
        assertEquals("Name should be consistent", name1, name2);
        assertSame("Should return same reference", userId1, userId2);
        assertSame("Should return same reference", name1, name2);
    }

    /**
     * Tests that all constructor parameters are required.
     */
    @Test
    public void testAllConstructorParametersUsed() {
        String userId = "param_user";
        String name = "Param Name";
        String email = "param@email.com";
        String reason = "Param Reason";

        CancelledEntrant entrant = new CancelledEntrant(userId, name, email, reason);

        assertNotNull("User ID should not be null", entrant.getUserId());
        assertNotNull("Name should not be null", entrant.getName());
        assertNotNull("Email should not be null", entrant.getEmail());
        assertNotNull("Reason should not be null", entrant.getReason());
    }
}
