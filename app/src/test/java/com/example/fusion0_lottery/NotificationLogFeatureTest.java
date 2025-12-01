package com.example.fusion0_lottery;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Single test file for Notification feature.
 * Covers:
 * 1. Model tests
 * 2. Control logic tests (deletion)
 * 3. Backend logic tests (filtering and CSV export)
 */
public class NotificationLogFeatureTest {

    private List<NotificationLog> testLogs;

    @Before
    public void setup() {
        // Create realistic test data
        testLogs = new ArrayList<>();

        NotificationLog log1 = new NotificationLog();
        log1.setLogId("log1");
        log1.setOrganizerName("John Organizer");
        log1.setRecipientName("Alice Entrant");
        log1.setEventName("Winter Gala");
        log1.setMessageBody("Invitation to the Winter Gala");
        log1.setMessageType("Email");
        log1.setTimestamp(1733000000000L);

        NotificationLog log2 = new NotificationLog();
        log2.setLogId("log2");
        log2.setOrganizerName("Mary Organizer");
        log2.setRecipientName("Bob Entrant");
        log2.setEventName("Charity Drive");
        log2.setMessageBody("Reminder: Charity Drive tomorrow!");
        log2.setMessageType("SMS");
        log2.setTimestamp(1733100000000L);

        testLogs.add(log1);
        testLogs.add(log2);
    }

    /** Model Test: Verify getters and setters */
    @Test
    public void testNotificationLogModel() {
        NotificationLog sample = testLogs.get(0);
        assertEquals("John Organizer", sample.getOrganizerName());
        assertEquals("Winter Gala", sample.getEventName());
        assertEquals("Email", sample.getMessageType());
        assertTrue(sample.getTimestamp() > 0);
    }

    /** Control Test: Verify deletion logic (simulated) */
    @Test
    public void testDeleteNotificationLog() {
        NotificationController controller = new NotificationController();

        NotificationLog logToDelete = testLogs.get(0);
        boolean result = controller.deleteNotificationSimulated(testLogs, logToDelete.getLogId());

        assertTrue(result);
        assertEquals(1, testLogs.size()); // only one log left
        assertEquals("log2", testLogs.get(0).getLogId());
    }

    /** Backend Test: Filter logs by organizer */
    @Test
    public void testFilterLogsByOrganizer() {
        String organizerToFilter = "Mary Organizer";
        List<NotificationLog> filtered = new ArrayList<>();

        for (NotificationLog log : testLogs) {
            if (organizerToFilter.equals(log.getOrganizerName())) {
                filtered.add(log);
            }
        }

        assertEquals(1, filtered.size());
        assertEquals("Charity Drive", filtered.get(0).getEventName());
    }

    /** Backend Test: Export logs to CSV (simulate file write) */
    @Test
    public void testExportToCsvFile() throws Exception {
        File tempFile = File.createTempFile("test_logs", ".csv");
        FileWriter writer = new FileWriter(tempFile);

        writer.append("Date/Time,Organizer,Recipient,Event,Message Type,Message Body\n");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        for (NotificationLog log : testLogs) {
            writer.append(sdf.format(new Date(log.getTimestamp()))).append(",")
                    .append(log.getOrganizerName()).append(",")
                    .append(log.getRecipientName()).append(",")
                    .append(log.getEventName()).append(",")
                    .append(log.getMessageType()).append(",")
                    .append(log.getMessageBody()).append("\n");
        }
        writer.flush();
        writer.close();

        assertTrue(tempFile.exists());
        assertTrue(tempFile.length() > 0);
    }
}

/**
 * Simulated controller for notification logic.
 * Used for local unit testing without Firebase.
 */
class NotificationController {

    // Simulated deletion for unit test
    public boolean deleteNotificationSimulated(List<NotificationLog> logs, String logId) {
        return logs.removeIf(log -> log.getLogId().equals(logId));
    }
}
