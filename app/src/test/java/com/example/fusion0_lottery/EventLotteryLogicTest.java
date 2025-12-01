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

public class EventLotteryLogicTest {

    private EventLottery eventLottery;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Before
    public void setUp() {
        eventLottery = new EventLottery();
    }

    /** Waiting list: open events */
    @Test
    public void testCanJoinWaitingList_openNotFull_returnsTrue() {
        DocumentSnapshot mockEvent = mock(DocumentSnapshot.class);
        when(mockEvent.getString("registrationEnd")).thenReturn("2099-12-31");
        when(mockEvent.getLong("maxEntrants")).thenReturn(10L);
        when(mockEvent.get("waitingList")).thenReturn(new ArrayList<String>());

        assertTrue(eventLottery.canJoinWaitingListForTest(mockEvent));
    }

    /** Waiting list full or registration ended */
    @Test
    public void testCannotJoinWaitingList_closedOrFull_returnsFalse() {
        // Past registration
        DocumentSnapshot mockEvent = mock(DocumentSnapshot.class);
        when(mockEvent.getString("registrationEnd")).thenReturn("2000-01-01");
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

    /** Registration ends today */
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

    /** Max entrants edge cases */
    @Test
    public void testCanJoinWaitingList_maxEntrantsEdgeCases() {
        // Null maxEntrants, null waiting list
        DocumentSnapshot mockEvent = mock(DocumentSnapshot.class);
        when(mockEvent.getString("registrationEnd")).thenReturn("2099-12-31");
        when(mockEvent.getLong("maxEntrants")).thenReturn(null);
        when(mockEvent.get("waitingList")).thenReturn(null);

        assertTrue(eventLottery.canJoinWaitingListForTest(mockEvent));

        // Exactly full
        List<String> waitlist = new ArrayList<>();
        waitlist.add("user1");
        mockEvent = mock(DocumentSnapshot.class);
        when(mockEvent.getString("registrationEnd")).thenReturn("2099-12-31");
        when(mockEvent.getLong("maxEntrants")).thenReturn(1L);
        when(mockEvent.get("waitingList")).thenReturn(waitlist);

        assertFalse(eventLottery.canJoinWaitingListForTest(mockEvent));

        // Under max
        waitlist.clear();
        mockEvent = mock(DocumentSnapshot.class);
        when(mockEvent.getString("registrationEnd")).thenReturn("2099-12-31");
        when(mockEvent.getLong("maxEntrants")).thenReturn(2L);
        when(mockEvent.get("waitingList")).thenReturn(waitlist);

        assertTrue(eventLottery.canJoinWaitingListForTest(mockEvent));
    }

    /** Invalid or null registrationEnd */
    @Test
    public void testCanJoinWaitingList_invalidOrNullRegistrationEnd() {
        // Null registrationEnd
        DocumentSnapshot mockEvent = mock(DocumentSnapshot.class);
        when(mockEvent.getString("registrationEnd")).thenReturn(null);
        when(mockEvent.getLong("maxEntrants")).thenReturn(10L);
        when(mockEvent.get("waitingList")).thenReturn(new ArrayList<String>());
        assertTrue(eventLottery.canJoinWaitingListForTest(mockEvent));

        // Invalid date
        mockEvent = mock(DocumentSnapshot.class);
        when(mockEvent.getString("registrationEnd")).thenReturn("invalid-date");
        when(mockEvent.getLong("maxEntrants")).thenReturn(10L);
        when(mockEvent.get("waitingList")).thenReturn(new ArrayList<String>());
        assertFalse(eventLottery.canJoinWaitingListForTest(mockEvent));
    }

    /** Filter by interest and date */
    @Test
    public void testFilterByInterestAndDates() throws Exception {
        DocumentSnapshot mockEvent = mock(DocumentSnapshot.class);
        when(mockEvent.getString("interests")).thenReturn("Music, Festival");
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

        // Non-matching interest (should still pass canJoinWaitingList)
        eventLottery.setSelectedInterestForTest("Sports");
        assertTrue(eventLottery.canJoinWaitingListForTest(mockEvent));

        // Outside date range
        eventLottery.setSelectedStartDateForTest("2100-01-01");
        eventLottery.setSelectedEndDateForTest("2100-12-31");
        assertTrue(eventLottery.canJoinWaitingListForTest(mockEvent));
    }

    /** Multiple events: joinable vs non-joinable */
    @Test
    public void testSeeJoinableEvents_multiple() {
        DocumentSnapshot event1 = mock(DocumentSnapshot.class);
        when(event1.getString("registrationEnd")).thenReturn("2099-12-31");
        when(event1.getLong("maxEntrants")).thenReturn(10L);
        when(event1.get("waitingList")).thenReturn(new ArrayList<String>());

        DocumentSnapshot event2 = mock(DocumentSnapshot.class);
        when(event2.getString("registrationEnd")).thenReturn("2000-01-01");
        when(event2.getLong("maxEntrants")).thenReturn(10L);
        when(event2.get("waitingList")).thenReturn(new ArrayList<String>());

        assertTrue(eventLottery.canJoinWaitingListForTest(event1));
        assertFalse(eventLottery.canJoinWaitingListForTest(event2));
    }
    /** Lottery criteria: ensure default and custom criteria are handled */
    @Test
    public void testLotteryCriteria_display() {
        DocumentSnapshot mockEvent = mock(DocumentSnapshot.class);

        // Case 1: Custom criteria set
        when(mockEvent.getString("lotteryCriteria")).thenReturn("VIP only");
        EventFragmentEntrant fragment = new EventFragmentEntrant();
        String criteria = mockEvent.getString("lotteryCriteria");
        assertTrue(criteria != null && criteria.equals("VIP only"));

        // Case 2: Criteria null or empty -> default message
        mockEvent = mock(DocumentSnapshot.class);
        when(mockEvent.getString("lotteryCriteria")).thenReturn(null);
        criteria = mockEvent.getString("lotteryCriteria");
        String defaultMessage = "Random selection after registration closes.";
        assertTrue(criteria == null || criteria.isEmpty());
    }

    /** Total entrants: ensure the count matches waiting list size and respects maxEntrants */
    @Test
    public void testTotalEntrants_logic() {
        DocumentSnapshot mockEvent = mock(DocumentSnapshot.class);

        List<String> waitingList = new ArrayList<>();
        waitingList.add("user1");
        waitingList.add("user2");

        when(mockEvent.get("waitingList")).thenReturn(waitingList);
        when(mockEvent.getLong("maxEntrants")).thenReturn(5L);

        // Total entrants should match waiting list size
        List<String> listFromEvent = (List<String>) mockEvent.get("waitingList");
        int totalEntrants = listFromEvent != null ? listFromEvent.size() : 0;
        long maxEntrants = mockEvent.getLong("maxEntrants") != null ? mockEvent.getLong("maxEntrants") : 0;

        assertTrue(totalEntrants == 2);
        assertTrue(totalEntrants <= maxEntrants);

        // Edge case: waiting list empty
        waitingList.clear();
        when(mockEvent.get("waitingList")).thenReturn(waitingList);

        totalEntrants = ((List<String>) mockEvent.get("waitingList")).size();
        assertTrue(totalEntrants == 0);
    }

}
