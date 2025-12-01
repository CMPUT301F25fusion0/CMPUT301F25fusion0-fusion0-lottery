package com.example.fusion0_lottery;

import static org.junit.Assert.*;
import org.junit.Test;


/**
 * Unit tests for FragmentSignUp class
 */
public class FragmentSignUpTest {

    @Test
    public void testValidation() {
        String name = "User";
        String email = "user@gmail.com";
        boolean isAdmin = false;
        String code = "";

        boolean isValid = !name.isEmpty() && !email.isEmpty() && !(isAdmin && code.isEmpty());
        assertTrue("Regular user should be valid", isValid);
    }

    @Test
    public void testEmptyNameFails() {
        String name = "";
        String email = "user@gmail.com";
        boolean isAdmin = false;
        String code = "";

        boolean isValid = !name.isEmpty() && !email.isEmpty() && !(isAdmin && code.isEmpty());
        assertFalse("Empty name should fail", isValid);
    }

    @Test
    public void testAdminNeedsCode() {
        String name = "Admin";
        String email = "admin@ualberta.ca";
        boolean isAdmin = true;
        String code = "";

        boolean isValid = !name.isEmpty() && !email.isEmpty() && !(isAdmin && code.isEmpty());
        assertFalse("Admin without code should fail", isValid);
    }

    @Test
    public void testUser() {
        User user = new User();
        user.setName("Test User");
        assertNotNull(user);
        assertEquals("Test User", user.getName());
    }
}