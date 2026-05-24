# Lab 2 — HTTP Server

Реализация части HTTP/1.1 без внешних библиотек. Сервер написан под Java 25 и использует `ServerSocketChannel` из `java.nio`.

## Что поддерживается

- создание HTTP-сервера на указанном host + port;
- регистрация обработчиков по HTTP-методу и пути через публичный API;
- методы `GET`, `POST`, `PUT`, `PATCH`, `DELETE`;
- доступ к методу, пути, протоколу, заголовкам и телу запроса;
- чтение тела запроса по `Content-Length`;
- ограничение размера тела запроса: 10 MB;
- частичная поддержка `multipart/form-data`;
- многопоточность;
- выбор типа executor через параметр `isVirtual`:
  - `true` — virtual threads;
  - `false` — fixed thread pool с настраиваемым числом потоков;
- неблокирующий `start()` — accept-loop запускается во внутреннем platform thread;
- `stop()` закрывает `ServerSocketChannel`, останавливает accept-loop и завершает executor;
- корректные ответы `400`, `404`, `405`, `413`, `500`;
- корректная полная запись HTTP-ответа в `SocketChannel` через цикл `while (buffer.hasRemaining())`.

## Требования

- JDK 25
- внешние библиотеки не используются

## Структура публичного API

Основные классы библиотеки:

- `HttpServer` — сервер;
- `HttpRequest` — входящий HTTP-запрос;
- `HttpResponse` — HTTP-ответ;
- `HttpHandler` — функциональный интерфейс обработчика маршрута.

Пример создания сервера:

```java
HttpServer server = new HttpServer("127.0.0.1", 8082, 15, true);

server.registerRoute("GET", "/api/ping", request ->
        new HttpResponse(200, "{\"status\": \"Server is alive\"}"));

Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
server.start();
```

`start()` возвращает управление после успешного запуска accept-thread, поэтому `stop()` можно вызвать из того же потока или через shutdown hook.

## Сборка без Maven

```bash
javac --release 25 -d out $(find src/main/java -name "*.java")
java -cp out lab2.Main
```

## Сборка через Maven

```bash
mvn clean package
java -cp target/classes lab2.Main
```

## Примеры запросов

### GET

```bash
curl -i -X GET http://127.0.0.1:8082/api/ping
```

Ответ:

```http
HTTP/1.1 200 OK
Connection: close
Content-Type: application/json; charset=utf-8
Content-Length: 29

{"status": "Server is alive"}
```

### POST с body

```bash
curl -i -X POST http://127.0.0.1:8082/api/store -d "Some data"
```

Ответ:

```http
HTTP/1.1 201 Created
Connection: close
Content-Type: application/json; charset=utf-8
Content-Length: 33

{"result": "Successfully stored"}
```

В консоли сервера:

```text
POST body: Some data
```

### PUT

```bash
curl -i -X PUT http://127.0.0.1:8082/api/update -d "New full data"
```

Ответ:

```http
HTTP/1.1 200 OK
Connection: close
Content-Type: application/json; charset=utf-8
Content-Length: 30

{"result": "Resource updated"}
```

В консоли сервера:

```text
PUT body: New full data
```

### PATCH

```bash
curl -i -X PATCH http://127.0.0.1:8082/api/modify -d "Small change"
```

Ответ:

```http
HTTP/1.1 200 OK
Connection: close
Content-Type: application/json; charset=utf-8
Content-Length: 30

{"result": "Resource patched"}
```

В консоли сервера:

```text
PATCH body: Small change
```

### DELETE

```bash
curl -i -X DELETE http://127.0.0.1:8082/api/remove
```

Ответ:

```http
HTTP/1.1 200 OK
Connection: close
Content-Type: application/json; charset=utf-8
Content-Length: 30

{"result": "Resource removed"}
```

### Multipart form data

```bash
curl -i -X POST http://127.0.0.1:8082/api/upload -F "title=MyDocument"
```

Ответ:

```http
HTTP/1.1 200 OK
Connection: close
Content-Type: application/json; charset=utf-8
Content-Length: 28

{"savedTitle": "MyDocument"}
```

В консоли сервера:

```text
Multipart fields: {title=MyDocument}
```

### Method Not Allowed

```bash
curl -i -X GET http://127.0.0.1:8082/api/store
```

Ответ:

```http
HTTP/1.1 405 Method Not Allowed
Connection: close
Content-Type: application/json; charset=utf-8
Content-Length: 31

{"error": "Method not allowed"}
```

### Слишком большой Content-Length

```bash
printf 'POST /api/store HTTP/1.1\r\nHost: 127.0.0.1\r\nContent-Length: 999999999\r\n\r\n' | nc 127.0.0.1 8082
```

Ответ:

```http
HTTP/1.1 413 Payload Too Large
Connection: close
Content-Type: application/json; charset=utf-8
Content-Length: 35

{"error": "Request body too large"}
```
