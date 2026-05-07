# Task 3 - Load testing report

This folder contains the benchmark project for task 3. It uses the HTTP server from task 2 (`2/src/httpserver`) and the JSON parser from task 1 (`1/library/json`). For comparison with a production parser I also added Gson 2.14.0.

The goal of the run was simple: keep the same HTTP workload and compare four server/parser combinations:
virtual threads + own parser, virtual threads + Gson, classic threads + own parser, classic threads + Gson.

## How to configure and launch

Run everything from the repository root. The scripts expect JDK 21+ and PowerShell.

```powershell
powershell -ExecutionPolicy Bypass -File .\3\scripts\download-gson.ps1
powershell -ExecutionPolicy Bypass -File .\3\scripts\build.ps1
powershell -ExecutionPolicy Bypass -File .\3\scripts\run-suite.ps1
```

`download-gson.ps1` only downloads `gson-2.14.0.jar` into `3/lib`. If there is no network, put the jar there manually and start with `build.ps1`.

The main runner accepts the benchmark parameters as script arguments, for example:

```powershell
powershell -ExecutionPolicy Bypass -File .\3\scripts\run-suite.ps1 -Requests 1000 -Repeats 2
```

After a full run the suite rewrites this README and updates:

- `3/results/latest-detailed.csv`
- `3/results/latest-summary.csv`

## Experiment description

For every variant the benchmark starts my HTTP server on a separate port and sends `POST` requests with `java.net.http.HttpClient`. The request body is always JSON with the same shape: request id, user id, category, timestamp, 40 numbers, nested details, and tags.

The benchmark has two endpoints:

- `Request-1`: parse JSON, write a compact event into a JSONL file, read the last line back from that file, and return a JSON response with a checksum. The append/read block is synchronized so the file stays valid under parallel load. This means the endpoint intentionally includes serialized file I/O, not only HTTP and JSON parsing.
- `Request-2`: parse JSON, read one item from a 1024-entry in-memory map, calculate a few values from the numeric array, and return JSON.

Before the measured part starts, the runner preheats each server/parser combination. For the measured run I use a fixed shuffled order (`seed = 20260507`) instead of the enum order, so the same variant is not always first or last. Request objects are built before the timer starts, so the reported latency does not include client-side JSON string construction.

## Hardware description

| Component | Value |
|-----------|-------|
| CPU | AMD Ryzen 5 5600H |
| Logical processors | 12 |
| RAM | 15.4 GiB |
| GPU | not relevant for this benchmark |
| Disk | 341.2 GiB on Data |
| OS | Windows 11 10.0 |
| JDK | 21.0.5 |

## Experiment parameters

| Parameter | Value |
|-----------|-------|
| Host | `127.0.0.1` |
| Base port | `18080` |
| Server threads | `12` |
| Client threads | `64` |
| JVM preheat requests | `500` per endpoint/variant |
| Warmup requests | `500` per endpoint/variant |
| Measured requests | `5000` per repeat |
| Repeats | `3` |
| Variant order seed | `20260507` |
| Measured variant order | `Classic + GSON -> Virtual + own parser -> Classic + own parser -> Virtual + GSON` |
| Payload numbers | `40` numeric values per request |
| Load generator | custom Java client using `java.net.http.HttpClient` |

## Resulting table

Average client-side latency per request, in milliseconds. The table uses the average of 3 measured repeats.

| req | Virtual + own parser | Virtual + GSON | Classic + own parser | Classic + GSON |
|-----|----------------------|----------------|----------------------|----------------|
| Request-1 | 100.379 ms | 100.461 ms | 99.866 ms | 101.831 ms |
| Request-2 | 16.066 ms | 17.115 ms | 16.320 ms | 16.539 ms |

Detailed CSV: `3/results/latest-detailed.csv`.
Summary CSV: `3/results/latest-summary.csv`.
Last full run: `2026-05-07T11:09:16.7647054`.
