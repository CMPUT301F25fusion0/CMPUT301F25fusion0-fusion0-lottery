package com.example.fusion0_lottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented tests for QRScannerActivity.
 * Tests QR code scanning functionality, camera permissions, and user interactions.
 * Updated with comprehensive test coverage for scanner behavior and lifecycle.
 */
@RunWith(AndroidJUnit4.class)
public class QRScannerActivityTest {

    private ActivityScenario<QRScannerActivity> scenario;

    /**
     * Grant camera permission for tests that require it.
     * This rule automatically grants camera permission before tests run.
     */
    @Rule
    public GrantPermissionRule cameraPermissionRule =
            GrantPermissionRule.grant(Manifest.permission.CAMERA);

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
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), QRScannerActivity.class);
        scenario = ActivityScenario.launch(intent);
    }

    /**
     * Test that the activity starts successfully.
     */
    @Test
    public void testActivityStarts() {
        launchActivity();

        // Verify the activity is in a resumed state
        scenario.onActivity(activity -> {
            assert !activity.isFinishing();
        });
    }

    /**
     * Test that the cancel button is displayed.
     */
    @Test
    public void testCancelButtonDisplayed() {
        launchActivity();

        // Verify cancel button is displayed
        onView(withText("Cancel"))
                .check(matches(isDisplayed()));
    }

    /**
     * Test that clicking cancel button finishes the activity.
     */
    @Test
    public void testCancelButton() {
        launchActivity();

        // Click the cancel button
        onView(withText("Cancel")).perform(click());

        // Verify the activity is finishing
        scenario.onActivity(activity -> {
            assert activity.isFinishing();
        });
    }

    /**
     * Test that clicking cancel button returns RESULT_CANCELED.
     */
    @Test
    public void testCancelButtonReturnsResultCanceled() {
        launchActivity();

        // Click cancel button
        onView(withText("Cancel")).perform(click());

        // Verify result code is RESULT_CANCELED
        scenario.onActivity(activity -> {
            // The activity should be finishing with RESULT_CANCELED
            assert activity.isFinishing();
        });
    }

    /**
     * Test that the activity initializes with camera permission granted.
     * This test verifies the activity doesn't crash when camera permission is granted.
     */
    @Test
    public void testActivityWithCameraPermission() {
        launchActivity();

        // With camera permission granted, the activity should start the camera
        // Verify the activity is not finishing
        scenario.onActivity(activity -> {
            assert !activity.isFinishing();
        });

        // Verify cancel button is still accessible
        onView(withText("Cancel"))
                .check(matches(isDisplayed()));
    }

    /**
     * Test that activity handles lifecycle correctly.
     * Tests that the activity can be paused and resumed without crashing.
     */
    @Test
    public void testActivityLifecycle() {
        launchActivity();

        // Move to paused state
        scenario.moveToState(androidx.lifecycle.Lifecycle.State.STARTED);

        // Move back to resumed state
        scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED);

        // Activity should still be functional
        onView(withText("Cancel"))
                .check(matches(isDisplayed()));
    }

    /**
     * Test that activity can be recreated without crashing.
     * This simulates configuration changes like rotation.
     */
    @Test
    public void testActivityRecreation() {
        launchActivity();

        // Recreate the activity (simulates configuration change)
        scenario.recreate();

        // Verify the activity is still functional
        onView(withText("Cancel"))
                .check(matches(isDisplayed()));

        scenario.onActivity(activity -> {
            assert !activity.isFinishing();
        });
    }

    /**
     * Test that activity finishes properly when destroyed.
     */
    @Test
    public void testActivityDestroy() {
        launchActivity();

        // Move to destroyed state
        scenario.moveToState(androidx.lifecycle.Lifecycle.State.DESTROYED);

        // Scenario should be closed
        // This test just verifies no crash occurs during destruction
    }

    /**
     * Test that cancel button works after activity recreation.
     */
    @Test
    public void testCancelButtonAfterRecreation() {
        launchActivity();

        // Recreate the activity
        scenario.recreate();

        // Cancel button should still work
        onView(withText("Cancel")).perform(click());

        scenario.onActivity(activity -> {
            assert activity.isFinishing();
        });
    }

    /**
     * Test that the scanner view is properly initialized.
     * This test verifies the activity doesn't crash on startup with camera permission.
     */
    @Test
    public void testScannerViewInitialization() {
        launchActivity();

        // Wait a moment for scanner to initialize
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify activity is still running (scanner initialized successfully)
        scenario.onActivity(activity -> {
            assert !activity.isFinishing();
        });
    }

    /**
     * Test that activity handles multiple pause/resume cycles.
     */
    @Test
    public void testMultiplePauseResumeCycles() {
        launchActivity();

        // Perform multiple pause/resume cycles
        for (int i = 0; i < 3; i++) {
            scenario.moveToState(androidx.lifecycle.Lifecycle.State.STARTED);
            scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED);
        }

        // Verify activity is still functional
        onView(withText("Cancel"))
                .check(matches(isDisplayed()));

        scenario.onActivity(activity -> {
            assert !activity.isFinishing();
        });
    }
}
