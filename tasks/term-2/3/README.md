# Load Testing Report — Java HTTP Server & JSON Parser

## 1. How to Configure and Launch

### Prerequisites
- Java 21+
- Files in `lib/`: `1.jar` (JSON parser), `2.jar` (HTTP server), `gson-2.10.1.jar`

### Build
```bash
javac -cp "lib/*" src/PerformanceServer.java src/LoadTest.java
```

### Configure server
Open `src/PerformanceServer.java` and set the constants:

```java
static final boolean USE_GSON            = false; // true = Gson, false = own parser
static final boolean USE_VIRTUAL_THREADS = false; // true = virtual threads, false = classic pool
static final int     THREAD_POOL_SIZE    = 16;    // ignored when virtual = true
```

### Run server (Terminal 1)
```bash
java -cp "lib/*;src" PerformanceServer
```

### Run load test (Terminal 2)
```bash
java -cp "lib/*;src" LoadTest
```

> To change load parameters edit `REQUESTS` and `CONCURRENCY` in `src/LoadTest.java`.

---

## 2. Experiment Description

Two request types are tested:

**Request-1** — I/O-bound path:
1. Client sends `POST /users` with JSON body `{"name": "...", "age": ...}`
2. Server parses JSON (own parser or Gson)
3. User is appended to a CSV file on disk
4. Server responds with the saved user as JSON

**Request-2** — CPU/memory-bound path:
1. Client sends `GET /stats`
2. Server iterates in-memory list of all users
3. Computes `count`, `sum(age)`, `avg(age)`
4. Serializes result to JSON and responds

---

## 3. Hardware Description

| Parameter      | Value                                |
|----------------|--------------------------------------|
| CPU            | AMD ryzen 5 3600, 10 cores (2P + 8E) |
| RAM            | 16 GB DDR4                           |
| OS             | Windows 11                           |
| JDK version    | OpenJDK 25                           |
| Server machine | Same machine as client (loopback)    |
| Network        | localhost (127.0.0.1)                |

---

## 4. Experiment Parameters

| Parameter       | Value   |
|-----------------|---------|
| Total requests  | 10 000  |
| Concurrency     | 50      |
| Thread pool size| 16      |
| Warm-up         | 100 req |
| Timeout per req | 30 s    |
| Body size (R-1) | ~35 B   |
| Data storage    | CSV file (data/store.txt) |

---

## 5. Results Table

### Avg time per request (ms)

| req       | Virtual + Own parser | Virtual + Gson | Classic + Own parser | Classic + Gson |
|-----------|----------------------|----------------|----------------------|----------------|
| Request-1 | 17.57 ms             | 18.49 ms       | 16.59 ms             | 17.00 ms       |
| Request-2 | 8.57 ms              | 8.87 ms        | 7.60 ms              | 7.85 ms        |

### Detailed metrics

| Metric         | Virtual + Own | Virtual + Gson | Classic + Own | Classic + Gson |
|----------------|---------------|----------------|---------------|----------------|
| R-1 Throughput | 2836 req/s    | 2694 req/s     | 3002 req/s    | 2929 req/s     |
| R-1 p50        | 17 ms         | 18 ms          | 16 ms         | 16 ms          |
| R-1 p95        | 30 ms         | 30 ms          | 21 ms         | 26 ms          |
| R-1 p99        | 44 ms         | 39 ms          | 23 ms         | 38 ms          |
| R-2 Throughput | 5814 req/s    | 5577 req/s     | 6540 req/s    | 6345 req/s     |
| R-2 p50        | 6 ms          | 7 ms           | 6 ms          | 6 ms           |
| R-2 p95        | 13 ms         | 19 ms          | 14 ms         | 23 ms          |
| R-2 p99        | 129 ms        | 42 ms          | 51 ms         | 55 ms          |

---

## 6. Analysis

**Classic vs Virtual threads:**
На данной нагрузке (concurrency=50, pool=16) классические потоки показали себя немного лучше:
Request-1 у классических ~16-17 ms против ~17-18 ms у виртуальных, Request-2 — 7.6-7.8 ms против 8.5-8.8 ms.
Это объясняется тем, что виртуальные потоки дают выигрыш при очень высоком числе блокирующих операций (тысячи одновременных соединений). При concurrency=50 и pool=16 классический пул справляется без накладных расходов на планировщик виртуальных потоков.

**Own parser vs Gson:**
Разница минимальна (в пределах погрешности). Собственный парсер незначительно быстрее на простых плоских объектах (User с 3 полями), что логично: Gson универсальнее и несёт больше overhead для reflection и обработки сложных типов.

**Request-1 vs Request-2:**
Request-2 стабильно в 2 раза быстрее Request-1 (7-9 ms vs 16-18 ms). Узкое место Request-1 — синхронизированная запись в файл на диск, что неизбежно создаёт очередь при высоком параллелизме.
