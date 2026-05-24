package com.labs.server;

import java.sql.*;

public class Storage {
    private final Connection conn;
    private static final String TABLE = "kv_store";

    public Storage(String dbPath) {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            initTable();
        } catch (Exception e) {
            throw new RuntimeException("DB init failed: " + e.getMessage(), e);
        }
    }

    private void initTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE + " (key TEXT PRIMARY KEY, value TEXT)";
        conn.createStatement().execute(sql);

        conn.createStatement().execute("PRAGMA journal_mode=WAL");
    }

    public void save(String key, String value) throws SQLException {
        String sql = "INSERT OR REPLACE INTO " + TABLE + " (key, value) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        }
    }

    public String load(String key) throws SQLException {
        String sql = "SELECT value FROM " + TABLE + " WHERE key = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("value") : null;
            }
        }
    }

    public void close() {
        try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
    }
}