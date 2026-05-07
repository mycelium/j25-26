package com.labs.db;
import java.sql.*;

public class SqliteDb implements AutoCloseable {
    private final Connection conn;

    public SqliteDb(String path) throws SQLException {
        // WAL режим улучшает конкурентность записи/чтения
        conn = DriverManager.getConnection("jdbc:sqlite:" + path + "?busy_timeout=5000");
        try (Statement s = conn.createStatement()) {
            s.execute("PRAGMA journal_mode=WAL;");
            s.execute("CREATE TABLE IF NOT EXISTS store (k TEXT PRIMARY KEY, v TEXT)");
        }
    }

    public synchronized void store(String key, String val) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("INSERT OR REPLACE INTO store VALUES (?, ?)")) {
            ps.setString(1, key); ps.setString(2, val); ps.executeUpdate();
        }
    }

    public synchronized String retrieve(String key) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT v FROM store WHERE k = ?")) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    @Override public void close() throws SQLException { conn.close(); }
}