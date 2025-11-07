package com.example.fusion0_lottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import android.view.View;
import android.widget.TextView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI (instrumented) tests for the EventLottery fragment.
 *
 * Verifies that key UI components render correctly and respond to user actions.
 * These tests ensure that buttons, spinners, and date pickers appear and function
 * as expected.
 *
 * Note: This test covers fragment behavior and layout only.
 * No backend or Firestore operations are invoked here.
 */
@RunWith(AndroidJUnit4.class)
public class EventLotteryFragmentTest {

    private FragmentScenario<EventLottery> scenario;

    @Before
    public void setUp() {
        scenario = FragmentScenario.launchInContainer(
                EventLottery.class,
                null,
                R.style.Theme_Fusion0_lottery
        );
    }

    @Test
    public void testFragmentLaunchesSuccessfully() {
        onView(withId(R.id.rootLayout)).check(matches(isDisplayed()));
    }

    @Test
    public void testButtonsAreVisible() {
        onView(withId(R.id.buttonBack)).check(matches(isDisplayed()));
        onView(withId(R.id.buttonApplyFilters)).check(matches(isDisplayed()));
        onView(withId(R.id.buttonClearFilters)).check(matches(isDisplayed()));
        onView(withId(R.id.buttonStartDate)).check(matches(isDisplayed()));
        onView(withId(R.id.buttonEndDate)).check(matches(isDisplayed()));
    }

    @Test
    public void testSpinnerIsVisible() {
        onView(withId(R.id.spinnerInterest)).check(matches(isDisplayed()));
    }

    @Test
    public void testDatePickersOpen() {
        onView(withId(R.id.buttonStartDate)).perform(click());
        onView(withId(android.R.id.button1)).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).perform(click());

        onView(withId(R.id.buttonEndDate)).perform(click());
        onView(withId(android.R.id.button1)).check(matches(isDisplayed()));
    }

    @Test
    public void testApplyAndClearFiltersButtons() {
        onView(withId(R.id.buttonApplyFilters)).perform(click());
        onView(withId(R.id.buttonClearFilters)).perform(click());
    }


    // Helper Matcher for TextView content
    public static Matcher<View> withTextInTextView(final String expectedText) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("TextView with text containing: " + expectedText);
            }

            @Override
            protected boolean matchesSafely(View view) {
                if (!(view instanceof TextView)) return false;
                return ((TextView) view).getText().toString().contains(expectedText);
            }
        };
    }
}
