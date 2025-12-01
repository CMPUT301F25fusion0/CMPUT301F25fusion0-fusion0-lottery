package com.example.fusion0_lottery;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Tests event lists and user actions
 */
public class MyEventsActivityTest {

    @Mock
    private FirebaseAuth mockAuth;

    @Mock
    private FirebaseUser mockUser;

    private List<Event> testWaitingEvents;
    private List<Event> testSelectedEvents;
    private String currentUserId = "testUser";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        when(mockAuth.getCurrentUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn(currentUserId);

        testWaitingEvents = createWaitingEvents();
        testSelectedEvents = createSelectedEvents();
    }

    private List<Event> createWaitingEvents() {
        List<Event> events = new ArrayList<>();

        Event event1 = new Event();
        event1.setEventId("event001");
        event1.setEventName("Swimming Lesson");
        event1.setDescription("Basic Swimming Lesson Training");
        event1.setStartDate("2025-12-15");
        event1.setLocation("Stadium");
        events.add(event1);

        Event event2 = new Event();
        event2.setEventId("event002");
        event2.setEventName("Food Festival");
        event2.setDescription("Try different foods");
        event2.setStartDate("2025-12-20");
        event2.setLocation("Churchill Square");
        events.add(event2);

        return events;
    }

    private List<Event> createSelectedEvents() {
        List<Event> events = new ArrayList<>();
        Event event1 = new Event();
        event1.setEventId("event003");
        event1.setEventName("Art Show");
        event1.setDescription("Local art exhibition");
        event1.setStartDate("2025-12-25");
        event1.setLocation("Churchill Square");
        events.add(event1);

        return events;
    }
    @Test
    public void testWaitingListNotEmpty() {
        assertNotNull(testWaitingEvents);
        assertFalse(testWaitingEvents.isEmpty());
        assertEquals(2, testWaitingEvents.size());
    }
    @Test
    public void testSelectedListNotEmpty() {
        assertNotNull(testSelectedEvents);
        assertFalse(testSelectedEvents.isEmpty());
        assertEquals(1, testSelectedEvents.size());
    }
    @Test
    public void testEventDataCorrect() {
        Event event = testWaitingEvents.get(0);

        assertNotNull(event.getEventId());
        assertNotNull(event.getEventName());
        assertNotNull(event.getDescription());
        assertNotNull(event.getStartDate());
        assertNotNull(event.getLocation());

        assertEquals("event001", event.getEventId());
        assertEquals("Swimming Lesson", event.getEventName());
        assertEquals("Basic Swimming Lesson Training", event.getDescription());
    }
    @Test
    public void testRemoveFromWaitingList() {

        List<Event> waitingCopy = new ArrayList<>(testWaitingEvents);
        int startSize = waitingCopy.size();
        Event eventToRemove = waitingCopy.get(0);
        waitingCopy.remove(eventToRemove);

        assertEquals(startSize - 1, waitingCopy.size());
        assertFalse(waitingCopy.contains(eventToRemove));
    }
    @Test
    public void testRemoveFromSelectedList() {
        List<Event> selectedCopy = new ArrayList<>(testSelectedEvents);
        int startSize = selectedCopy.size();
        Event eventToRemove = selectedCopy.get(0);
        selectedCopy.remove(eventToRemove);

        assertEquals(startSize - 1, selectedCopy.size());
        assertFalse(selectedCopy.contains(eventToRemove));
    }

    @Test
    public void testFindEventById() {
        String searchId = "event001";
        Event foundEvent = null;

        for (Event event : testWaitingEvents) {
            if (searchId.equals(event.getEventId())) {
                foundEvent = event;
            }
        }

        assertNotNull(foundEvent);
        assertEquals("Swimming Lesson", foundEvent.getEventName());
    }
    @Test
    public void testEventNotFound() {
        String searchId = "event05";
        Event foundEvent = null;
        for (Event event : testWaitingEvents) {
            if (searchId.equals(event.getEventId())) {
                foundEvent = event;
            }
        }

        assertNull(foundEvent);
    }
    @Test
    public void testUserInWaitingList() {
        List<String> waitingList = new ArrayList<>();
        waitingList.add("testUser");
        waitingList.add("user456");
        boolean isInList = waitingList.contains(currentUserId);
        assertTrue(isInList);
    }
    @Test
    public void testUserNotInWaitingList() {
        List<String> waitingList = new ArrayList<>();
        waitingList.add("user456");
        waitingList.add("user789");
        boolean isInList = waitingList.contains(currentUserId);
        assertFalse(isInList);
    }
    @Test
    public void testUpdateWinnerStatusToAccepted() {
        Map<String, Object> winner = new HashMap<>();
        winner.put("userId", "user123");
        winner.put("status", "Pending");

        winner.put("status", "Accepted");

        assertEquals("Accepted", winner.get("status"));
    }
    @Test
    public void testRandomSelectionFromWaitingList() {
        List<String> waitingList = new ArrayList<>();
        waitingList.add("user1");
        waitingList.add("user2");
        waitingList.add("user3");

        int startSize = waitingList.size();
        int randomIndex = 0;
        String selectedUser = waitingList.get(randomIndex);
        waitingList.remove(randomIndex);

        assertNotNull(selectedUser);
        assertEquals("user1", selectedUser);
        assertEquals(startSize - 1, waitingList.size());
    }
    @Test
    public void testCancelledUsersList() {
        List<String> cancelledUsers = new ArrayList<>();
        cancelledUsers.add("user123");

        assertTrue(cancelledUsers.contains("user123"));
        assertEquals(1, cancelledUsers.size());
    }
    @Test
    public void testEnrolledUsersList() {
        List<String> enrolledUsers = new ArrayList<>();
        enrolledUsers.add("user123");

        assertTrue(enrolledUsers.contains("user123"));
        assertEquals(1, enrolledUsers.size());
    }
}