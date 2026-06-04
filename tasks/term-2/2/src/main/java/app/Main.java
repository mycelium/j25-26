package app;

import httpserver.*;
import java.sql.*;

public class Main {
    public static void main(String[] args) throws Exception {
        String parserName = "own";
        boolean virtual = false;

        for (String arg : args) {
            if (arg.startsWith("--parser=")) {
                parserName = arg.substring("--parser=".length());
            } else if (arg.startsWith("--virtual=")) {
                virtual = Boolean.parseBoolean(arg.substring("--virtual=".length()));
            }
        }

        ParserType parser = ParserType.create(parserName);
        System.out.println("Using parser: " + parserName + ", virtual: " + virtual);

        // SQLite
        Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db");
        conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS records (id INTEGER PRIMARY KEY AUTOINCREMENT, data TEXT)"
        );
        conn.close();

        HttpServer server = new HttpServer("localhost", 8080, 4, virtual);

        // GET /
        server.addRoute("/", "GET", (req, resp) -> {
            resp.setBody("Home page");
        });

        // GET /hello
        server.addRoute("/hello", "GET", (req, resp) -> {
            String name = req.getQueryParams().getOrDefault("name", "World");
            resp.setBody("Hello, " + name + "!");
        });

        // POST /request1
        server.addRoute("/request1", "POST", (req, resp) -> {
            Object obj = parser.parse(req.getBodyAsString(), Object.class);
            String json = parser.toJson(obj);

            Connection c = DriverManager.getConnection("jdbc:sqlite:test.db");

            PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO records (data) VALUES (?)"
            );
            ps.setString(1, json);
            ps.executeUpdate();
            ps.close();

            Statement st = c.createStatement();
            ResultSet rs = st.executeQuery("SELECT last_insert_rowid()");
            rs.next();
            int id = rs.getInt(1);
            rs.close();
            st.close();

            PreparedStatement ps2 = c.prepareStatement(
                    "SELECT data FROM records WHERE id = ?"
            );
            ps2.setInt(1, id);
            ResultSet rs2 = ps2.executeQuery();
            rs2.next();
            String result = rs2.getString("data");
            rs2.close();
            ps2.close();
            c.close();

            resp.setBody(result);
        });

        // POST /request2
        server.addRoute("/request2", "POST", (req, resp) -> {
            Object obj = parser.parse(req.getBodyAsString(), Object.class);
            String result = parser.toJson(obj);
            resp.setBody(result);
        });

        server.start();
        System.out.println("Server started on http://localhost:8080");
        while (true) {
            Thread.sleep(10000);
        }
    }
}