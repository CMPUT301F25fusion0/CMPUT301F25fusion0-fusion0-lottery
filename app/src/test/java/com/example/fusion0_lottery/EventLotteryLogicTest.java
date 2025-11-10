package com.example.fusion0_lottery;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import com.google.firebase.firestore.DocumentSnapshot;

import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Logic tests for EventLottery
 * Covers: US 01.01.01 – 01.01.04
 * These tests validate the logical methods used for waiting list eligibility and event filtering, independent of UI or Firestore.
 */
public class EventLotteryLogicTest {

    private EventLottery eventLottery;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Before
    public void setUp() {
        eventLottery = new EventLottery();
    }

    // US 01.01.01
    @Test
    public void testCanJoinWaitingList_openNotFull_returnsTrue() {
        DocumentSnapshot mockEvent = mock(DocumentSnapshot.class);
        when(mockEvent.getString("registrationEnd")).thenReturn("2099-12-31");
        when(mockEvent.getLong("maxEntrants")).thenReturn(10L);
        when(mockEvent.get("waitingList")).thenReturn(new ArrayList<String>());

        assertTrue(eventLottery.canJoinWaitingListForTest(mockEvent));
    }

    @Test
    public void testCanJoinWaitingList_lastSpotAvailable_returnsTrue() {
        DocumentSnapshot mockEvent = mock(DocumentSnapshot.class);
        List<String> waitlist = new ArrayList<>();
        waitlist.add("user1"); // 1 spot already taken
        when(mockEvent.get("waitingList")).thenReturn(waitlist);
        when(mockEvent.getLong("maxEntrants")).thenReturn(2L);
        when(mockEvent.getString("registrationEnd")).thenReturn("2099-12-31");

        assertTrue(eventLottery.canJoinWaitingListForTest(mockEvent));
    }

    // US 01.01.02
    @Test
    public void testCannotJoinWaitingList_closedOrFull_returnsFalse() {
        // Registration ended
        DocumentSnapshot mockEvent = mock(DocumentSnapshot.class);
        when(mockEvent.getString("registrationEnd")).thenReturn("2000-01-01"); // past
        when(mockEvent.getLong("maxEntrants")).thenReturn(10L);
        when(mockEvent.get("waitingList")).thenReturn(new ArrayList<String>());
        assertFalse(eventLottery.canJoinWaitingListForTest(mockEvent));

        // Waiting list full
        List<String> waitlist = new ArrayList<>();
        waitlist.add("user1");
        mockEvent = mock(DocumentSnapshot.class);
        when(mockEvent.getString("registrationEnd")).thenReturn("2099-12-31");
        when(mockEvent.getLong("maxEntrants")).thenReturn(1L);
        when(mockEvent.get("waitingList")).thenReturn(waitlist);
        assertFalse(eventLottery.canJoinWaitingListForTest(mockEvent));
    }

    //  Edge Case: Registration ends today
    @Test
    public void testCanJoinWaitingList_registrationEndsToday_returnsTrue() throws Exception {
        Calendar cal = Calendar.getInstance();
        String todayStr = sdf.format(cal.getTime());

        DocumentSnapshot mockEvent = mock(DocumentSnapshot.class);
        when(mockEvent.getString("registrationEnd")).thenReturn(todayStr);
        when(mockEvent.getLong("maxEntrants")).thenReturn(10L);
        when(mockEvent.get("waitingList")).thenReturn(new ArrayList<String>());

        assertTrue(eventLottery.canJoinWaitingListForTest(mockEvent));
    }

    // US 01.01.03
    @Test
    public void testSeeJoinableEvents_onlyIncludeOpenNotFull() {
        DocumentSnapshot event1 = mock(DocumentSnapshot.class);
        when(event1.getString("registrationEnd")).thenReturn("2099-12-31");
        when(event1.getLong("maxEntrants")).thenReturn(10L);
        when(event1.get("waitingList")).thenReturn(new ArrayList<String>());

        DocumentSnapshot event2 = mock(DocumentSnapshot.class);
        when(event2.getString("registrationEnd")).thenReturn("2000-01-01"); // past
        when(event2.getLong("maxEntrants")).thenReturn(10L);
        when(event2.get("waitingList")).thenReturn(new ArrayList<String>());

        assertTrue(eventLottery.canJoinWaitingListForTest(event1));
        assertFalse(eventLottery.canJoinWaitingListForTest(event2));
    }

    //  US 01.01.04
    @Test
    public void testFilterByInterestAndDates() {
        DocumentSnapshot mockEvent = mock(DocumentSnapshot.class);
        when(mockEvent.getString("interests")).thenReturn("Music");
        when(mockEvent.getString("startDate")).thenReturn("2099-01-01");
        when(mockEvent.getString("endDate")).thenReturn("2099-12-31");
        when(mockEvent.getString("registrationEnd")).thenReturn("2099-12-31");
        when(mockEvent.getLong("maxEntrants")).thenReturn(10L);
        when(mockEvent.get("waitingList")).thenReturn(new ArrayList<String>());

        // Matching interest & date
        eventLottery.setSelectedInterestForTest("Music");
        eventLottery.setSelectedStartDateForTest("2099-01-01");
        eventLottery.setSelectedEndDateForTest("2099-12-31");
        assertTrue(eventLottery.canJoinWaitingListForTest(mockEvent));

        // Non-matching interest
        eventLottery.setSelectedInterestForTest("Sports");
        assertTrue(eventLottery.canJoinWaitingListForTest(mockEvent));

        // Outside date range
        eventLottery.setSelectedStartDateForTest("2100-01-01");
        eventLottery.setSelectedEndDateForTest("2100-12-31");
        assertTrue(eventLottery.canJoinWaitingListForTest(mockEvent));
    }
}
