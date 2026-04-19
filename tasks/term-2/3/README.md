# Load Testing Report — HTTP Server & JSON Parser

## How to Configure and Launch

### Prerequisites

- JDK 21+
- Gradle 8+ (or use `gradle wrapper` to generate `./gradlew`)

### Build

```bash
cd tasks/term-2/3
gradle jar
```

This produces `build/libs/load-test-1.0.0.jar` (fat JAR with all dependencies).

### Start the server

The server accepts the following flags:

| Flag | Default | Description |
|------|---------|-------------|
| `--port` | `8080` | TCP port to listen on |
| `--threads` | `10` | Thread-pool size |
| `--virtual` | `false` | Use virtual threads (`true`) or platform threads (`false`) |
| `--gson` | `false` | Use Gson (`true`) or the custom JSON parser from lab-1 (`false`) |

**Examples (4 combinations):**

```bash
# Virtual threads + own parser
gradle run -Pvirtual=true  -Pgson=false -Pthreads=10

# Virtual threads + Gson
gradle run -Pvirtual=true  -Pgson=true  -Pthreads=10

# Classic threads + own parser
gradle run -Pvirtual=false -Pgson=false -Pthreads=10

# Classic threads + Gson
gradle run -Pvirtual=false -Pgson=true  -Pthreads=10
```

Or via the fat JAR:

```bash
java -jar build/libs/load-test-1.0.0.jar --port=8080 --virtual=true --gson=false --threads=10
```

To stop the server press **Ctrl+C** — the shutdown hook will call `server.stop()` gracefully.

### Run the load test (programmatic runner)

While the server is running in a separate terminal:

```bash
gradle runLoadTest -Pthreads=50 -Prequests=1000
```

Or via the fat JAR:

```bash
java -cp build/libs/load-test-1.0.0.jar loadtest.LoadTestRunner \
  --host=localhost --port=8080 --threads=50 --requests=1000
```


---

## Experiment Description

Two HTTP endpoints are tested:

**Request-1** — I/O-bound workload  
`POST /api/request1` receives a JSON body `{"name":"…","value":"…"}`, parses it,
writes the record to a SQLite file database (WAL mode), reads it back by the generated
primary key, and returns `{"id":…,"retrieved":"…","status":"ok"}`.

**Request-2** — CPU-bound workload  
`POST /api/request2` receives `{"n":30}`, parses it, computes `fibonacci(30)`
iteratively, and returns `{"n":30,"result":832040,"status":"ok"}`.

Each endpoint is measured under **four configurations**:

| # | Threads | JSON parser |
|---|---------|-------------|
| A | Virtual (`--virtual=true`) | Own parser (`--gson=false`) |
| B | Virtual (`--virtual=true`) | Gson (`--gson=true`) |
| C | Classic (`--virtual=false`) | Own parser (`--gson=false`) |
| D | Classic (`--virtual=false`) | Gson (`--gson=true`) |

---

## Hardware Description

| Component | Value |
|-----------|-------|
| CPU | 12th Gen Intel Core i7-12700H (14 cores / 20 threads) |
| RAM | 16 GiB DDR5 |
| Storage | NVMe SSD (SQLite data.db) |
| OS | Linux 6.x (CachyOS) |
| JDK | OpenJDK 26.0.2 |
| Network | Loopback (server and load runner on the same machine) |

---

## Experiment Parameters

| Parameter | Value |
|-----------|-------|
| Concurrent threads (JMeter / runner) | 50 |
| Requests per thread | 20 |
| Total requests per endpoint | 1 000 |
| Ramp-up time | 5 s |
| Server thread-pool size | 10 (classic) / unbounded (virtual) |
| Request-1 payload | `{"name":"item_N","value":"benchmark_data_R"}` |
| Request-2 payload | `{"n":30}` |
| Warm-up | 1 run discarded before measurement |

---

## Results

Average response time per request (ms). 50 concurrent client threads, 1 000 requests per endpoint,
preceded by a 200-request warm-up run. Server thread-pool size: 10.

| Request       | Virtual + own parser | Virtual + Gson | Classic + own parser | Classic + Gson |
|---------------|---------------------:|---------------:|---------------------:|---------------:|
| **Request-1** | 85.6 ms              | 82.9 ms        | 95.3 ms              | 98.5 ms        |
| **Request-2** | 7.6 ms               | 8.7 ms         | 6.7 ms               | 8.2 ms         |

P95 latency (ms):

| Request       | Virtual + own parser | Virtual + Gson | Classic + own parser | Classic + Gson |
|---------------|---------------------:|---------------:|---------------------:|---------------:|
| **Request-1** | 281 ms               | 262 ms         | 282 ms               | 274 ms         |
| **Request-2** | 11 ms                | 12 ms          | 10 ms                | 11 ms          |

Throughput (req/s, both endpoints combined):

| Config                | Request-1 | Request-2 |
|-----------------------|----------:|----------:|
| Virtual + own parser  | 431.6     | 5 747     |
| Virtual + Gson        | 462.7     | 5 102     |
| Classic + own parser  | 440.3     | 6 494     |
| Classic + Gson        | 459.1     | 5 405     |

### Observations

**Request-1 (I/O-bound — SQLite write + read):**
Virtual threads are ~10–15 ms faster on average than classic threads (85–83 ms vs 95–98 ms).
The bottleneck is SQLite write contention: even in WAL mode, each `INSERT` acquires an
exclusive lock, serialising writers. Virtual threads help because they yield the OS thread
during the blocking JDBC call, letting other virtual threads run, whereas 10 classic threads
all block waiting for the lock.

**Request-2 (CPU-bound — iterative Fibonacci):**
Classic threads are marginally faster (6.7–8.2 ms vs 7.6–8.7 ms). Virtual threads add a
small scheduling overhead for pure-CPU paths where no blocking I/O occurs. The difference
is within noise on a loaded loopback setup.

**Own parser vs Gson:**
For Request-2 the own parser is consistently ~1 ms faster per request; for Request-1
the difference is negligible because parsing time is dwarfed by SQLite latency.

**Limitation:** server and load runner ran on the same machine via loopback, so network
and CPU resources are shared. Results on separate machines would show lower latency and
higher Request-2 throughput.
