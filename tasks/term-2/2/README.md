# Task 2

## 2. HTTP Server

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

## Explanation

| Requirement | Class | Method / Field |
|---|---|---|
| HTTP/1.1 via `ServerSocketChannel` (java.nio) | `HttpServer` | `start()` — opens `ServerSocketChannel`, `readRequest()` — reads bytes via `ByteBuffer` |
| Methods GET, POST, PUT, PATCH, DELETE | `HttpServer` | `HttpMethod` enum (nested) |
| Headers accessible as `Map` | `HttpRequest` | `getHeaders()` → `Map<String,String>`, `getHeader(name)` |
| Body | `HttpRequest` | `getBody()`, `getBodyAsString()` |
| Bonus: multipart form-data | `HttpRequest` | `parseMultipart()`, `isMultipart()`, `getMultipartParts()` |
| Each part's fields/filename/content-type | `MultipartPart` | `getName()`, `getFilename()`, `getContentType()`, `getBody()` |
| Create server on host+port | `HttpServerBuilder` | `host()`, `port()`, `build()` |
| Add listener to path and method | `HttpServer` | `addListener(path, method, handler)` |
| Path parameters `{name}` | `HttpServer` | `matchPath()` — extracts params; `HttpRequest.getPathParams()` |
| Query parameters | `HttpRequest` | `getQueryParams()`, `getQueryParam(name)` |
| Process `HttpResponse` | `HttpResponse` | `setStatus()`, `setBody()`, `addHeader()`, `toBytes()` |
| Multi-threading, configurable thread count | `HttpServerBuilder` / `HttpServer` | `threadCount()` → `Executors.newFixedThreadPool(n)` |
| `isVirtual` executor type | `HttpServerBuilder` | `useVirtualThreads(true)` → `Executors.newVirtualThreadPerTaskExecutor()` (Java 21+) |
| Public API (library) | all classes | constructors are package-private; public surface is `HttpServerBuilder`, `HttpServer`, `HttpRequest`, `HttpResponse`, `MultipartPart`, `HttpHandler` |