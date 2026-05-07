# Term 2 / Task 3: Load Testing Report

This is a Java project for measuring the HTTP server from `lab-2` and the JSON parser from `lab-1`.
The experiment compares virtual/classic server threads and the custom JSON parser/Gson.

## How to configure and launch

Requirements: JDK 21+, PowerShell, Maven Central access for downloading Gson.

Gson version: `2.14.0`;
Maven Central: https://central.sonatype.com/artifact/com.google.code.gson/gson/2.14.0

## Experiment description

For each of the 4 variants, the suite starts the server on a dedicated port and the Java load generator sends `POST` requests through `java.net.http.HttpClient`.
All requests use `Content-Type: application/json; charset=UTF-8` and the same payload shape: id, userId, category, 40 numbers, details, and tags.

- `Request-1`: accepts JSON, parses it with the selected parser backend, writes a JSONL record to `3/runtime/store-<variant>.jsonl`, reads the last line from that file, and returns JSON with a checksum of the retrieved data.
- `Request-2`: accepts JSON, parses it with the selected parser backend, reads an item from a prefilled in-memory map with 1024 entries, performs calculations over the numeric array, and returns JSON.

## Hardware description

| Component | Value |
|-----------|-------|
| CPU | AMD Ryzen 5 5600H with Radeon Graphics, 3.30 GHz |
| Logical processors | 12 |
| RAM | 16.0 GB, 3200 MT/s |
| GPU | AMD Radeon(TM) Graphics, 496 MB |
| Disk | 477 GB |
| OS | Windows 11 10.0 |
| JDK | 21.0.5 |

## Experiment parameters

| Parameter | Value |
|-----------|-------|
| Host | `127.0.0.1` |
| Base port | `18080` |
| Server threads | `12` |
| Client threads | `64` |
| Warmup requests | `500` per endpoint/variant |
| Measured requests | `5000` per repeat |
| Repeats | `3` |
| Payload numbers | `40` numeric values per request |
| Load generator | custom Java client based on `java.net.http.HttpClient` |

## Resulting table

Average time per request, milliseconds. Values are averaged across repeats.

| req | Virtual + own parser | Virtual + GSON | Classic + own parser | Classic + GSON |
|-----|----------------------|----------------|----------------------|----------------|
| Request-1 | 117.425 ms | 106.951 ms | 98.935 ms | 94.491 ms |
| Request-2 | 18.571 ms | 16.735 ms | 16.215 ms | 16.832 ms |

Detailed CSV: `3/results/latest-detailed.csv`.
Summary CSV: `3/results/latest-summary.csv`.
