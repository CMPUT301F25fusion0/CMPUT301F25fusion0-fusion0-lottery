package com.example.fusion0_lottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;


public class QRScannerActivityTest {
    @Rule
    public ActivityScenarioRule<QRScannerActivity> scenario = new ActivityScenarioRule<>(QRScannerActivity.class);

    @Test
    public void testActivityStarts(){

    }
    @Test
    public void testCancelButton(){
        onView(withText("Cancel")).perform(click());
    }
}
