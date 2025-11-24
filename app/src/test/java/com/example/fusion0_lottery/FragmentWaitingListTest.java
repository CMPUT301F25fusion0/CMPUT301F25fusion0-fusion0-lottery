package com.example.fusion0_lottery;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class FragmentWaitingListTest {

    private FragmentWaitingList fragment;
    private ArrayList<WaitingListEntrants> sampleList;

    @Before
    public void setUp() {
        fragment = new FragmentWaitingList();
        sampleList = new ArrayList<>();
        sampleList.add(new WaitingListEntrants("Test1", "2025-01-01", "Pending"));
        sampleList.add(new WaitingListEntrants("Test2", "2025-01-02", "Approved"));
    }

    @Test
    public void testDisplayWaitingListInfo() {
        ArrayList<String> displayStrings = fragment.getDisplayStrings(sampleList);

        assertTrue(displayStrings.get(0).contains("Name: Test1"));
        assertTrue(displayStrings.get(0).contains("Joined: 2025-01-01"));
        assertTrue(displayStrings.get(0).contains("Status: Pending"));
        assertFalse(displayStrings.get(0).contains("Name: Test2"));

        assertTrue(displayStrings.get(1).contains("Name: Test2"));
        assertTrue(displayStrings.get(1).contains("Joined: 2025-01-02"));
        assertTrue(displayStrings.get(1).contains("Status: Approved"));
        assertFalse(displayStrings.get(1).contains("Joined: 2025-01-03"));
        assertFalse(displayStrings.get(1).contains("Status: Pending"));

    }

    @Test
    public void testEmptyWaitingList() {
        ArrayList<WaitingListEntrants> emptyList = new ArrayList<>();
        ArrayList<String> displayStrings = fragment.getDisplayStrings(emptyList);
        assertTrue(displayStrings.isEmpty());
    }
}
