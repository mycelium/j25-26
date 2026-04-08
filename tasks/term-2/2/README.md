# HTTP Server

Простой HTTP/1.1 сервер на чистой Java без внешних библиотек.

## Реализовано

- Методы: GET, POST, PUT, PATCH, DELETE
- Заголовки запроса/ответа (`Map<String, String>`)
- Тело запроса и query-параметры
- Multipart form data (`MultipartParser`)
- Многопоточность с настраиваемым пулом потоков
- Поддержка виртуальных потоков (Project Loom, Java 21+)

## Запуск

```java
SimpleHttpServer server = new SimpleHttpServer("localhost", 8080, 10, false);

server.get("/hello", (req, res) -> res.send("Hello, World!"));
server.post("/echo", (req, res) -> res.send(req.getBody()));

server.start();
```

Четвёртый параметр `isVirtual`:
- `false` — классический `FixedThreadPool` (указанное число потоков)
- `true` — виртуальные потоки (`VirtualThreadPerTaskExecutor`)

## Особенности

- Транспортный слой на `ServerSocketChannel` (java.nio) — неблокирующий I/O
- Лямбда-обработчики маршрутов (`@FunctionalInterface HttpHandler`)
- Ответ поддерживает `send()`, `sendJson()`, `sendHtml()` и явный `status(code)`
