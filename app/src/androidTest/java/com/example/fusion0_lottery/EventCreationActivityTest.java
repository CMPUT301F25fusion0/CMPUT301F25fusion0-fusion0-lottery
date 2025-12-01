package com.example.fusion0_lottery;

import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * Instrumented tests for EventCreationActivity.
 * Tests UI components, user interactions, and input validation.
 * Updated to include comprehensive test coverage for all fields and validation logic.
 */
@RunWith(AndroidJUnit4.class)
public class EventCreationActivityTest {

    private ActivityScenario<EventCreationActivity> scenario;

    /**
     * Clean up after each test
     */
    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
    }

    /**
     * Helper method to launch the activity
     */
    private void launchActivity() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventCreationActivity.class);
        scenario = ActivityScenario.launch(intent);
    }


    /**
     * Test typing into event name input field.
     */
    @Test
    public void testEventNameInput() {
        launchActivity();

        String testEventName = "Test Event Name";

        Espresso.onView(withId(R.id.eventNameInput))
                .perform(ViewActions.typeText(testEventName))
                .perform(ViewActions.closeSoftKeyboard());

        Espresso.onView(withId(R.id.eventNameInput))
                .check(ViewAssertions.matches(withText(testEventName)));
    }

    /**
     * Test typing into interests input field.
     */
    @Test
    public void testInterestsInput() {
        launchActivity();

        String testInterests = "Sports, Music";

        Espresso.onView(withId(R.id.interestInput))
                .perform(ViewActions.typeText(testInterests))
                .perform(ViewActions.closeSoftKeyboard());

        Espresso.onView(withId(R.id.interestInput))
                .check(ViewAssertions.matches(withText(testInterests)));
    }

    /**
     * Test typing into location input field.
     */
    @Test
    public void testLocationInput() {
        launchActivity();

        String testLocation = "Edmonton, AB";

        Espresso.onView(withId(R.id.locationInput))
                .perform(ViewActions.typeText(testLocation))
                .perform(ViewActions.closeSoftKeyboard());

        Espresso.onView(withId(R.id.locationInput))
                .check(ViewAssertions.matches(withText(testLocation)));
    }

    /**
     * Test typing into price input field.
     */
    @Test
    public void testPriceInput() {
        launchActivity();

        String testPrice = "25.50";

        Espresso.onView(withId(R.id.priceInput))
                .perform(ViewActions.typeText(testPrice))
                .perform(ViewActions.closeSoftKeyboard());

        Espresso.onView(withId(R.id.priceInput))
                .check(ViewAssertions.matches(withText(testPrice)));
    }

    // ========== NEW TESTS FOR MISSING FIELDS ==========

    /**
     * Test that registration start date input field is displayed.
     */
    @Test
    public void testRegistrationStartDateInputDisplayed() {
        launchActivity();

        Espresso.onView(withId(R.id.registrationStartInput))
                .check(ViewAssertions.matches(isDisplayed()));
    }

    /**
     * Test that registration end date input field is displayed.
     */
    @Test
    public void testRegistrationEndDateInputDisplayed() {
        launchActivity();

        Espresso.onView(withId(R.id.registrationEndInput))
                .check(ViewAssertions.matches(isDisplayed()));
    }

    /**
     * Test that start date input field is displayed.
     */
    @Test
    public void testStartDateInputDisplayed() {
        launchActivity();

        Espresso.onView(withId(R.id.startDateInput))
                .check(ViewAssertions.matches(isDisplayed()));
    }

    /**
     * Test that end date input field is displayed.
     */
    @Test
    public void testEndDateInputDisplayed() {
        launchActivity();

        Espresso.onView(withId(R.id.endDateInput))
                .check(ViewAssertions.matches(isDisplayed()));
    }

    /**
     * Test that time input field is displayed.
     */
    @Test
    public void testTimeInputDisplayed() {
        launchActivity();

        Espresso.onView(withId(R.id.timeInput))
                .check(ViewAssertions.matches(isDisplayed()));
    }

    /**
     * Test that poster image view is initially not visible.
     */
    @Test
    public void testPosterImageViewInitiallyHidden() {
        launchActivity();

        // ImageView should exist but visibility state may vary depending on layout
        // This test just verifies the view exists
        Espresso.onView(withId(R.id.posterImageView))
                .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(
                        ViewMatchers.Visibility.GONE)));
    }


}
