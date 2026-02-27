## Java core tasks

You must complete three tasks below individually in **common repository**. Use your branch from previous term

### 1. JSON parser

- Do not use external libraries
- Read JSON string
  - To Java Object
  - To Map<String, Object>
  - *To specified class*
- Convert Java object to JSON string
- Library should support
  - Classes with fields (primitives, boxing types, null, arrays, classes)
  - Arrays
  - Collections
- Limitations (you may skip implementation)
  - Cyclic dependencies
  - non-representable in JSON types
- It should be a library, so all interactions and configurations should be made through public API

### 2. HTTP Server

- Do not use external libraries
- Implement part of HTTP 1.1 protocol using ServerSocketChannel (java.nio)
- Methods:
  - GET
  - POST
  - PUT
  - PATCH
  - Delete
- Headers (should be accessible as Map)
- Body
  - Bonus: multipart form data
- Your library should support:
  - Create and httpserver on specified host+port
  - Add listener to specific path and method
  - Access to request parameters (headers, method, etc)
  - Process HttpResponse
- Your library should support multi-threading
  - Number of thread should be configurable
  - Add boolean parameter: `isVirtual` - type of Executor
- It should be a library, so all interactions and configurations should be made through public API


### 3. Load testing report

- Create a separate project to measure performance of your HTTP server and JSON parser
- You can use load testing frameworks (like JMeter for example) or write your own scripts
- HTTP server from `lab-2` should be configured to:
  - `Request 1`: Accept request, parse JSON,  store something in file (or in database, you could use something like SQLite, but **NOT** in-memory DB), retrieve something from file
  - `Request 2`: Accept request, parse JSON, get something from memory (or perform calculations), create and return JSON
- Combine different variants: Virtual/Classic Threads, your JSON library (`lab-1`) / Jackson or Gson
- It will be good to run load tests on separate machine (one for HTTP server and another for tests)
- The report must contain:
  - How to configure and launch (README)
  - Experiment description
  - Hardware description
  - Experiment parameters (number of threads, number of requests, amount of data etc)
  - resulting table
- The table with results must contains:

| req       | Virtual + own parser | Virtual + GSON | Classic + own parser | Classic + GSON |
|-----------|----------------------|---------------|----------------------|----------------|
| Request-1 | avg time per request | ...           | ...                  | ...            |
| Request-2 | avg time per request | ...           | ...                  | ...            |
