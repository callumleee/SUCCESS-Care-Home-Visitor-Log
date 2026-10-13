package persistence;

import model.VisitorEntry;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

// Stores and retrieves visitor entries. Kept as an interface so the storage
// (currently SQLite) can be swapped out, e.g. for PostgreSQL.
public interface VisitorRepository {

    // EFFECTS: returns all stored visitor entries, oldest first
    List<VisitorEntry> findAll() throws SQLException;

    // MODIFIES: entry
    // EFFECTS: stores the given entry and sets its id to the generated id
    void insert(VisitorEntry entry) throws SQLException;

    // EFFECTS: records the sign-out time for the entry with the given id
    void signOut(int id, LocalDateTime timeOut) throws SQLException;
}
