# Lab 3: Load Testing Report

## 1. How to Configure and Launch

**Requirements:** Java 21+, IntelliJ IDEA (or any IDE), `gson-2.10.1.jar` in `3/lib/`.

**Project setup (IntelliJ IDEA):**
1. Mark folders `1/`, `2/`, `3/` as **Sources Root** (right-click → Mark Directory as → Sources Root).
2. Add `3/lib/gson-2.10.1.jar` as a dependency: File → Project Structure → Modules → Dependencies → `+` → JARs or directories.

**Start the server:**
```
java TestServer [virtual|classic] [own|gson] [port] [threads]
```
Examples:
```
java TestServer virtual own 8082 200
java TestServer classic gson 8082 100
```

**Run the load tester:**
```
java LoadTester [threads] [requestsPerThread] [path] [port] [warmupRequests]
```
Examples:
```
java LoadTester 50 20 /req1 8082 50
java LoadTester 50 20 /req2 8082 50
```

To reproduce the full results table, run all 4 server configurations × 2 paths = 8 test runs.

---

## 2. Experiment Description

The experiment benchmarks the custom HTTP server under concurrent load, comparing:

- **Thread model:** Virtual threads (`Executors.newVirtualThreadPerTaskExecutor`) vs. Classic threads (`Executors.newFixedThreadPool`)
- **JSON parser:** Own implementation (Lab 1) vs. Google Gson

Two request types are tested:

- **Request 1 — I/O Bound:** Server parses incoming JSON, writes the payload to a physical file (`temp_db.txt`), reads it back, and returns a JSON response. File access is synchronized to avoid race conditions.
- **Request 2 — CPU Bound:** Server parses incoming JSON, computes a loop sum up to the given `limit`, and returns a JSON response with the result.

Each test run includes a **warmup phase** (50 requests, not measured) to allow JIT compilation to stabilize before recording results.

---

## 3. Hardware Description

| Component | Details                  |
|-----------|--------------------------|
| OS        | Windows 10               |
| CPU       | AMD Ryzen 7 5800U (8c/16t) |
| RAM       | 16 GB DDR4               |
| Storage   | NVMe SSD                 |

> Note: Both server and load tester ran on the same machine. Network latency is negligible (loopback), so results reflect pure server processing time.

---

## 4. Experiment Parameters

| Parameter                  | Value                          |
|----------------------------|--------------------------------|
| Concurrent threads         | 50                             |
| Requests per thread        | 20                             |
| Total requests per run     | 1000                           |
| Warmup requests            | 50 (excluded from results)     |
| JSON payload size          | ~50 bytes                      |
| File storage (Request 1)   | Local NVMe SSD (`temp_db.txt`) |
| Loop iterations (Request 2)| 5000                           |

---

## 5. Results Table

### Average latency (ms)

| Request   | Virtual + Own parser | Virtual + GSON | Classic + Own parser | Classic + GSON |
|-----------|----------------------|----------------|----------------------|----------------|
| Request-1 | 12.05 ms             | 15.60 ms       | 14.17 ms             | 14.92 ms       |
| Request-2 | 8.05 ms              | 8.16 ms        | 5.16 ms              | 8.63 ms        |

### p95 latency (ms)

| Request   | Virtual + Own parser | Virtual + GSON | Classic + Own parser | Classic + GSON |
|-----------|----------------------|----------------|----------------------|----------------|
| Request-1 | 28 ms                | 34 ms          | 31 ms                | 33 ms          |
| Request-2 | 18 ms                | 19 ms          | 11 ms                | 20 ms          |

### Throughput (req/s)

| Request   | Virtual + Own parser | Virtual + GSON | Classic + Own parser | Classic + GSON |
|-----------|----------------------|----------------|----------------------|----------------|
| Request-1 | ~830 req/s           | ~640 req/s     | ~705 req/s           | ~670 req/s     |
| Request-2 | ~1240 req/s          | ~1220 req/s    | ~1935 req/s          | ~1160 req/s    |

---

## 6. Analysis and Conclusions

### Request 1 (I/O Bound — file read/write)

Virtual threads outperform classic threads here (12 ms vs 14 ms avg). This is the expected result: virtual threads are designed for I/O-bound workloads. When a virtual thread blocks on a file write/read, the underlying carrier thread is released and can serve other requests, keeping CPU utilization high. Classic threads block their OS thread during I/O, reducing effective concurrency.

The own parser is faster than Gson for Request 1 (12 ms vs 15 ms with virtual threads). Since the JSON payloads are small (~50 bytes), the own parser's simpler implementation has less overhead than Gson's general-purpose machinery.

### Request 2 (CPU Bound — loop computation)

Classic threads outperform virtual threads for CPU-bound work (5.16 ms vs 8.05 ms avg). This is also expected: virtual threads provide no benefit when there is no I/O blocking — the carrier thread is never released. With 50 concurrent requests all doing CPU work, virtual threads add scheduling overhead without any compensating benefit. Classic threads map 1:1 to OS threads and are scheduled directly by the OS scheduler, which is more efficient for pure computation.

The own parser and Gson perform similarly for Request 2 since JSON parsing is a small fraction of total request time (dominated by the loop computation).

### Parser comparison

The own parser is competitive with Gson for small payloads. For larger or more complex JSON structures, Gson would likely win due to its optimized internals. The own parser's advantage is zero external dependencies.

### Key takeaway

Use **virtual threads for I/O-bound workloads** and **classic threads for CPU-bound workloads**. The thread model choice matters more than the JSON parser choice for these workload types.
