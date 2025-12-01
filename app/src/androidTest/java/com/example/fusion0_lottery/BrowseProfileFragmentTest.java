package com.example.fusion0_lottery;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;

public class BrowseProfileFragmentTest {
    @BeforeClass
    public static void signin() throws Exception {

        if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
            FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext());
        }

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
     * Test that RecyclerView is displayed.
     */
    @Test
    public void testRecyclerViewDisplayed() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent);
        Espresso.onView(ViewMatchers.withId(R.id.nav_profiles))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.recyclerView_profiles))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        scenario.close();
    }


    /**
     * Test that role filter spinner is displayed.
     */
    @Test
    public void testRoleSpinnerDisplayed() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent);
        Espresso.onView(ViewMatchers.withId(R.id.nav_profiles))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.spinnerRole))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        scenario.close();
    }

    /**
     * Test that apply filter button is displayed.
     */
    @Test
    public void testApplyFilterButtonDisplayed() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent);
        Espresso.onView(ViewMatchers.withId(R.id.nav_profiles))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.applyFilter))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        scenario.close();
    }

    /**
     * Test that bottom navigation is displayed.
     */
    @Test
    public void testBottomNavigationDisplayed() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent);
        Espresso.onView(ViewMatchers.withId(R.id.nav_profiles))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.bottom_navigation))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        scenario.close();
    }

    /**
     * Test that all bottom navigation items are present.
     */
    @Test
    public void testBottomNavigationItems() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent);

        Espresso.onView(ViewMatchers.withId(R.id.nav_profiles))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.nav_profiles))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withId(R.id.nav_events))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withId(R.id.nav_images))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withId(R.id.nav_logs))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        scenario.close();
    }

    /**
     * Test clicking apply filter button.
     */
    @Test
    public void testApplyFilterButtonClick() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent);

        Espresso.onView(ViewMatchers.withId(R.id.nav_profiles))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.applyFilter))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.recyclerView_profiles))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        scenario.close();
    }

    /**
     * Test navigation to events fragment.
     */
    @Test
    public void testNavigationToEvents() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent);

        Espresso.onView(ViewMatchers.withId(R.id.nav_profiles))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.nav_events))
                .perform(ViewActions.click());
        scenario.close();
    }

    /**
     * Test navigation to images fragment.
     */
    @Test
    public void testNavigationToImages() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent);

        Espresso.onView(ViewMatchers.withId(R.id.nav_profiles))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.nav_images))
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

        Espresso.onView(ViewMatchers.withId(R.id.nav_profiles))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.nav_logs))
                .perform(ViewActions.click());
        scenario.close();
    }
    /**
     * Test clicking on user opens details dialog
     */
    @Test
    public void testUserClickOpensDialog() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent);

        Espresso.onView(ViewMatchers.withId(R.id.nav_profiles))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.recyclerView_profiles))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, ViewActions.click()));
        Espresso.onView(ViewMatchers.withText("User Details"))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        scenario.close();
    }

    /**
     * Test Remove button exists in details dialog
     */
    @Test
    public void testRemoveButtonInDialog() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent);

        Espresso.onView(ViewMatchers.withId(R.id.nav_profiles))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.recyclerView_profiles))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, ViewActions.click()));
        Espresso.onView(ViewMatchers.withText("Remove"))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        scenario.close();
    }
    /**
     * Test click Remove opens confirmation
     */
    @Test
    public void testRemoveOpensConfirmation() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent);

        Espresso.onView(ViewMatchers.withId(R.id.nav_profiles))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.recyclerView_profiles))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, ViewActions.click()));
        Espresso.onView(ViewMatchers.withText("Remove"))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withText("Remove User?"))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        scenario.close();
    }
    /**
     * Test cancel deletion
     */
    @Test
    public void testCancelDeletion() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent);

        Espresso.onView(ViewMatchers.withId(R.id.nav_profiles))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.recyclerView_profiles))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, ViewActions.click()));
        Espresso.onView(ViewMatchers.withText("Remove"))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withText("Cancel"))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.recyclerView_profiles))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        scenario.close();
    }
}
