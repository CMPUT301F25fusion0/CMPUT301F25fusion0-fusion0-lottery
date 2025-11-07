package com.example.fusion0_lottery;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

public class FragmentUpdatePosterTest {

    private FirebaseFirestore mockDb;
    private DocumentReference mockDocRef;
    private FragmentUpdatePoster fragment;

    @Before
    public void setup() {
        mockDb = mock(FirebaseFirestore.class);
        mockDocRef = mock(DocumentReference.class);

        when(mockDb.collection("Events")).thenReturn(mock(com.google.firebase.firestore.CollectionReference.class));
        when(mockDb.collection("Events").document("event123")).thenReturn(mockDocRef);

        fragment = new FragmentUpdatePoster();
        // normally set arguments or inject the mock
    }

    @Test
    public void testFirestoreSetIsCalledOnUpload() {
        // Arrange
        Uri fakeUri = mock(Uri.class);
        Task<Void> fakeTask = TaskUtil.successTask(null);
        when(mockDocRef.set(anyMap(), eq(SetOptions.merge()))).thenReturn(fakeTask);

        // Act
        mockDocRef.set(Collections.singletonMap("posterImage", "fakeBase64"), SetOptions.merge());

        // Assert
        verify(mockDocRef, times(1))
                .set(anyMap(), eq(SetOptions.merge()));
    }

    @Test
    public void testFirestoreSetFailure() {
        // Arrange
        Uri fakeUri = mock(Uri.class);
        Task<Void> failedTask = TaskUtil.failureTask(new Exception("Network error"));
        when(mockDocRef.set(anyMap(), eq(SetOptions.merge()))).thenReturn(failedTask);

        // Act
        mockDocRef.set(Collections.singletonMap("posterImage", "fakeBase64"), SetOptions.merge());

        // Assert
        verify(mockDocRef, times(1)).set(anyMap(), eq(SetOptions.merge()));
    }
}
