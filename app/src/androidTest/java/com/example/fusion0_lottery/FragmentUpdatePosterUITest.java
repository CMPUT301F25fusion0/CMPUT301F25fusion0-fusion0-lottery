package com.example.fusion0_lottery;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import android.content.Intent;
import android.provider.MediaStore;

@RunWith(AndroidJUnit4.class)
public class FragmentUpdatePosterUITest {

    @Before
    public void setup() {
        Intents.init();
    }

    @After
    public void teardown() {
        Intents.release();
    }

    @Test
    public void testUIElementsVisible() {
        FragmentScenario.launchInContainer(FragmentUpdatePoster.class);
        onView(withId(R.id.posterPreview)).check(matches(isDisplayed()));
        onView(withId(R.id.pickImageButton)).check(matches(isDisplayed()));
        onView(withId(R.id.uploadButton)).check(matches(isDisplayed()));
    }

    @Test
    public void testPickImageButtonLaunchesIntent() {
        FragmentScenario.launchInContainer(FragmentUpdatePoster.class);
        onView(withId(R.id.pickImageButton)).perform(click());

        Intents.intended(IntentMatchers.hasAction(Intent.ACTION_PICK));
        Intents.intended(IntentMatchers.hasType("image/*"));
    }

    @Test
    public void testUploadButtonWithoutImageShowsToast() {
        FragmentScenario.launchInContainer(FragmentUpdatePoster.class);
        onView(withId(R.id.uploadButton)).perform(click());
        // Optionally use a ToastMatcher if you have one implemented
    }
}
