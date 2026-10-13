package persistence;

import model.VisitorEntry;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// A VisitorRepository backed by a SQLite database file.
// Times are stored as ISO-8601 text (e.g. 2025-10-09T22:00).
public class SqliteVisitorRepository implements VisitorRepository {
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS visits ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "visitor_name TEXT NOT NULL, "
            + "resident_name TEXT NOT NULL, "
            + "time_in TEXT NOT NULL, "
            + "time_out TEXT)";

    private final String url;

    // EFFECTS: opens (or creates) the database at the given file path and
    // creates the visits table if it does not exist yet
    public SqliteVisitorRepository(String dbPath) throws SQLException {
        File parent = new File(dbPath).getAbsoluteFile().getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        this.url = "jdbc:sqlite:" + dbPath;

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_TABLE);
        }
    }

    @Override
    public List<VisitorEntry> findAll() throws SQLException {
        String sql = "SELECT id, visitor_name, resident_name, time_in, time_out FROM visits ORDER BY id";
        List<VisitorEntry> entries = new ArrayList<>();

        try (Connection conn = connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String timeOut = rs.getString("time_out");
                entries.add(new VisitorEntry(
                        rs.getInt("id"),
                        rs.getString("visitor_name"),
                        rs.getString("resident_name"),
                        LocalDateTime.parse(rs.getString("time_in")),
                        timeOut == null ? null : LocalDateTime.parse(timeOut)));
            }
        }
        return entries;
    }

    @Override
    public void insert(VisitorEntry entry) throws SQLException {
        String sql = "INSERT INTO visits (visitor_name, resident_name, time_in, time_out) VALUES (?, ?, ?, ?)";

        try (Connection conn = connect();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, entry.getVisitorName());
            stmt.setString(2, entry.getResidentName());
            stmt.setString(3, entry.getTimeIn().toString());
            stmt.setString(4, entry.getTimeOut() == null ? null : entry.getTimeOut().toString());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    entry.setId(keys.getInt(1));
                }
            }
        }
    }

    @Override
    public void signOut(int id, LocalDateTime timeOut) throws SQLException {
        String sql = "UPDATE visits SET time_out = ? WHERE id = ?";

        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, timeOut.toString());
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    // EFFECTS: opens a new connection to the database
    private Connection connect() throws SQLException {
        return DriverManager.getConnection(url);
    }
}
