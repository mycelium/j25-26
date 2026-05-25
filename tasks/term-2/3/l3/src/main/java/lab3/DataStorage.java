package lab3;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class DataStorage {

    private static final String DB_KEY = "latest";
    private final String url;

    public DataStorage(String dbFile) {
        this.url = "jdbc:sqlite:" + dbFile;
        initTable();
    }

    private void initTable() {
        try (Connection conn = DriverManager.getConnection(url);
             Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS storage (
                    key   TEXT PRIMARY KEY,
                    value TEXT NOT NULL
                )
            """);
        } catch (Exception e) {
            throw new RuntimeException("Failed to init DB", e);
        }
    }

    public synchronized void write(String data) {
        String sql = """
            INSERT INTO storage(key, value) VALUES(?, ?)
            ON CONFLICT(key) DO UPDATE SET value = excluded.value
        """;
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, DB_KEY);
            ps.setString(2, data);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to write to DB", e);
        }
    }

    public synchronized String read() {
        String sql = "SELECT value FROM storage WHERE key = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, DB_KEY);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("value") : "";
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read from DB", e);
        }
    }
}
