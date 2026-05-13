## HTTP Server

HTTP-сервер на Java с использованием NIO (`ServerSocketChannel`).

### Структура

- `server.HttpServer` — основной сервер, принимает подключения и диспетчеризует запросы
- `server.HttpMethod` — перечисление HTTP-методов (GET, POST, PUT, PATCH, DELETE)
- `server.HttpHandler` — функциональный интерфейс для обработчиков маршрутов
- `server.HttpRequest` — распарсенный HTTP-запрос (метод, путь, заголовки, тело)
- `server.HttpResponse` — HTTP-ответ с телом, заголовками и статус-кодом
- `server.RequestParser` — парсер запроса из `SocketChannel` через `ByteBuffer`

### Возможности

- NIO через `ServerSocketChannel` с `ByteBuffer`
- Виртуальные (`isVirtual=true`) и платформенные потоки
- Все 5 HTTP-методов
- Парсинг query-параметров
- Корректный `Content-Length` (по байтам, не символам)
- Graceful shutdown через `stop()`

### Запуск

```sh
javac -d bin src/server/*.java Main.java
java -cp bin Main
```

### Тестирование

```sh
curl http://localhost:8080/hello
curl http://localhost:8080/greet?name=Ivan
curl -X POST -d "test body" http://localhost:8080/echo
curl -X PUT -d "new data" http://localhost:8080/data
curl -X PATCH -d "partial" http://localhost:8080/data
curl -X DELETE http://localhost:8080/data
```
