package com.example.fusion0_lottery;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the Event model class.
 * Tests basic getter and setter functionality including lottery criteria.
 */
public class EventTest {

    /**
     * Test creating an event with valid data including lottery criteria.
     */
    @Test
    public void testEventCreation() {
        Event event = new Event(
                "Test Event",
                "Sports, Music",
                "Test Description",
                "2025-01-01",
                "2025-01-02",
                "14:00",
                10.0,
                "Test Location",
                "2024-12-01",
                "2024-12-31",
                100,
                1,
                1,
                1,
                5,
                "Must be 18+ and have basic programming skills"
        );

        // Basic event details
        assertEquals("Test Event", event.getEventName());
        assertEquals("Sports, Music", event.getInterests());
        assertEquals("Test Description", event.getDescription());
        assertEquals("2025-01-01", event.getStartDate());
        assertEquals("2025-01-02", event.getEndDate());
        assertEquals("14:00", event.getTime());
        assertEquals(10.0, event.getPrice(), 0.01);
        assertEquals("Test Location", event.getLocation());

        // Registration and capacity
        assertEquals("2024-12-01", event.getRegistrationStart());
        assertEquals("2024-12-31", event.getRegistrationEnd());
        assertEquals(Integer.valueOf(100), event.getMaxEntrants());

        // Lottery criteria
        assertEquals("Must be 18+ and have basic programming skills", event.getLotteryCriteria());
    }

    /**
     * Test event setters work correctly including lottery criteria.
     */
    @Test
    public void testEventSetters() {
        Event event = new Event();

        event.setEventName("Updated Event");
        event.setInterests("Gaming");
        event.setDescription("Updated Description");
        event.setPrice(25.0);
        event.setLocation("New Location");
        event.setMaxEntrants(50);
        event.setLotteryCriteria("Must complete beginner coding challenge");

        assertEquals("Updated Event", event.getEventName());
        assertEquals("Gaming", event.getInterests());
        assertEquals("Updated Description", event.getDescription());
        assertEquals(25.0, event.getPrice(), 0.01);
        assertEquals("New Location", event.getLocation());
        assertEquals(Integer.valueOf(50), event.getMaxEntrants());
        assertEquals("Must complete beginner coding challenge", event.getLotteryCriteria());
    }

    /**
     * Test event ID setter and getter.
     */
    @Test
    public void testEventId() {
        Event event = new Event();
        event.setEventId("event123");

        assertEquals("event123", event.getEventId());
    }

    /**
     * Test poster URL setter and getter.
     */
    @Test
    public void testPosterUrl() {
        Event event = new Event();
        event.setPosterUrl("https://example.com/poster.jpg");

        assertEquals("https://example.com/poster.jpg", event.getPosterUrl());
    }

    /**
     * Test QR code URL setter and getter.
     */
    @Test
    public void testQrCodeUrl() {
        Event event = new Event();
        event.setQrCodeUrl("https://example.com/qr.png");

        assertEquals("https://example.com/qr.png", event.getQrCodeUrl());
    }

    /**
     * Test event with null max entrants (unlimited).
     */
    @Test
    public void testEventWithNullMaxEntrants() {
        Event event = new Event(
                "Test Event",
                "Sports",
                "Description",
                "2025-01-01",
                "2025-01-02",
                "14:00",
                0.0,
                "Location",
                "2024-12-01",
                "2024-12-31",
                null,
                1,
                1,
                1,
                3,
                "Must be 18+"
        );

        assertNull(event.getMaxEntrants());
        assertEquals("Must be 18+", event.getLotteryCriteria());
    }

    /**
     * Test empty event constructor.
     */
    @Test
    public void testEmptyConstructor() {
        Event event = new Event();

        assertNull(event.getEventName());
        assertNull(event.getInterests());
        assertNull(event.getEventId());
        assertNull(event.getLotteryCriteria());
    }

    /**
     * Test requiresGeolocation setter and getter.
     */
    @Test
    public void testRequiresGeolocation() {
        Event event = new Event();

        // Default should be false
        assertFalse(event.isRequiresGeolocation());

        // Set to true
        event.setRequiresGeolocation(true);
        assertTrue(event.isRequiresGeolocation());

        // Set to false
        event.setRequiresGeolocation(false);
        assertFalse(event.isRequiresGeolocation());
    }

    /**
     * Test event creation with geolocation requirement.
     */
    @Test
    public void testEventWithGeolocationRequirement() {
        Event event = new Event(
                "Test Event with Location",
                "Sports",
                "Event requiring location",
                "2025-01-01",
                "2025-01-02",
                "14:00",
                10.0,
                "Test Location",
                "2024-12-01",
                "2024-12-31",
                100,
                1,
                1,
                1,
                5,
                "Location required for entry"
        );

        // Set geolocation requirement
        event.setRequiresGeolocation(true);

        // Verify
        assertEquals("Test Event with Location", event.getEventName());
        assertEquals("Location required for entry", event.getLotteryCriteria());
        assertTrue(event.isRequiresGeolocation());
    }

    /**
     * Test event without geolocation requirement.
     */
    @Test
    public void testEventWithoutGeolocationRequirement() {
        Event event = new Event();
        event.setEventName("Non-Location Event");
        event.setRequiresGeolocation(false);

        assertEquals("Non-Location Event", event.getEventName());
        assertFalse(event.isRequiresGeolocation());
    }
}
