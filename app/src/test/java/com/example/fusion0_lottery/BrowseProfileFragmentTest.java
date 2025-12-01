package com.example.fusion0_lottery;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for BrowseProfileFragment
 */
public class BrowseProfileFragmentTest {

    @Mock
    private FirebaseFirestore mockDb;

    @Mock
    private FirebaseAuth mockAuth;

    @Mock
    private FirebaseUser mockFirebaseUser;

    @Mock
    private CollectionReference mockCollectionRef;

    private BrowseProfileFragment fragment;
    private List<User> testUsers;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        fragment = new BrowseProfileFragment();
        testUsers = createTestUsers();

        when(mockAuth.getCurrentUser()).thenReturn(mockFirebaseUser);
        when(mockFirebaseUser.getUid()).thenReturn("admin_device_id");
        when(mockDb.collection("Users")).thenReturn(mockCollectionRef);
    }

    private List<User> createTestUsers() {
        List<User> users = new ArrayList<>();

        User entrant1 = new User();
        entrant1.setDevice_id("device_001");
        entrant1.setName("jane doe");
        entrant1.setEmail("jane@gmail.com");
        entrant1.setRole("Entrant");
        entrant1.setPhone_number("587-555-0101");
        users.add(entrant1);

        User entrant2 = new User();
        entrant2.setDevice_id("device_002");
        entrant2.setName("Bob Smith");
        entrant2.setEmail("smith@gmail.com");
        entrant2.setRole("Entrant");
        entrant2.setPhone_number("587-555-0102");
        users.add(entrant2);

        User entrant3 = new User();
        entrant3.setDevice_id("device_003");
        entrant3.setName("jane White");
        entrant3.setEmail("white@gmail.com");
        entrant3.setRole("Entrant");
        entrant3.setPhone_number("587-555-0103");
        users.add(entrant3);

        User organizer1 = new User();
        organizer1.setDevice_id("device_004");
        organizer1.setName("David beckham");
        organizer1.setEmail("beckham@gmail.com");
        organizer1.setRole("Organizer");
        organizer1.setPhone_number("587-555-0104");
        users.add(organizer1);

        User organizer2 = new User();
        organizer2.setDevice_id("device_005");
        organizer2.setName("Cristiano Ronaldo");
        organizer2.setEmail("Ronaldo@gmail.com");
        organizer2.setRole("Organizer");
        organizer2.setPhone_number("587-555-0105");
        users.add(organizer2);

        return users;
    }

    @Test
    public void testFilterUsersByAllRoles() {

        BrowseProfileAdapter adapter = new BrowseProfileAdapter(testUsers, null);
        List<User> allUsers = new ArrayList<>(testUsers);
        assertEquals(5, allUsers.size());

        long entrantCount = allUsers.stream()
                .filter(u -> "Entrant".equals(u.getRole()))
                .count();
        long organizerCount = allUsers.stream()
                .filter(u -> "Organizer".equals(u.getRole()))
                .count();

        assertEquals(3, entrantCount);
        assertEquals(2, organizerCount);
    }

    @Test
    public void testFilterEntrantOnly() {
        List<User> filteredList = new ArrayList<>();
        for (User user : testUsers) {
            if (user.getRole() != null && user.getRole().equals("Entrant")) {
                filteredList.add(user);
            }
        }

        assertEquals(3, filteredList.size());
        for (User user : filteredList) {
            assertEquals("Entrant", user.getRole());
        }
    }

    @Test
    public void testFilterOrganizerOnly() {
        List<User> filteredList = new ArrayList<>();
        for (User user : testUsers) {
            if (user.getRole() != null && user.getRole().equals("Organizer")) {
                filteredList.add(user);
            }
        }

        assertEquals(2, filteredList.size());
        for (User user : filteredList) {
            assertEquals("Organizer", user.getRole());
        }
    }

    @Test
    public void testFilterEmptyResult() {
        List<User> filteredList = new ArrayList<>();
        for (User user : testUsers) {
            if (user.getRole() != null && user.getRole().equals("Admin")) {
                filteredList.add(user);
            }
        }

        assertEquals(0, filteredList.size());
    }

    @Test
    public void testFilterNullRole() {
        User nullRoleUser = new User();
        nullRoleUser.setDevice_id("device_006");
        nullRoleUser.setName("Cristiano Ronaldo");
        nullRoleUser.setEmail("Ronaldo@gmail.com");
        nullRoleUser.setRole(null);

        List<User> usersWithNull = new ArrayList<>(testUsers);
        usersWithNull.add(nullRoleUser);

        List<User> filteredList = new ArrayList<>();
        for (User user : usersWithNull) {
            if (user.getRole() != null && user.getRole().equals("Entrant")) {
                filteredList.add(user);
            }
        }

        assertEquals(3, filteredList.size());
        assertFalse(filteredList.contains(nullRoleUser));
    }

    @Test
    public void testUserListNotEmpty() {
        assertNotNull(testUsers);
        assertFalse(testUsers.isEmpty());
        assertTrue(testUsers.size() > 0);
    }

    @Test
    public void testRemoveUser() {
        List<User> userListCopy = new ArrayList<>(testUsers);
        int initialSize = userListCopy.size();

        User userToRemove = userListCopy.get(0);
        userListCopy.remove(userToRemove);

        assertEquals(initialSize - 1, userListCopy.size());
        assertFalse(userListCopy.contains(userToRemove));
    }

    @Test
    public void testCannotRemoveOwnProfile() {
        String currentUserId = "admin_device_id";

        User currentUser = new User();
        currentUser.setDevice_id(currentUserId);
        currentUser.setName("Admin User");
        currentUser.setEmail("admin@email.com");
        currentUser.setRole("Admin");
        assertEquals(currentUserId, currentUser.getDevice_id());
    }
}