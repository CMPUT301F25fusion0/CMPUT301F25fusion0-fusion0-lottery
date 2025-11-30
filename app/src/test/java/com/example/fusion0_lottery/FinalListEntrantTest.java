package com.example.fusion0_lottery;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the FinalListEntrant model class.
 *
 * <p>This test class validates the functionality of the FinalListEntrant class,
 * which represents users who have accepted their lottery invitations and are
 * confirmed for the event.</p>
 *
 * <p>Test Coverage:</p>
 * <ul>
 *   <li>Constructor properly initializes all fields</li>
 *   <li>All getter methods return correct values</li>
 *   <li>Immutability of the model (no setters)</li>
 *   <li>Null and empty string handling</li>
 *   <li>Realistic test data with contact information</li>
 * </ul>
 *
 * @see FinalListEntrant
 * @version 1.0
 * @since 2024-11-30
 */
public class FinalListEntrantTest {

    private FinalListEntrant entrant;
    private static final String TEST_USER_ID = "final_user_123";
    private static final String TEST_NAME = "Sarah Martinez";
    private static final String TEST_EMAIL = "sarah.martinez@example.com";
    private static final String TEST_PHONE = "+1-555-0123";
    private static final String TEST_STATUS = "Accepted";

    /**
     * Sets up test fixture before each test method.
     */
    @Before
    public void setUp() {
        entrant = new FinalListEntrant(TEST_USER_ID, TEST_NAME, TEST_EMAIL, TEST_PHONE, TEST_STATUS);
    }

    /**
     * Tests constructor initializes all fields correctly.
     */
    @Test
    public void testConstructor() {
        assertEquals("User ID should be set correctly", TEST_USER_ID, entrant.getUserId());
        assertEquals("Name should be set correctly", TEST_NAME, entrant.getName());
        assertEquals("Email should be set correctly", TEST_EMAIL, entrant.getEmail());
        assertEquals("Phone should be set correctly", TEST_PHONE, entrant.getPhone());
        assertEquals("Status should be set correctly", TEST_STATUS, entrant.getStatus());
    }

    /**
     * Tests getUserId returns the correct user ID.
     */
    @Test
    public void testGetUserId() {
        String userId = entrant.getUserId();
        assertEquals("getUserId should return the correct user ID", TEST_USER_ID, userId);
    }

    /**
     * Tests getName returns the correct name.
     */
    @Test
    public void testGetName() {
        String name = entrant.getName();
        assertEquals("getName should return the correct name", TEST_NAME, name);
    }

    /**
     * Tests getEmail returns the correct email.
     */
    @Test
    public void testGetEmail() {
        String email = entrant.getEmail();
        assertEquals("getEmail should return the correct email", TEST_EMAIL, email);
    }

    /**
     * Tests getPhone returns the correct phone number.
     */
    @Test
    public void testGetPhone() {
        String phone = entrant.getPhone();
        assertEquals("getPhone should return the correct phone", TEST_PHONE, phone);
    }

    /**
     * Tests getStatus returns the correct status.
     */
    @Test
    public void testGetStatus() {
        String status = entrant.getStatus();
        assertEquals("getStatus should return the correct status", TEST_STATUS, status);
    }

    /**
     * Tests constructor with null userId.
     */
    @Test
    public void testConstructorWithNullUserId() {
        FinalListEntrant nullUserIdEntrant = new FinalListEntrant(null, TEST_NAME, TEST_EMAIL, TEST_PHONE, TEST_STATUS);
        assertNull("Should handle null user ID", nullUserIdEntrant.getUserId());
        assertEquals("Other fields should be set", TEST_NAME, nullUserIdEntrant.getName());
    }

    /**
     * Tests constructor with null name.
     */
    @Test
    public void testConstructorWithNullName() {
        FinalListEntrant nullNameEntrant = new FinalListEntrant(TEST_USER_ID, null, TEST_EMAIL, TEST_PHONE, TEST_STATUS);
        assertNull("Should handle null name", nullNameEntrant.getName());
        assertEquals("Other fields should be set", TEST_USER_ID, nullNameEntrant.getUserId());
    }

