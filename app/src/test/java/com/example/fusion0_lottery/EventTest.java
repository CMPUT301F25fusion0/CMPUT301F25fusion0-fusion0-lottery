package com.example.fusion0_lottery;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class EventTest {
    private Event event;

    @Before
    public void setup() {
        event = new Event(
                "test event",
                "this event is a test",
                "2025-11-06",
                "2025-11-07",
                "1:00",
                10.0,
                "test city",
                "2025-11-06",
                "2025-11-07",
                1000,
                0,
                0,
                0
        );
    }

    @Test
    public void testEvent() {
        assertEquals("test event", event.getEventName());
        assertEquals("this event is a test", event.getDescription());
        assertEquals("2025-11-06", event.getStartDate());
        assertEquals("2025-11-07", event.getEndDate());
        assertEquals("1:00", event.getTime());
        assertEquals(10.0, event.getPrice(), 0.001);
        assertEquals("test city", event.getLocation());
        assertEquals("2025-11-06", event.getRegistrationStart());
        assertEquals("2025-11-07", event.getRegistrationEnd());
        assertEquals(1000, event.getMaxEntrants().intValue());
        assertEquals(0, event.getWaitingListCount().intValue());
        assertEquals(0, event.getUserSelectedCount().intValue());
        assertEquals(0, event.getUserEnrolledCount().intValue());
    }

    @Test
    public void testSetters() {
        event.setEventName("hello");
        event.setPrice(20.0);
        event.setLocation("Earth");

        assertEquals("hello", event.getEventName());
        assertEquals(20.0, event.getPrice(), 0.001);
        assertEquals("Earth", event.getLocation());

        event.setEventName("not real");
        assertEquals("not real", event.getEventName());
    }
}
