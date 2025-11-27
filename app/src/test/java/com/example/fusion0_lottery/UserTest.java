package com.example.fusion0_lottery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class UserTest {
    private User mockUser(){
        return new User("new user","user@gmail.com","587-111-1111", "Entrant","123456");
    }

    @Test
    void testSignUp(){
        User user = mockUser();
        assertEquals("new user", user.getName());
        assertEquals("user@gmail.com", user.getEmail());
    }
    @Test
    void testUserRole(){
        User user = mockUser();
        assertEquals("Entrant", user.getRole());
    }
    @Test
    void testUserNumber(){
        User user = mockUser();
        assertEquals("587-111-1111", user.getPhone());
    }
    @Test
    void testUserDeviceId(){
        User user = mockUser();
        assertEquals("123456", user.getDeviceId());
    }
}