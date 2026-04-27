# HTTP Server Library

A lightweight, non‑blocking HTTP/1.1 server library written in pure Java 21+ using `java.nio.channels.ServerSocketChannel`.  
It supports the core HTTP methods, multipart form data, configurable threading (including virtual threads).

---

## Features

- **HTTP/1.1** – request/response parsing with persistent connections (keep‑alive)
- **Methods** – `GET`, `POST`, `PUT`, `PATCH`, `DELETE`
- **Headers & Body** – full access to request headers and body as a byte array
- **Multipart Form Data** – parse `multipart/form-data` (files and fields)
- **Configurable Threading** – fixed thread pool or Java 21 virtual threads
- **Fluent API** – builder pattern for easy server configuration

---

## Requirements

- Java 21 or higher (virtual threads require Java 21+)
- Gradle (for building from source)

---

## Installation

### Build from source

Build the JAR:

```bash
./gradlew jar
```
The JAR will be created in build/libs/.

## Use in your project
Copy the JAR into your project’s libs/ folder and add it to your dependencies.
**Gradle (Kotlin DSL)**
```kotlin
dependencies {
    implementation(files("libs/http-server-lib-1.0.0.jar"))
}
```

**Maven**
```xml
<dependency>
    <groupId>com.httpserverlib</groupId>
    <artifactId>http-server-lib</artifactId>
    <version>1.0.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/http-server-lib-1.0.0.jar</systemPath>
</dependency>
```


## Configuration

| Method               |      Description |
|----| ----|
|`host(String host)`      |    Server hostname or IP (default `localhost`)|
|`port(int port) `        |    Listening port (default `8080`)|
|`threadPoolSize(int size)`  | Number of threads (default `10`, ignored when using virtual threads)|
|`useVirtualThreads(boolean)` |Use Java 21 virtual threads (default `false`)|

All settings are optional; the builder returns a fully configured `HttpServer` instance.

## API Reference

### `HttpServer`

- `static Builder create()` – start building a server
- `HttpServer on(HttpMethod method, String path, HttpHandler handler)` – register a handler
- Convenience methods: `get`, `post`, `put`, `patch`, `delete`
- `void start()` – start the server (blocking)
- `void stop()` – gracefully stop the server

### `HttpRequest`

| Method                              |          Description|
|-------------------------------------| --- |
| `HttpMethod getMethod()  `          | HTTP method of the request
| `String getPath() `                   |  Request path (without query string)
| `String getQueryString()`             |  Raw query string (e.g., `name=Alice&age=30`)
| `Map<String,String> getQueryParams()` |  Case‑insensitive map of query parameters
| `String getQueryParam(String name)`   |  Single query parameter value
| `Map<String,String> getHeaders()`     |  Case‑insensitive header map
| `String getHeader(String name)`       |  Single header value
| `byte[] getBody()`                    |  Raw request body
| `String getBodyAsString()`            |  Body decoded as UTF‑8
| `List<Part> getMultipartParts()`      |  Parsed multipart form data (empty if not multipart)
| `boolean  isMultipart()`              | Whether the request is `multipart/form-data`

### HttpResponse

**Static factory methods:**
- `ok()` – 200 OK
- `ok(String body)` – 200 with plain text body
- `ok(byte[] body)` – 200 with binary body
- `created()` – 201 Created
- `noContent()` – 204 No Content
- `badRequest()` – 400 Bad Request
- `notFound()` – 404 Not Found
- `internalServerError()` – 500 Internal Server Error

Fluent methods:
- `status(HttpStatus status)` – set custom status
- `header(String name, String value)` – add a response header
- `body(String body)` – set body as plain text (sets `Content-Type: text/plain`)
- `json(String json)` – set JSON body (sets `Content-Type: application/json`)
- `html(String html)` – set HTML body (sets `Content-Type: text/html`)


# Example Test Commands (using curl)
In `lib/test/java/ru/spbstu` `ExampleServer` is provided. Those examples and answers are from 
running that server.

## GET with query parameter
Command:
```bash
curl "http://localhost:8080/hello?name=Alice"
```
Response:
```bash
< HTTP/1.1 200 OK
< Date: Mon, 27 Apr 2026 16:55:22 GMT
< Server: Java-HTTP-Library/1.0
< Content-Length: 13
< X-Custom-Header: greeting-sent
< Content-Type: text/plain; charset=utf-8
<
Hello, Alice!
```

## GET headers echo
Command:
```bash
curl -H "X-Test: 123" -H "Another: value" http://localhost:8080/headers
```

Result:
```bash
Headers received:
Accept: */*
Another: value
Host: localhost:8080
User-Agent: curl/8.13.0
X-Test: 123
```

## POST JSON

Command:
```bash
curl -X POST http://localhost:8080/echo -H "Content-Type: application/json" -d '{"msg":"Hi"}'
```
Response:
```bash
< HTTP/1.1 200 OK
< Date: Mon, 27 Apr 2026 16:53:11 GMT
< Server: Java-HTTP-Library/1.0
< Content-Length: 26
< Content-Type: application/json; charset=utf-8
<
{"received": "'{msg:Hi}'"}
```


## POST multipart
Command:
```bash
echo "This is a test" > test.txt
curl -X POST http://localhost:8080/upload -F "field=value" -F "file=@test.txt"
```

Result:
```bash
< HTTP/1.1 200 OK
< Date: Mon, 27 Apr 2026 16:57:39 GMT
< Server: Java-HTTP-Library/1.0
< Content-Length: 66
< Content-Type: text/plain; charset=utf-8
<
Part: field =
value
Part: file (file: test.txt, size: 21 bytes)
```


## Post to create
Command:
```bash
curl -X POST http://localhost:8080/items -v
```

Result:
```bash
< HTTP/1.1 201 Created
< Date: Mon, 27 Apr 2026 17:01:47 GMT
< Server: Java-HTTP-Library/1.0
< Location: /items/123
* no chunk, no close, no size. Assume close to signal end
<
```

## PUT
Command:
```bash
curl -X PUT "http://localhost:8080/resource?id=42"
```

Result:
```bash
< HTTP/1.1 200 OK
< Date: Mon, 27 Apr 2026 16:58:16 GMT
< Server: Java-HTTP-Library/1.0
< Content-Length: 19
< Content-Type: text/plain; charset=utf-8
<
Resource 42 updated
```


## DELETE
Command:
```bash
curl -X DELETE "http://localhost:8080/resource?id=42"
```

Result:
```bash
< HTTP/1.1 204 No Content
< Date: Mon, 27 Apr 2026 16:58:31 GMT
< Server: Java-HTTP-Library/1.0
<
```


## Non‑existing path
Command:
```bash
curl -v http://localhost:8080/not-there
```

Result:
```bash
< HTTP/1.1 404 Not Found
< Date: Mon, 27 Apr 2026 17:05:20 GMT
< Server: Java-HTTP-Library/1.0
* no chunk, no close, no size. Assume close to signal end
```