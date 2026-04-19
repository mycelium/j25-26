package loadtest;

import java.sql.*;

public class DatabaseHelper {
    private final String dbPath;

    public DatabaseHelper(String dbPath) {
        this.dbPath = dbPath;
    }

    public void init() throws SQLException {
        try (Connection conn = getConnection();
             Statement st = conn.createStatement()) {
            st.execute("PRAGMA journal_mode=WAL");
            st.execute(
                "CREATE TABLE IF NOT EXISTS items (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  name TEXT NOT NULL," +
                "  value TEXT NOT NULL," +
                "  created_at TEXT DEFAULT (datetime('now'))" +
                ")"
            );
        }
    }

    public long insert(String name, String value) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "INSERT INTO items(name, value) VALUES(?, ?)",
                 Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, value);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getLong(1) : -1;
            }
        }
    }

    public String getById(long id) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT name, value FROM items WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name") + ":" + rs.getString("value");
                }
            }
        }
        return null;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }
}
