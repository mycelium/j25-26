import com.google.gson.Gson;
import com.webserver.*;
import jsonlib.Json;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class PerformanceServer {

    // ── Настройки ─────────────────────────────────────────────────────────────
    static final boolean USE_GSON           = true; // true = Gson, false = собственная библиотека
    static final boolean USE_VIRTUAL_THREADS = true; // true = виртуальные потоки, false = классический пул
    static final int     THREAD_POOL_SIZE   = 16;    // размер пула (игнорируется при virtual=true)

    static final String DATA_FILE = "data/store.txt";

    // ── In-memory хранилище ───────────────────────────────────────────────────
    static final List<User> users  = new CopyOnWriteArrayList<>();
    static final AtomicInteger seq = new AtomicInteger(1);
    static final Object fileLock   = new Object();
    static final Gson gson         = new Gson();

    // ── Модель ───────────────────────────────────────────────────────────────
    public static class User {
        public int    id;
        public String name;
        public int    age;

        public User() {}
        public User(int id, String name, int age) {
            this.id = id; this.name = name; this.age = age;
        }
    }

    public static class Stats {
        public long   count;
        public long   sumAge;
        public double avgAge;

        public Stats(long count, long sumAge, double avgAge) {
            this.count = count; this.sumAge = sumAge; this.avgAge = avgAge;
        }
    }

    // ── Запуск ────────────────────────────────────────────────────────────────
    public static void main(String[] args) throws Exception {
        new File("data").mkdirs();
        loadFromFile();

        WebServerConfig config = new WebServerConfig()
                .host("localhost")
                .port(8080)
                .threadPoolSize(THREAD_POOL_SIZE)
                .virtualThreads(USE_VIRTUAL_THREADS);

        WebServer server = new WebServer(config);

        // Request-1: POST /users — парсим JSON → сохраняем в файл → возвращаем пользователя
        server.post("/users", (req, res) -> {
            try {
                User user;
                if (USE_GSON) {
                    user = gson.fromJson(req.getBody(), User.class);
                } else {
                    Map<String, Object> map = Json.fromJsonToMap(req.getBody());
                    user = new User(0,
                            String.valueOf(map.get("name")),
                            ((Number) map.get("age")).intValue());
                }
                user.id = seq.getAndIncrement();
                users.add(user);
                appendToFile(user);

                String body = USE_GSON ? gson.toJson(user) : Json.toJson(user);
                return res.status(201, "Created")
                          .header("Content-Type", "application/json")
                          .body(body);
            } catch (Exception e) {
                return res.status(400, "Bad Request").body("Error: " + e.getMessage());
            }
        });

        // Request-1: GET /users — читаем из файла (уже загружено при старте)
        server.get("/users", (req, res) -> {
            String body = USE_GSON
                    ? gson.toJson(users)
                    : Json.toJson(new ArrayList<>(users));
            return res.header("Content-Type", "application/json").body(body);
        });

        // Request-2: GET /stats — считаем sum/avg age из памяти, возвращаем JSON
        server.get("/stats", (req, res) -> {
            long count = 0, sumAge = 0;
            for (User u : users) { count++; sumAge += u.age; }
            double avg = count == 0 ? 0 : (double) sumAge / count;
            Stats stats = new Stats(count, sumAge, avg);

            String body = USE_GSON ? gson.toJson(stats) : Json.toJson(stats);
            return res.header("Content-Type", "application/json").body(body);
        });

        System.out.printf("Server started: port=8080 virtual=%b gson=%b%n",
                USE_VIRTUAL_THREADS, USE_GSON);
        server.start();
    }

    // ── Файловое хранилище ────────────────────────────────────────────────────

    private static void appendToFile(User u) {
        synchronized (fileLock) {
            try (BufferedWriter bw = Files.newBufferedWriter(
                    Path.of(DATA_FILE), StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                bw.write(u.id + "," + u.name + "," + u.age);
                bw.newLine();
            } catch (IOException e) {
                throw new RuntimeException("File write error", e);
            }
        }
    }

    private static void loadFromFile() {
        Path p = Path.of(DATA_FILE);
        if (!Files.exists(p)) return;
        try (BufferedReader br = Files.newBufferedReader(p)) {
            String line;
            int maxId = 0;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 3);
                if (parts.length < 3) continue;
                int id  = Integer.parseInt(parts[0].trim());
                String name = parts[1].trim();
                int age = Integer.parseInt(parts[2].trim());
                users.add(new User(id, name, age));
                if (id > maxId) maxId = id;
            }
            seq.set(maxId + 1);
            System.out.println("Loaded " + users.size() + " users from file.");
        } catch (IOException e) {
            throw new RuntimeException("File read error", e);
        }
    }
}
