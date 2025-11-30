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
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;

public class BrowseImagesFragmentTest {

    @BeforeClass
    public static void signin() throws Exception {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Tasks.await(auth.signInAnonymously());
        }
        String userId = auth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, Object> userData = new HashMap<>();
        userData.put("role", "Admin");
        Tasks.await(db.collection("Users").document(userId).set(userData));
    }
    /**
     * Test that RecyclerView for images is displayed.
     */
    @Test
    public void testImagesRecyclerViewDisplayed() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent);
        Espresso.onView(ViewMatchers.withId(R.id.nav_images))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.recycler_images))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        scenario.close();
    }

    /**
     * Test that remove button is clickable.
     */
    @Test
    public void testRemoveButtonClickable() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent);
        Espresso.onView(ViewMatchers.withId(R.id.nav_images))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.remove_btn))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.remove_btn))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        scenario.close();
    }

    /**
     * Test navigation to profiles fragment.
     */
    @Test
    public void testNavigationToProfiles() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent);
        Espresso.onView(ViewMatchers.withId(R.id.nav_images))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.nav_profiles))
                .perform(ViewActions.click());
        scenario.close();
    }

    /**
     * Test navigation to events fragment.
     */
    @Test
    public void testNavigationToEvents() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent);
        Espresso.onView(ViewMatchers.withId(R.id.nav_images))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.nav_events))
                .perform(ViewActions.click());
        scenario.close();
    }

    /**
     * Test navigation to notifications fragment.
     */
    @Test
    public void testNavigationToNotifications() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent);
        Espresso.onView(ViewMatchers.withId(R.id.nav_images))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.nav_logs))
                .perform(ViewActions.click());
        scenario.close();
    }

    /**
     * Test clicking images navigation item
     */
    @Test
    public void testImagesNavigationClick() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent);
        Espresso.onView(ViewMatchers.withId(R.id.nav_images))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.nav_images))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.recycler_images))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        scenario.close();
    }
}