    /**
     * Tests constructor with null email.
     */
    @Test
    public void testConstructorWithNullEmail() {
        FinalListEntrant nullEmailEntrant = new FinalListEntrant(TEST_USER_ID, TEST_NAME, null, TEST_PHONE, TEST_STATUS);
        assertNull("Should handle null email", nullEmailEntrant.getEmail());
        assertEquals("Other fields should be set", TEST_NAME, nullEmailEntrant.getName());
    }

    /**
     * Tests constructor with null phone.
     */
    @Test
    public void testConstructorWithNullPhone() {
        FinalListEntrant nullPhoneEntrant = new FinalListEntrant(TEST_USER_ID, TEST_NAME, TEST_EMAIL, null, TEST_STATUS);
        assertNull("Should handle null phone", nullPhoneEntrant.getPhone());
        assertEquals("Other fields should be set", TEST_NAME, nullPhoneEntrant.getName());
    }

    /**
     * Tests constructor with null status.
     */
    @Test
    public void testConstructorWithNullStatus() {
        FinalListEntrant nullStatusEntrant = new FinalListEntrant(TEST_USER_ID, TEST_NAME, TEST_EMAIL, TEST_PHONE, null);
        assertNull("Should handle null status", nullStatusEntrant.getStatus());
        assertEquals("Other fields should be set", TEST_NAME, nullStatusEntrant.getName());
    }

    /**
     * Tests constructor with empty strings.
     */
    @Test
    public void testConstructorWithEmptyStrings() {
        FinalListEntrant emptyEntrant = new FinalListEntrant("", "", "", "", "");
        assertEquals("Should accept empty user ID", "", emptyEntrant.getUserId());
        assertEquals("Should accept empty name", "", emptyEntrant.getName());
        assertEquals("Should accept empty email", "", emptyEntrant.getEmail());
        assertEquals("Should accept empty phone", "", emptyEntrant.getPhone());
        assertEquals("Should accept empty status", "", emptyEntrant.getStatus());
    }

    /**
     * Tests realistic scenario with complete contact information.
     */
    @Test
    public void testRealisticCompleteData() {
        FinalListEntrant realistic = new FinalListEntrant(
            "uid_789xyz",
            "Michael Chen",
            "michael.chen@company.com",
            "+1-555-0198",
            "Accepted"
        );

        assertEquals("User ID should match", "uid_789xyz", realistic.getUserId());
        assertEquals("Name should match", "Michael Chen", realistic.getName());
        assertEquals("Email should match", "michael.chen@company.com", realistic.getEmail());
        assertEquals("Phone should match", "+1-555-0198", realistic.getPhone());
        assertEquals("Status should be Accepted", "Accepted", realistic.getStatus());
    }

    /**
     * Tests realistic scenario with missing phone number.
     */
    @Test
    public void testRealisticNoPhoneScenario() {
        FinalListEntrant noPhone = new FinalListEntrant(
            "uid_456abc",
            "Emily Rodriguez",
            "emily.r@email.com",
            "No phone",
            "Accepted"
        );

        assertEquals("Should handle 'No phone' placeholder", "No phone", noPhone.getPhone());
        assertNotNull("Email should still be available", noPhone.getEmail());
    }

    /**
     * Tests realistic scenario with international phone format.
     */
    @Test
    public void testInternationalPhoneFormat() {
        FinalListEntrant international = new FinalListEntrant(
            "intl_user_001",
            "Akira Tanaka",
            "akira.tanaka@example.jp",
            "+81-3-1234-5678",
            "Accepted"
        );

        assertEquals("Should handle international phone format", "+81-3-1234-5678", international.getPhone());
    }

    /**
     * Tests names with special characters.
     */
    @Test
    public void testSpecialCharactersInName() {
        FinalListEntrant special = new FinalListEntrant(
            "special_user",
            "François O'Neill-MacDonald",
            "francois@example.com",
            "555-0100",
            "Accepted"
        );

        assertEquals("Should handle special characters in name",
                    "François O'Neill-MacDonald", special.getName());
    }

    /**
     * Tests email with various valid formats.
     */
    @Test
    public void testVariousEmailFormats() {
        String[] emails = {
            "simple@example.com",
            "first.last@company.co.uk",
            "user+tag@subdomain.example.org",
            "name_123@test-domain.io"
        };

        for (String email : emails) {
            FinalListEntrant testEntrant = new FinalListEntrant(
                "test_user",
                "Test Name",
                email,
                "555-0000",
                "Accepted"
            );
            assertEquals("Should handle email format: " + email, email, testEntrant.getEmail());
        }
    }

