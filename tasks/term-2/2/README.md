# Java HTTP Server Library

This project is a small HTTP/1.1 server library implemented in pure Java without external dependencies. It uses `ServerSocketChannel` from `java.nio`, supports configurable multithreading, and exposes a public API for registering handlers by HTTP method and path.

## Features

- Supported methods: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`
- Request headers available as `Map<String, String>`
- Request body support
- Query parameter parsing
- `multipart/form-data` parsing
- Exact path + method routing
- Configurable thread count
- Executor type switch with `isVirtual`
- HTTP responses returned from handlers
- Demo `Main` class that shows how to use the library with all required functionality

## Requirements

- Java 21 or newer
- No external libraries

## Project Structure

- `src/httpserver/` - library source files
- `src/Main.java` - example of library usage

## Public API

### `HttpServer`

```java
HttpServer(String host, int port, int threadCount, boolean isVirtual)
void addListener(HttpMethod method, String path, RouteHandler handler)
void start() throws IOException
void stop() throws IOException
```

### `RouteHandler`

```java
HttpResponse handle(HttpRequest request) throws Exception;
```

### `HttpRequest`

Available getters:

- `getMethod()`
- `getPath()`
- `getVersion()`
- `getHeaders()`
- `getQueryParams()`
- `getBody()`
- `getBodyAsString()`
- `getMultipartParts()`

### `HttpResponse`

```java
HttpResponse(int statusCode, Map<String, String> headers, byte[] body)
static HttpResponse text(int statusCode, String body)
```

### `MultipartPart`

Available getters:

- `getName()`
- `getFilename()`
- `getHeaders()`
- `getContent()`
- `getContentAsString()`

## Build

From the project root:

```powershell
javac -d out src\Main.java src\httpserver\*.java
```

## Run

```powershell
java -cp out Main
```

`Main` starts a sample server, registers handlers for all required HTTP methods, shows request header/query/body access, demonstrates multipart handling, and prints example endpoints you can call manually.

The example uses:

- host: `127.0.0.1`
- port: `8080`
- thread count: `4`
- `isVirtual = true`

Press `Enter` in the console to stop the server.

## Example Usage

```java
HttpServer server = new HttpServer("127.0.0.1", 8080, 4, true);

server.addListener(HttpMethod.GET, "/hello", request ->
        HttpResponse.text(200, "Hello, world!"));

server.addListener(HttpMethod.POST, "/echo", request ->
        HttpResponse.text(200, request.getBodyAsString()));

server.start();
```

For a fuller example that includes all required methods and multipart handling, see `src/Main.java`.

## Notes and Limitations

- This is a partial HTTP/1.1 implementation.
- The server processes one request per connection and then closes the socket.
- `Content-Length` is used for request body reading.
- Chunked transfer encoding is not implemented.
- Routing is based on exact paths only.
- If the same header is sent multiple times, the last value is kept in the request header map.
