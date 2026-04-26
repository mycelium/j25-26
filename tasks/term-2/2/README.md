# HTTP Server Library

## Description
HTTP/1.1 server implementation using ServerSocketChannel (Java NIO) without external dependencies.

## Features
- GET, POST, PUT, PATCH, DELETE methods
- Headers accessible as Map
- Request body access
- Configurable thread pool
- Virtual threads support (Java 21+)
- multipart/form-data parsing

## Build and Run

### Compile
```bash
javac src/httpserver/*.java
Run
bash
java -cp src httpserver.Main
curl Examples
GET
bash
curl "http://localhost:8800/welcome?name=Julia"
POST
bash
curl -X POST http://localhost:8800/receive -d "Hello World"
PUT
bash
curl -X PUT http://localhost:8800/replace -d "New data"
PATCH
bash
curl -X PATCH http://localhost:8800/modify -d "Patch content"
DELETE
bash
curl -X DELETE http://localhost:8800/remove -v
Headers
bash
curl -H "User-Agent: MyBrowser/1.0" http://localhost:8800/info
Multipart
bash
curl -X POST http://localhost:8800/upload -F "username=Julia" -F "message=Hello"
404
bash
curl http://localhost:8800/notexist
Configuration
java
Server webServer = Server.configure()
    .address("localhost", 8800)
    .workers(6)
    .virtual(false)
    .build();
Requirements
Java 11+

Java 21+ for virtual threads
