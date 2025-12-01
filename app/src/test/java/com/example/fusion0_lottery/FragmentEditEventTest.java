package com.example.fusion0_lottery;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.widget.EditText;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class FragmentEditEventTest {

    private FragmentEditEvent fragment;

    // Fields used by validateInputs()
    private EditText titleInput;
    private EditText descInput;
    private EditText interestsInput;
    private EditText startDateInput;
    private EditText endDateInput;
    private EditText timeInput;
    private EditText locationInput;
    private EditText priceInput;
    private EditText maxEntrantsInput;
    private EditText lotteryCriteriaInput;

    @Before
    public void setUp() {
        fragment = new FragmentEditEvent();

        Context context = ApplicationProvider.getApplicationContext();

        // Real EditText instances (no mocking)
        titleInput = new EditText(context);
        descInput = new EditText(context);
        interestsInput = new EditText(context);
        startDateInput = new EditText(context);
        endDateInput = new EditText(context);
        timeInput = new EditText(context);
        locationInput = new EditText(context);
        priceInput = new EditText(context);
        maxEntrantsInput = new EditText(context);
        lotteryCriteriaInput = new EditText(context);

        // Three dummy EditTexts for the first 3 params of testingEdit (they are unused)
        EditText dummy1 = new EditText(context);
        EditText dummy2 = new EditText(context);
        EditText dummy3 = new EditText(context);

        // Assign all fields to the fragment using testingEdit
        fragment.testingEdit(dummy1, dummy2, dummy3, titleInput, descInput, startDateInput, endDateInput,
                timeInput, locationInput, priceInput, maxEntrantsInput, interestsInput, lotteryCriteriaInput);
    }

    /**
     * All fields filled -> validateInputs() and editThenSave() should be true.
     */
    @Test
    public void validateInputs_allFieldsFilled_returnsTrue() {
        titleInput.setText("My Event");
        descInput.setText("Some description");
        interestsInput.setText("Sports");
        startDateInput.setText("2025-11-06");
        endDateInput.setText("2025-11-07");
        timeInput.setText("12:00 PM");
        locationInput.setText("Outside");
        priceInput.setText("0.0");
        maxEntrantsInput.setText("100");
        lotteryCriteriaInput.setText("Criteria");

        assertTrue(fragment.validateInputs());
        assertTrue(fragment.editThenSave());
    }

    /**
     * One or more fields empty -> validateInputs() and editThenSave() should be false.
     */
    @Test
    public void validateInputs_someFieldsEmpty_returnsFalse() {
        // Leave at least one field empty to trigger the failure.
        titleInput.setText("");
        descInput.setText("Some description");
        interestsInput.setText("Sports");
        startDateInput.setText("2025-11-06");
        endDateInput.setText("2025-11-07");
        timeInput.setText("12:00 PM");
        locationInput.setText("Outside");
        priceInput.setText("0.0");
        maxEntrantsInput.setText("100");
        lotteryCriteriaInput.setText("Criteria");

        assertFalse(fragment.validateInputs());
        assertFalse(fragment.editThenSave());
    }

    /**
     * All fields empty -> definitely false.
     */
    @Test
    public void validateInputs_allFieldsEmpty_returnsFalse() {
        titleInput.setText("");
        descInput.setText("");
        interestsInput.setText("");
        startDateInput.setText("");
        endDateInput.setText("");
        timeInput.setText("");
        locationInput.setText("");
        priceInput.setText("");
        maxEntrantsInput.setText("");
        lotteryCriteriaInput.setText("");

        assertFalse(fragment.validateInputs());
        assertFalse(fragment.editThenSave());
    }
}
