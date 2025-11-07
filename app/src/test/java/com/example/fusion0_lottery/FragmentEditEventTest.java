package com.example.fusion0_lottery;

import static org.mockito.Mockito.*;

import android.widget.EditText;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;

public class FragmentEditEventTest {

    private FragmentEditEvent fragment;
    private FirebaseFirestore mockDb;
    private CollectionReference mockCollection;
    private DocumentReference mockDoc;

    private EditText titleInput, descInput, startInput, endInput, timeInput, priceInput, maxEntrantsInput;

    @Before
    public void setUp() {
        fragment = new FragmentEditEvent();

        // Mock Firestore and references
        mockDb = mock(FirebaseFirestore.class);
        mockCollection = mock(CollectionReference.class);
        mockDoc = mock(DocumentReference.class);

        when(mockDb.collection("Events")).thenReturn(mockCollection);
        when(mockCollection.document(anyString())).thenReturn(mockDoc);

        // Inject Firestore mock and fake event ID
        fragment.db = mockDb;
        fragment.eventId = "testEventId";

        // Mock EditTexts (you’re not testing UI behavior, just the update call)
        titleInput = mock(EditText.class);
        descInput = mock(EditText.class);
        startInput = mock(EditText.class);
        endInput = mock(EditText.class);
        timeInput = mock(EditText.class);
        priceInput = mock(EditText.class);
        maxEntrantsInput = mock(EditText.class);

        // Inject mocks using your new test-only setter
        fragment.setMockInputs(titleInput, descInput, startInput, endInput, timeInput, priceInput, maxEntrantsInput);
    }


    @Test
    public void testSaveEventsFirestore() {
        // Act — call your new helper method that doesn’t rely on EditText
        fragment.testSaveEventChanges(
                "new event name",
                "hello world",
                "2025-11-06",
                "2025-11-07",
                "12:00 PM",
                "2.0",
                "500"
        );

        // Assert — verify the Firestore update call
        verify(mockDoc, times(1)).update(
                eq("eventName"), eq("new event name"),
                eq("description"), eq("hello world"),
                eq("startDate"), eq("2025-11-06"),
                eq("endDate"), eq("2025-11-07"),
                eq("time"), eq("12:00 PM"),
                eq("price"), eq(2.0),
                eq("maxEntrants"), eq(500)
        );
    }
}
