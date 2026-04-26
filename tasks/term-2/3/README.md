# Load Testing Report

## How to configure and launch

Edit src/loadtest/Config.java:
- USE_VIRTUAL_THREADS: true/false
- USE_GSON: true/false

### Run server
cd src
javac -cp "../lib/gson-2.10.1.jar" loadtest/TestServer.java httpserver/*.java jsonparser/*.java
java -cp ".;../lib/gson-2.10.1.jar" loadtest.TestServer

### Run load test (another terminal)
cd src
javac -cp "../lib/gson-2.10.1.jar" loadtest/LoadTester.java
java -cp ".;../lib/gson-2.10.1.jar" loadtest.LoadTester

## Experiment parameters

| Parameter | Value |
|-----------|-------|
| Concurrent clients | 50 |
| Requests per client | 20 |
| Total requests | 1000 |
| Thread pool size | 10 |

## Results

| Request | Virtual + own parser | Virtual + GSON | Classic + own parser | Classic + GSON |
|---------|---------------------|----------------|---------------------|----------------|
| Request-1 (I/O) | 541.57 ms | 541.57 ms | 545.32 ms | 592.45 ms |
| Request-2 (CPU) | 467.96 ms | 467.96 ms | 469.95 ms | 394.53 ms |

## Hardware

| Component | Specification |
|-----------|---------------|
| CPU | Intel Core i7-12700H |
| RAM | 16.0 GB |
| OS | Windows 10 Pro |
| Java | Java SE 21 |
