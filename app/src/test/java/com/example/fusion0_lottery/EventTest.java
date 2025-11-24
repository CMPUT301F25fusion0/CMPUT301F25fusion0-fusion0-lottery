package com.example.fusion0_lottery;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the Event model class.
 * Tests basic getter and setter functionality.
 */
public class EventTest {

    /**
     * Test creating an event with valid data.
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
                5
        );

        assertEquals("Test Event", event.getEventName());
        assertEquals("Sports, Music", event.getInterests());
        assertEquals("Test Description", event.getDescription());
        assertEquals("2025-01-01", event.getStartDate());
        assertEquals("2025-01-02", event.getEndDate());
        assertEquals("14:00", event.getTime());
        assertEquals(10.0, event.getPrice(), 0.01);
        assertEquals("Test Location", event.getLocation());
        assertEquals("2024-12-01", event.getRegistrationStart());
        assertEquals("2024-12-31", event.getRegistrationEnd());
        assertEquals(Integer.valueOf(100), event.getMaxEntrants());
    }

    /**
     * Test event setters work correctly.
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

        assertEquals("Updated Event", event.getEventName());
        assertEquals("Gaming", event.getInterests());
        assertEquals("Updated Description", event.getDescription());
        assertEquals(25.0, event.getPrice(), 0.01);
        assertEquals("New Location", event.getLocation());
        assertEquals(Integer.valueOf(50), event.getMaxEntrants());
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
                3
        );

        assertNull(event.getMaxEntrants());
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
    }
}