    /**
     * Tests phone number with various formats.
     */
    @Test
    public void testVariousPhoneFormats() {
        String[] phones = {
            "+1-555-0123",
            "(555) 123-4567",
            "555.123.4567",
            "5551234567",
            "+44 20 1234 5678"
        };

        for (String phone : phones) {
            FinalListEntrant testEntrant = new FinalListEntrant(
                "test_user",
                "Test Name",
                "test@example.com",
                phone,
                "Accepted"
            );
            assertEquals("Should handle phone format: " + phone, phone, testEntrant.getPhone());
        }
    }

    /**
     * Tests multiple instances are independent.
     */
    @Test
    public void testInstanceIndependence() {
        FinalListEntrant entrant1 = new FinalListEntrant(
            "user1", "Alice", "alice@test.com", "111-1111", "Accepted"
        );
        FinalListEntrant entrant2 = new FinalListEntrant(
            "user2", "Bob", "bob@test.com", "222-2222", "Accepted"
        );

        assertEquals("Entrant1 user ID should be user1", "user1", entrant1.getUserId());
        assertEquals("Entrant2 user ID should be user2", "user2", entrant2.getUserId());
        assertEquals("Entrant1 phone should be 111-1111", "111-1111", entrant1.getPhone());
        assertEquals("Entrant2 phone should be 222-2222", "222-2222", entrant2.getPhone());
    }

    /**
     * Tests data consistency after creation.
     */
    @Test
    public void testDataConsistency() {
        FinalListEntrant consistent = new FinalListEntrant(
            "consistent_user",
            "Consistent Name",
            "consistent@email.com",
            "555-9999",
            "Accepted"
        );

        // Retrieve values multiple times
        String userId1 = consistent.getUserId();
        String userId2 = consistent.getUserId();
        String email1 = consistent.getEmail();
        String email2 = consistent.getEmail();
        String phone1 = consistent.getPhone();
        String phone2 = consistent.getPhone();

        // Verify consistency
        assertEquals("User ID should be consistent", userId1, userId2);
        assertEquals("Email should be consistent", email1, email2);
        assertEquals("Phone should be consistent", phone1, phone2);
        assertSame("Should return same reference", userId1, userId2);
        assertSame("Should return same reference", email1, email2);
    }

    /**
     * Tests complete workflow scenario.
     */
    @Test
    public void testCompleteWorkflowScenario() {
        // Create entrant representing a real accepted user
        FinalListEntrant accepted = new FinalListEntrant(
            "workflow_user_001",
            "Jennifer Wilson",
            "j.wilson@example.com",
            "+1-555-0167",
            "Accepted"
        );

        // Verify all contact details are present
        assertNotNull("User ID should be present", accepted.getUserId());
        assertNotNull("Name should be present", accepted.getName());
        assertNotNull("Email should be present for communication", accepted.getEmail());
        assertNotNull("Phone should be present for contact", accepted.getPhone());
        assertEquals("Status should be Accepted", "Accepted", accepted.getStatus());

        // Verify data can be retrieved for export
        String exportLine = String.format("%s,%s,%s,%s",
            accepted.getName(),
            accepted.getEmail(),
            accepted.getPhone(),
            accepted.getUserId()
        );

        assertTrue("Export should contain name", exportLine.contains("Jennifer Wilson"));
        assertTrue("Export should contain email", exportLine.contains("j.wilson@example.com"));
        assertTrue("Export should contain phone", exportLine.contains("+1-555-0167"));
    }

    /**
     * Tests that all constructor parameters are required and used.
     */
    @Test
    public void testAllConstructorParametersUsed() {
        String userId = "param_user";
        String name = "Param Name";
        String email = "param@email.com";
        String phone = "555-PARAM";
        String status = "Accepted";

        FinalListEntrant paramEntrant = new FinalListEntrant(userId, name, email, phone, status);

        assertNotNull("User ID should not be null", paramEntrant.getUserId());
        assertNotNull("Name should not be null", paramEntrant.getName());
        assertNotNull("Email should not be null", paramEntrant.getEmail());
        assertNotNull("Phone should not be null", paramEntrant.getPhone());
        assertNotNull("Status should not be null", paramEntrant.getStatus());
    }
}
