# Lab 3 — Load Testing Report

## Hardware Description

| Property | Value |
|---|---|
| OS | Windows 11 10.0 |
| JVM | 25.0.2 |
| CPU cores | 16 |
| Max heap | 7120 MB |

## Experiment Parameters

| Parameter | Value |
|---|---|
| Client threads | 50 |
| Server threads | 50 |
| Total requests per endpoint | 5000 |
| Request-1 endpoint | POST /store (JSON parse + file write/read) |
| Request-2 endpoint | POST /compute (JSON parse + in-memory fibonacci) |
| Payload size | ~80 bytes |

## Experiment Description

Each scenario runs two endpoints sequentially:

- **Request-1** (`POST /store`): the server parses the JSON body, appends a record to `data/store.txt`, reads back the last 5 lines, and returns them as JSON. Measures I/O-bound performance.
- **Request-2** (`POST /compute`): the server parses the JSON body, computes `fibonacci(a % 30) + b` in memory, and returns the result as JSON. Measures CPU-bound + JSON throughput.

Four combinations are tested:

| Combination | Thread type | JSON parser |
|---|---|---|
| Virtual + OwnParser | Virtual (JDK 21) | Lab-1 custom parser |
| Virtual + Gson      | Virtual (JDK 21) | Gson 2.x |
| Classic + OwnParser | Fixed thread pool | Lab-1 custom parser |
| Classic + Gson      | Fixed thread pool | Gson 2.x |

## How to Configure and Launch

```bash
# 1. Compile everything
./compile.sh

# 2. Run with defaults (20 threads, 1000 requests)
./run.sh

# 3. Custom parameters
java -cp out:lib/gson.jar \
     -Dthreads=50 -Drequests=5000 -Dhost=192.168.1.10 \
     loadtest.LoadTestRunner
```

> **Gson**: download `gson-2.x.jar` and place it in `lib/`.
> Run without Gson to test only `OwnParser` combinations.

## Results

| Request | Virtual + OwnParser | Virtual + Gson | Classic + OwnParser | Classic + Gson |
|---|---|---|---|---|
| Request-1 | 28,7 ms avg (p95: 53,0 ms) | 25,6 ms avg (p95: 35,0 ms) | 28,7 ms avg (p95: 43,0 ms) | 31,8 ms avg (p95: 43,0 ms) |
| Request-2 | 8,0 ms avg (p95: 10,0 ms) | 5,9 ms avg (p95: 7,0 ms) | 5,3 ms avg (p95: 7,0 ms) | 6,5 ms avg (p95: 13,0 ms) |

### Throughput (req/s)

| Request | Virtual + OwnParser | Virtual + Gson | Classic + OwnParser | Classic + Gson |
|---|---|---|---|---|
| Request-1 | 1723,5 req/s | 1944,8 req/s | 1728,3 req/s | 1565,4 req/s |
| Request-2 | 6142,5 req/s | 8445,9 req/s | 9416,2 req/s | 7657,0 req/s |

### Error rates

| Request | Scenario | Total | OK | Errors |
|---|---|---|---|---|
| Request-1 | Virtual + OwnParser | 5000 | 5000 | 0 |
| Request-2 | Virtual + OwnParser | 5000 | 5000 | 0 |
| Request-1 | Virtual + Gson | 5000 | 5000 | 0 |
| Request-2 | Virtual + Gson | 5000 | 5000 | 0 |
| Request-1 | Classic + OwnParser | 5000 | 5000 | 0 |
| Request-2 | Classic + OwnParser | 5000 | 5000 | 0 |
| Request-1 | Classic + Gson | 5000 | 5000 | 0 |
| Request-2 | Classic + Gson | 5000 | 5000 | 0 |
