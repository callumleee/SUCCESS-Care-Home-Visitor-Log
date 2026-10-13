package persistence;

import model.VisitorEntry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SqliteVisitorRepositoryTest {
    private Path dbFile;
    private SqliteVisitorRepository repository;
    private LocalDateTime timeIn;

    @BeforeEach
    void runBefore() throws IOException, SQLException {
        dbFile = Files.createTempFile("visitorlog-test", ".db");
        repository = new SqliteVisitorRepository(dbFile.toString());
        timeIn = LocalDateTime.of(2025, 10, 9, 22, 0);
    }

    @AfterEach
    void runAfter() throws IOException {
        Files.deleteIfExists(dbFile);
    }

    @Test
    void testEmptyDatabase() throws SQLException {
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void testInsertAssignsIds() throws SQLException {
        VisitorEntry entry1 = new VisitorEntry("Callum", "Resident A", timeIn);
        VisitorEntry entry2 = new VisitorEntry("Joe", "Resident B", timeIn.plusMinutes(10));
        assertEquals(0, entry1.getId());

        repository.insert(entry1);
        repository.insert(entry2);

        assertTrue(entry1.getId() > 0);
        assertTrue(entry2.getId() > entry1.getId());
    }

    @Test
    void testInsertAndFindAll() throws SQLException {
        repository.insert(new VisitorEntry("Callum", "Resident A", timeIn));
        repository.insert(new VisitorEntry("Joe", "Resident B", timeIn.plusMinutes(10)));

        List<VisitorEntry> entries = repository.findAll();
        assertEquals(2, entries.size());
        checkEntry(entries.get(0), "Callum", "Resident A", timeIn, null);
        checkEntry(entries.get(1), "Joe", "Resident B", timeIn.plusMinutes(10), null);
    }

    @Test
    void testSignOut() throws SQLException {
        VisitorEntry entry = new VisitorEntry("Callum", "Resident A", timeIn);
        repository.insert(entry);
        repository.signOut(entry.getId(), timeIn.plusMinutes(30));

        VisitorEntry loaded = repository.findAll().get(0);
        checkEntry(loaded, "Callum", "Resident A", timeIn, timeIn.plusMinutes(30));
        assertFalse(loaded.isSignedIn());
        assertEquals(30, loaded.getDurationMinutes());
    }

    @Test
    void testSignOutOnlyAffectsOneEntry() throws SQLException {
        VisitorEntry entry1 = new VisitorEntry("Callum", "Resident A", timeIn);
        VisitorEntry entry2 = new VisitorEntry("Joe", "Resident B", timeIn);
        repository.insert(entry1);
        repository.insert(entry2);

        repository.signOut(entry2.getId(), timeIn.plusMinutes(45));

        List<VisitorEntry> entries = repository.findAll();
        assertTrue(entries.get(0).isSignedIn());
        assertFalse(entries.get(1).isSignedIn());
    }

    @Test
    void testDataPersistsAfterReopening() throws SQLException {
        repository.insert(new VisitorEntry("Callum", "Resident A", timeIn));

        SqliteVisitorRepository reopened = new SqliteVisitorRepository(dbFile.toString());
        List<VisitorEntry> entries = reopened.findAll();
        assertEquals(1, entries.size());
        checkEntry(entries.get(0), "Callum", "Resident A", timeIn, null);
    }

    private void checkEntry(VisitorEntry entry, String visitor, String resident,
            LocalDateTime in, LocalDateTime out) {
        assertEquals(visitor, entry.getVisitorName());
        assertEquals(resident, entry.getResidentName());
        assertEquals(in, entry.getTimeIn());
        assertEquals(out, entry.getTimeOut());
    }
}
