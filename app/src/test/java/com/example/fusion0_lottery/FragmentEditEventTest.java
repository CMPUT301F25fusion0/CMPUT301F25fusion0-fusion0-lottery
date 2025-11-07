package com.example.fusion0_lottery;

import static org.junit.Assert.*;
import android.text.Editable;
import static org.mockito.Mockito.*;
import android.widget.EditText;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class FragmentEditEventTest {

    private FragmentEditEvent fragment;
    private EditText title, desc, interests, startDate, endDate, time, location, price, max;

    @Before
    public void setUp() {
        fragment = new FragmentEditEvent();

        // Mock EditText inputs
        title = Mockito.mock(EditText.class);
        desc = Mockito.mock(EditText.class);
        interests = Mockito.mock(EditText.class);
        startDate = Mockito.mock(EditText.class);
        endDate = Mockito.mock(EditText.class);
        time = Mockito.mock(EditText.class);
        location = Mockito.mock(EditText.class);
        price = Mockito.mock(EditText.class);
        max = Mockito.mock(EditText.class);

        fragment.testingEdit(title, desc, startDate, endDate, time, location, price, max, interests);
    }


    @Test
    public void testEdit() {
        Editable titleEditable = mock(Editable.class),
                descEditable = mock(Editable.class),
                interestsEditable = mock(Editable.class),
                startDateEditable = mock(Editable.class),
                endDateEditable = mock(Editable.class),
                timeEditable = mock(Editable.class),
                locationEditable = mock(Editable.class),
                priceEditable = mock(Editable.class),
                maxEditable = mock(Editable.class);


        when(titleEditable.toString()).thenReturn("Random");
        when(title.getText()).thenReturn(titleEditable);

        when(descEditable.toString()).thenReturn("This is a description");
        when(desc.getText()).thenReturn(descEditable);

        when(interestsEditable.toString()).thenReturn("Sports");
        when(interests.getText()).thenReturn(interestsEditable);

        when(startDateEditable.toString()).thenReturn("2025-11-06");
        when(startDate.getText()).thenReturn(startDateEditable);

        when(endDateEditable.toString()).thenReturn("2025-11-07");
        when(endDate.getText()).thenReturn(endDateEditable);

        when(timeEditable.toString()).thenReturn("12:00 PM");
        when(time.getText()).thenReturn(timeEditable);

        when(locationEditable.toString()).thenReturn("Outside");
        when(location.getText()).thenReturn(locationEditable);

        when(priceEditable.toString()).thenReturn("0.0");
        when(price.getText()).thenReturn(priceEditable);

        when(maxEditable.toString()).thenReturn("100");
        when(max.getText()).thenReturn(maxEditable);

        assertTrue(fragment.validateInputs());
        assertTrue(fragment.editThenSave());
    }


    @Test
    public void testEmptyInput() {
        Editable titleEditable = mock(Editable.class),
                descEditable = mock(Editable.class),
                interestsEditable = mock(Editable.class),
                startDateEditable = mock(Editable.class),
                endDateEditable = mock(Editable.class),
                timeEditable = mock(Editable.class),
                locationEditable = mock(Editable.class),
                priceEditable = mock(Editable.class),
                maxEditable = mock(Editable.class);


        when(titleEditable.toString()).thenReturn("");
        when(title.getText()).thenReturn(titleEditable);

        when(descEditable.toString()).thenReturn("");
        when(desc.getText()).thenReturn(descEditable);

        when(interestsEditable.toString()).thenReturn("");
        when(interests.getText()).thenReturn(interestsEditable);

        when(startDateEditable.toString()).thenReturn("");
        when(startDate.getText()).thenReturn(startDateEditable);

        when(endDateEditable.toString()).thenReturn("");
        when(endDate.getText()).thenReturn(endDateEditable);

        when(timeEditable.toString()).thenReturn("");
        when(time.getText()).thenReturn(timeEditable);

        when(locationEditable.toString()).thenReturn("");
        when(location.getText()).thenReturn(locationEditable);

        when(priceEditable.toString()).thenReturn("");
        when(price.getText()).thenReturn(priceEditable);

        when(maxEditable.toString()).thenReturn("");
        when(max.getText()).thenReturn(maxEditable);

        assertFalse(fragment.validateInputs());
        assertFalse(fragment.editThenSave());
    }
}
