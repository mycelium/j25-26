# Load Testing: HTTP Server + JSON Parser

## Overview

This project benchmarks the HTTP server (lab-2) combined with different JSON parser implementations:
- **Own parser** (lab-1) vs **Gson**
- **Virtual threads** vs **Classic (platform) threads**

Two request types are tested:
- **Request-1**: Parse JSON → store in file → retrieve from file → return response
- **Request-2**: Parse JSON → calculate sum/average → return JSON response

## How to Build and Run

### Prerequisites
- Java 21+
- Gradle 8.x (or use the included `./gradlew`)

### Build
```bash
./gradlew app:build
```

### Run Load Test (default settings)
```bash
./gradlew app:run
```

### Run with Custom Parameters
```bash
./gradlew app:run --args="--port 18090 --server-threads 8 --client-threads 10 --requests 200"
```

### Parameters

| Parameter | Default | Description |
|-----------|---------|-------------|
| `--port` | 18090 | Server port |
| `--server-threads` | 8 | Number of server worker threads |
| `--client-threads` | 10 | Number of concurrent client threads |
| `--requests` | 200 | Requests per client thread |

Total requests per endpoint = `client-threads × requests`

## Experiment Description

### Request-1 (File I/O)
- Client sends: `POST /request1` with JSON `{"id":"test-123","data":"Hello load test world!"}`
- Server: parses JSON → appends to `load-test-data.txt` → reads last line → returns JSON

### Request-2 (Computation)
- Client sends: `POST /request2` with JSON `{"values":[1,2,3,4,5,6,7,8,9,10]}`
- Server: parses JSON → computes sum and average → returns JSON

### Variants Tested

| Variant | Threads | Parser |
|---------|---------|--------|
| Virtual + Own Parser | Virtual (JDK 21) | Custom JSON parser (lab-1) |
| Virtual + GSON | Virtual (JDK 21) | Google Gson 2.11.0 |
| Classic + Own Parser | Fixed thread pool | Custom JSON parser (lab-1) |
| Classic + GSON | Fixed thread pool | Google Gson 2.11.0 |

## Hardware Description

## Hardware Description

- **CPU**: AMD Ryzen 7 5700U with Radeon Graphics, 8 cores / 16 threads, 1.80 GHz base
- **RAM**: 16 GB DDR4 (15.3 GB available)
- **Storage**: KIOXIA KBG50ZNV512G NVMe SSD, 512 GB
- **OS**: Windows 11 Pro, 64-bit (x64)


## Experiment Parameters

| Parameter | Value |
|-----------|-------|
| Server threads | 8 |
| Client threads | 10 |
| Requests per thread | 200 |
| Total requests | 2000 per endpoint |
| Warmup requests | 40 per endpoint |
| Request-1 body size | ~50 bytes |
| Request-2 body size | ~40 bytes |

## Results

> **Note**: Run `./gradlew app:run` to generate actual results. The table below shows example output format.

| req       | Virtual + own parser | Virtual + GSON | Classic + own parser | Classic + GSON |
|-----------|----------------------|----------------|----------------------|----------------|
| Request-1 | 12,973 ms            | 13,146 ms      | 13,506 ms            | 11,095 ms      |
| Request-2 | 1,869 ms             | 1,549 ms       | 1,682 ms             | 1,763 ms       |


