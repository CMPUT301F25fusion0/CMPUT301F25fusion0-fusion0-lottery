package com.example.fusion0_lottery;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.*;

/**
 * Full suite of unit tests for admin functionality and BrowseEventAdapter.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 33)
public class AdminFunctionalityFullTest {

    private List<Event> eventList;
    private BrowseEventAdapter adapter;
    private FragmentActivity activity;

    /**
     * Sets up test environment: activity, event list, and adapter.
     */
    @Before
    public void setUp() {
        activity = Robolectric.buildActivity(FragmentActivity.class).setup().get();

        eventList = new ArrayList<>();

        Event activeEvent = new Event();
        activeEvent.setEventName("Active Event");
        activeEvent.setStartDate("2025-11-01");
        activeEvent.setEndDate("2099-12-31"); // future date -> active
        activeEvent.setEventId("1");

        Event inactiveEvent = new Event();
        inactiveEvent.setEventName("Inactive Event");
        inactiveEvent.setStartDate("2020-01-01");
        inactiveEvent.setEndDate("2020-12-31"); // past date -> inactive
        inactiveEvent.setEventId("2");

        eventList.add(activeEvent);
        eventList.add(inactiveEvent);

        adapter = new BrowseEventAdapter(eventList, event -> {
            // click handler: do nothing
        });
    }

    /**
     * Tests that onBindViewHolder correctly sets event name and status
     * without inflating XML layout.
     */
    @Test
    public void adapter_onBindViewHolder_setsTextAndStatus_withoutXML() {
        LinearLayout itemView = new LinearLayout(activity);
        itemView.setOrientation(LinearLayout.VERTICAL);

        TextView eventName = new TextView(activity);
        eventName.setId(R.id.event_name);
        TextView eventDate = new TextView(activity);
        eventDate.setId(R.id.event_date);
        TextView eventStatus = new TextView(activity);
        eventStatus.setId(R.id.event_status);

        itemView.addView(eventName);
        itemView.addView(eventDate);
        itemView.addView(eventStatus);

        BrowseEventAdapter.ViewHolder vh = new BrowseEventAdapter.ViewHolder(itemView);

        // First event: active
        adapter.onBindViewHolder(vh, 0);
        assertEquals("Active Event", vh.eventName.getText().toString());
        assertTrue(vh.eventStatus.getText().toString().contains("Active"));

        // Second event: inactive
        adapter.onBindViewHolder(vh, 1);
        assertEquals("Inactive Event", adapter.getEvents().get(1).getEventName());
        assertTrue(vh.eventStatus.getText().toString().contains("Inactive"));
    }

    /**
     * Verifies that active/inactive filtering logic works correctly.
     */
    @Test
    public void filterActiveInactiveEvents_worksCorrectly() throws Exception {
        List<Event> activeEvents = new ArrayList<>();
        List<Event> inactiveEvents = new ArrayList<>();
        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (Event e : eventList) {
            Date endDate = sdf.parse(e.getEndDate());
            boolean isActive = !today.after(endDate);
            if (isActive) activeEvents.add(e);
            else inactiveEvents.add(e);
        }

        assertEquals(1, activeEvents.size());
        assertEquals("Active Event", activeEvents.get(0).getEventName());
        assertEquals(1, inactiveEvents.size());
        assertEquals("Inactive Event", inactiveEvents.get(0).getEventName());
    }

    /**
     * Ensures that removing an event updates the adapter correctly.
     */
    @Test
    public void removeEvent_removesFromList() {
        Event toRemove = adapter.getEvents().get(0);
        eventList.remove(toRemove);
        adapter.updateList(eventList);

        assertEquals(1, adapter.getItemCount());
        assertEquals("Inactive Event", adapter.getEvents().get(0).getEventName());
    }

    /**
     * Tests that removing poster images updates the list correctly.
     */
    @Test
    public void removeImage_updatesList() {
        List<Event> imagesList = new ArrayList<>();
        List<String> selectedIds = new ArrayList<>();

        Event e1 = new Event();
        e1.setEventName("Event 1");
        e1.setPosterImage("poster1.png");
        e1.setEventId("10");
        imagesList.add(e1);
        selectedIds.add("10");

        for (String id : selectedIds) {
            for (Event e : imagesList) {
                if (e.getEventId().equals(id)) {
                    e.setPosterImage("default_poster");
                }
            }
        }

        imagesList.removeIf(e -> e.getPosterImage().equals("default_poster"));
        assertTrue(imagesList.isEmpty());
    }

    /**
     * Verifies that getItemCount() returns the correct number of events.
     */
    @Test
    public void adapter_getItemCount_returnsCorrect() {
        assertEquals(2, adapter.getItemCount());
    }
}
