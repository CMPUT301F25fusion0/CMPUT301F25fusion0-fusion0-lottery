package com.example.fusion0_lottery;

import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented tests for EventCreationActivity.
 * Tests UI components and user interactions.
 */
@RunWith(AndroidJUnit4.class)
public class EventCreationActivityTest {

    /**
     * Test that activity launches successfully.
     */
    @Test
    public void testActivityLaunches() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventCreationActivity.class);
        ActivityScenario<EventCreationActivity> scenario = ActivityScenario.launch(intent);

        // Verify the activity is displayed
        Espresso.onView(ViewMatchers.withId(R.id.createEventButton))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        scenario.close();
    }

    /**
     * Test that all input fields are displayed.
     */
    @Test
    public void testInputFieldsDisplayed() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventCreationActivity.class);
        ActivityScenario<EventCreationActivity> scenario = ActivityScenario.launch(intent);

        // Check that key input fields are displayed
        Espresso.onView(ViewMatchers.withId(R.id.eventNameInput))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withId(R.id.interestInput))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withId(R.id.descriptionInput))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withId(R.id.locationInput))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withId(R.id.priceInput))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        scenario.close();
    }

    /**
     * Test that buttons are displayed and clickable.
     */
    @Test
    public void testButtonsDisplayed() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventCreationActivity.class);
        ActivityScenario<EventCreationActivity> scenario = ActivityScenario.launch(intent);

        // Check create button
        Espresso.onView(ViewMatchers.withId(R.id.createEventButton))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                .check(ViewAssertions.matches(ViewMatchers.isClickable()));

        // Check cancel button
        Espresso.onView(ViewMatchers.withId(R.id.cancelButton))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                .check(ViewAssertions.matches(ViewMatchers.isClickable()));

        // Check upload poster button
        Espresso.onView(ViewMatchers.withId(R.id.uploadPosterButton))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                .check(ViewAssertions.matches(ViewMatchers.isClickable()));

        scenario.close();
    }

    /**
     * Test typing into event name input field.
     */
    @Test
    public void testEventNameInput() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventCreationActivity.class);
        ActivityScenario<EventCreationActivity> scenario = ActivityScenario.launch(intent);

        String testEventName = "Test Event Name";

        Espresso.onView(ViewMatchers.withId(R.id.eventNameInput))
                .perform(ViewActions.typeText(testEventName))
                .perform(ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withId(R.id.eventNameInput))
                .check(ViewAssertions.matches(ViewMatchers.withText(testEventName)));

        scenario.close();
    }

    /**
     * Test typing into interests input field.
     */
    @Test
    public void testInterestsInput() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventCreationActivity.class);
        ActivityScenario<EventCreationActivity> scenario = ActivityScenario.launch(intent);

        String testInterests = "Sports, Music";

        Espresso.onView(ViewMatchers.withId(R.id.interestInput))
                .perform(ViewActions.typeText(testInterests))
                .perform(ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withId(R.id.interestInput))
                .check(ViewAssertions.matches(ViewMatchers.withText(testInterests)));

        scenario.close();
    }

    /**
     * Test typing into description input field.
     */
    @Test
    public void testDescriptionInput() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventCreationActivity.class);
        ActivityScenario<EventCreationActivity> scenario = ActivityScenario.launch(intent);

        String testDescription = "Test event description";

        Espresso.onView(ViewMatchers.withId(R.id.descriptionInput))
                .perform(ViewActions.typeText(testDescription))
                .perform(ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withId(R.id.descriptionInput))
                .check(ViewAssertions.matches(ViewMatchers.withText(testDescription)));

        scenario.close();
    }

    /**
     * Test typing into location input field.
     */
    @Test
    public void testLocationInput() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventCreationActivity.class);
        ActivityScenario<EventCreationActivity> scenario = ActivityScenario.launch(intent);

        String testLocation = "Edmonton, AB";

        Espresso.onView(ViewMatchers.withId(R.id.locationInput))
                .perform(ViewActions.typeText(testLocation))
                .perform(ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withId(R.id.locationInput))
                .check(ViewAssertions.matches(ViewMatchers.withText(testLocation)));

        scenario.close();
    }

    /**
     * Test typing into price input field.
     */
    @Test
    public void testPriceInput() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventCreationActivity.class);
        ActivityScenario<EventCreationActivity> scenario = ActivityScenario.launch(intent);

        String testPrice = "25.50";

        Espresso.onView(ViewMatchers.withId(R.id.priceInput))
                .perform(ViewActions.typeText(testPrice))
                .perform(ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withId(R.id.priceInput))
                .check(ViewAssertions.matches(ViewMatchers.withText(testPrice)));

        scenario.close();
    }

    /**
     * Test that QR code checkbox is displayed.
     */
    @Test
    public void testQrCheckboxDisplayed() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventCreationActivity.class);
        ActivityScenario<EventCreationActivity> scenario = ActivityScenario.launch(intent);

        Espresso.onView(ViewMatchers.withId(R.id.generateQrCheckbox))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        scenario.close();
    }
}
