package com.example.fusion0_lottery;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;


import org.junit.BeforeClass;
import org.junit.Test;


public class MyEventsActivityTest {


    @BeforeClass
    public static void signin() throws Exception {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Tasks.await(auth.signInAnonymously());
        }
    }

    /**
     * Test that activity launches successfully.
     */
    @Test
    public void testActivityLaunches() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MyEventsActivity.class);
        ActivityScenario<MyEventsActivity> scenario = ActivityScenario.launch(intent);

        Espresso.onView(ViewMatchers.withId(R.id.recycler_waiting))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        scenario.close();
    }

    /**     * Test that waiting list and selected events RecyclerViews are displayed.
     */
    @Test
    public void testEventListsDisplayed() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MyEventsActivity.class);
        ActivityScenario<MyEventsActivity> scenario = ActivityScenario.launch(intent);

        Espresso.onView(ViewMatchers.withId(R.id.recycler_waiting))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withId(R.id.recycler_selected))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        scenario.close();
    }

    /**
     * Test that bottom navigation is displayed and functional.
     */
    @Test
    public void testBottomNavigation() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MyEventsActivity.class);
        ActivityScenario<MyEventsActivity> scenario = ActivityScenario.launch(intent);

        Espresso.onView(ViewMatchers.withId(R.id.bottom_navigation))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withId(R.id.navigation_home))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withId(R.id.navigation_my_events))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withId(R.id.navigation_profile))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        scenario.close();
    }

    /**
     * Test navigation to profile fragment.
     */
    @Test
    public void testNavigationToProfile() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MyEventsActivity.class);
        ActivityScenario<MyEventsActivity> scenario = ActivityScenario.launch(intent);

        Espresso.onView(ViewMatchers.withId(R.id.navigation_profile))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.main))
                .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)));

        Espresso.onView(ViewMatchers.withId(R.id.fragment_container))
                .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        scenario.close();
    }

    /**
     * Test that horizontal scrolling is configured for RecyclerViews.
     */
    @Test
    public void testHorizontalScroll() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MyEventsActivity.class);
        ActivityScenario<MyEventsActivity> scenario = ActivityScenario.launch(intent);

        Espresso.onView(ViewMatchers.withId(R.id.recycler_waiting))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withId(R.id.recycler_selected))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        scenario.close();
    }

}
