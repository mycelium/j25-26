# Lab 3 — Load Testing Report

## Project Structure

```
lab3/
├── src/
│   ├── jsonlib/          ← Lab-1 JSON library (unchanged)
│   ├── com/httpserver/   ← Lab-2 HTTP server (unchanged)
│   └── loadtest/
│       ├── JsonAdapter.java       API to swap OwnParser ↔ Gson
│       ├── LoadTestConfig.java    Scenario configuration
│       ├── LoadTestResult.java    Result model + statistics
│       ├── LoadTestServer.java    HTTP server with /store and /compute
│       └── LoadTestRunner.java    Main class: sends requests + generates report
├── lib/
│   └── gson.jar          ← Place Gson here (optional)
├── data/
│   └── store.txt         ← Created at runtime by Request-1
├── results/
│   └── report.md         ← Generated after each run
├── compile.sh
└── run.sh
```

## How to Configure and Launch

### Prerequisites

- Java 21+ (`java -version`)
- *(Optional)* Gson JAR for Gson scenarios

### Step 1 — Get Gson (optional)

Download from Maven Central and place in `lib/`:

```bash
mkdir -p lib
curl -L "https://search.maven.org/remotecontent?filepath=com/google/gson/gson/2.10.1/gson-2.10.1.jar" \
     -o lib/gson.jar
```

Without Gson, only the `OwnParser` combinations will run.

### Step 2 — Compile

```bash
chmod +x compile.sh run.sh
./compile.sh
```

### Step 3 — Run (default: 20 threads, 1000 requests)

```bash
./run.sh
```

### Step 4 — Custom parameters

```bash
THREADS=50 REQUESTS=5000 ./run.sh
```

Or directly:

```bash
java -cp out:lib/gson.jar \
     -Dthreads=50 \
     -Drequests=5000 \
     -Dhost=192.168.1.10 \
     loadtest.LoadTestRunner
```

### Running on separate machines (recommended)

On the **server machine** — start the server only (modify `LoadTestRunner.main` or use the provided flag):

```bash
# Edit LoadTestRunner.java: set REMOTE_MODE=true and provide a serverOnly flag
java -cp out:lib/gson.jar -Dhost=0.0.0.0 loadtest.LoadTestRunner
```

On the **client machine**:

```bash
java -cp out:lib/gson.jar -Dhost=<SERVER_IP> -Drequests=5000 loadtest.LoadTestRunner
```

---

## Experiment Description

### Endpoints

| Endpoint | Method | Description |
|---|---|---|
| `POST /store` | **Request-1** | Parse JSON body → append to `data/store.txt` → read last 5 lines → return as JSON |
| `POST /compute` | **Request-2** | Parse JSON body → compute `fibonacci(a % 30) + b` in memory → return as JSON |

**Request-1** is I/O-bound (file write + read on disk).  
**Request-2** is CPU+JSON-bound (pure in-memory computation).

### Scenarios (4 combinations)

| # | Thread type | JSON parser |
|---|---|---|
| 1 | Virtual threads (JDK 21) | Lab-1 custom parser |
| 2 | Virtual threads (JDK 21) | Gson |
| 3 | Classic fixed thread pool | Lab-1 custom parser |
| 4 | Classic fixed thread pool | Gson |

### Payload sizes

- Request-1 body: `{"id":N,"value":"load-test-entry-N-padding-xxxxxxxxxxxxxxxxxxxxxxxxxx"}` (~80 bytes)
- Request-2 body: `{"a":N,"b":M,"label":"bench-N"}` (~40 bytes)

---

## Hardware Description

*(Fill in with your actual machine specs)*

| Property | Value |
|---|---|
| Machine | *(e.g. MacBook Pro M2, or EC2 t3.medium)* |
| OS | *(e.g. Ubuntu 22.04 / macOS 14)* |
| CPU | *(e.g. 8 cores @ 3.5 GHz)* |
| RAM | *(e.g. 16 GB)* |
| JVM | Java 21 (OpenJDK) |
| Storage | *(e.g. NVMe SSD)* |

---

## Experiment Parameters

| Parameter | Value |
|---|---|
| Client threads | 20 (default) |
| Server threads | 20 (default) |
| Total requests per endpoint | 1000 (default) |
| Warmup | none (first scenario acts as warmup) |
| Connection timeout | 5 000 ms |
| Read timeout | 10 000 ms |

---

## Results

*(Generated automatically in `results/report.md` after `./run.sh`)*

### Average latency per request

| Request | Virtual + OwnParser | Virtual + Gson | Classic + OwnParser | Classic + Gson |
|---|---|---|---|---|
| Request-1 | avg time per request | ... | ... | ... |
| Request-2 | avg time per request | ... | ... | ... |

### Throughput (req/s)

| Request | Virtual + OwnParser | Virtual + Gson | Classic + OwnParser | Classic + Gson |
|---|---|---|---|---|
| Request-1 | req/s | ... | ... | ... |
| Request-2 | req/s | ... | ... | ... |

---

## Notes

- **Virtual threads** shine when the workload is I/O-bound (Request-1) because they don't
  block a platform thread during file I/O.
- **Classic threads** can outperform virtual threads for pure CPU work (Request-2) due to
  lower scheduling overhead when threads are always busy.
- **OwnParser vs Gson**: expect similar performance; Gson may be slightly faster for large
  payloads due to streaming internals, while OwnParser has lower startup overhead.
- Run with `-Drequests=5000` or more for statistically significant p95 values.
