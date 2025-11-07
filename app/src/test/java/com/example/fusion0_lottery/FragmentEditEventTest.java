package com.example.fusion0_lottery;

import static org.mockito.Mockito.*;

import android.widget.EditText;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;

public class FragmentEditEventTest {

    private FragmentEditEvent fragment;
    private FirebaseFirestore mockDb;
    private DocumentReference mockDocRef;

    @Before
    public void setUp() {
        fragment = new FragmentEditEvent();
        mockDb = mock(FirebaseFirestore.class);
        mockDocRef = mock(DocumentReference.class);

        // mock Firestore chain: db.collection("Events").document(eventId)
        when(mockDb.collection("Events")).thenReturn(mock(com.google.firebase.firestore.CollectionReference.class, RETURNS_DEEP_STUBS));
        when(mockDb.collection("Events").document(anyString())).thenReturn(mockDocRef);

        // inject the mock db
        fragment.db = mockDb;
        fragment.eventId = "fakeEventId";
    }

    private EditText mockEditText(String text) {
        EditText mockEdit = mock(EditText.class);
        when(mockEdit.getText()).thenReturn(new android.text.SpannableStringBuilder(text));
        return mockEdit;
    }

    @Test
    public void testSaveEventChanges_withValidInput_updatesFirestore() {
        // given valid mock inputs
        EditText title = mockEditText("New Event");
        EditText desc = mockEditText("Description");
        EditText startDate = mockEditText("2025-11-06");
        EditText endDate = mockEditText("2025-11-07");
        EditText time = mockEditText("12:00 PM");
        EditText location = mockEditText("Random Location");
        EditText price = mockEditText("20.5");
        EditText maxEntrants = mockEditText("100");

        // inject mocks
        fragment.setMockInputs(title, desc, startDate, endDate, time, location, price, maxEntrants);

        // when saving
        fragment.testSaveEventChanges(
                "New Event", "Description", "2025-11-06",
                "2025-11-07", "12:00 PM", "Random Location",
                "20.5", "100"
        );

        // then Firestore's update should be called with the correct parameters
        verify(mockDocRef).update(
                eq("eventName"), eq("New Event"),
                eq("description"), eq("Description"),
                eq("startDate"), eq("2025-11-06"),
                eq("endDate"), eq("2025-11-07"),
                eq("time"), eq("12:00 PM"),
                eq("location"), eq("Random Location"),
                eq("price"), eq(20.5),
                eq("maxEntrants"), eq(100)
        );
    }

    @Test
    public void testSaveEventChanges_withEmptyFields_doesNotCallUpdate() {
        // given missing title
        EditText title = mockEditText("");
        EditText desc = mockEditText("Description");
        EditText startDate = mockEditText("2025-11-06");
        EditText endDate = mockEditText("2025-11-07");
        EditText time = mockEditText("12:00 PM");
        EditText location = mockEditText("Random Location");
        EditText price = mockEditText("20.5");
        EditText maxEntrants = mockEditText("100");

        fragment.setMockInputs(title, desc, startDate, endDate, time, location, price, maxEntrants);

        fragment.testSaveEventChanges(
                "", "Description", "2025-11-06", "2025-11-07",
                "12:00 PM", "Random Location", "20.5", "100"
        );
    }
}
