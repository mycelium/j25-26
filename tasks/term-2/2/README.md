# HTTP Server на Java

Сервер построен на `ServerSocketChannel`, принимает обработчики для конкретной пары
`HTTP-метод + путь` и передает пользователю объекты `HttpRequest` и `HttpResponse`.

## Архитектура

- `HttpServer` — хранит маршруты, запускает accept-loop и передает соединения в `ExecutorService`.
- `RequestParser` — внутренний класс для разбора стартовой строки, headers, query parameters, body и multipart parts через `SocketChannel`.
- `ResponseWriter` — внутренний класс для формирования HTTP-ответа, status line, headers и body через `SocketChannel`.
- `HttpRequest` — immutable объект запроса.
- `HttpResponse` — mutable/fluent объект ответа, который изменяется обработчиком.
- `MultipartPart` — объект одной части `multipart/form-data`, включая `name`, `filename`, `contentType`, headers и body.
- `RouteHandler` — функциональный интерфейс пользовательского обработчика.

## Компиляция и запуск

1. Компиляция файлов библиотеки `.java` в `.class`

```bash
javac -d output src\http\*.java
```

2. Сборка `.jar` файла библиотеки

```bash
cd output
```

```bash
jar cvf http-server.jar http\*.class
```

```bash
cd ..
```

3. Компиляция примера

```bash
javac -cp "output/http-server.jar;." Main.java
```

4. Запуск примера

```bash
java -cp "output/http-server.jar;." Main
```

После запуска сервер слушает адрес:

```text
http://localhost:55555
```

Остановка выполняется нажатием `ENTER` в консоли.

## Быстрая проверка

В PowerShell лучше использовать `curl.exe`, чтобы не попасть в алиас
`Invoke-WebRequest`.

```bash
curl.exe http://localhost:55555/hello
```

```bash
curl.exe "http://localhost:55555/greet?name=Aleksei"
```

```bash
curl.exe -X POST http://localhost:55555/echo -d "test body"
```

```bash
curl.exe -X PUT http://localhost:55555/items -d "new item"
```

```bash
curl.exe -X PATCH http://localhost:55555/items -d "patched item"
```

```bash
curl.exe -X DELETE http://localhost:55555/items
```

```bash
curl.exe http://localhost:55555/headers -H "X-Demo: yes"
```

```bash
curl.exe -X POST http://localhost:55555/form -F "name=Aleksei"
```

```bash
curl.exe -X POST http://localhost:55555/upload -F "file=@test.txt"
```

## Публичный API

### `HttpServer`

Главный класс библиотеки.

```java
HttpServer server = new HttpServer("localhost", 55555, 4, false);
```

Также доступен builder API:

```java
HttpServer server = HttpServer.builder()
    .host("localhost")
    .port(55555)
    .threads(4)
    .virtual(false)
    .build();
```

Параметры конструктора:

- `host` - адрес, на котором будет поднят сервер.
- `port` - порт сервера.
- `threads` - количество потоков для обычного фиксированного пула.
- `isVirtual` - тип исполнителя: `false` включает `Executors.newFixedThreadPool`,
  `true` включает `Executors.newVirtualThreadPerTaskExecutor`.

Основные методы:

- `on(HttpMethod method, String path, RouteHandler handler)` - регистрирует
  обработчик для точного пути и HTTP-метода.
- `start()` - открывает `ServerSocketChannel`, запускает цикл приема соединений.
- `stop()` - закрывает серверный канал и завершает пул потоков.
- `routes()` - возвращает копию карты зарегистрированных маршрутов.

Пример регистрации маршрута:

```java
server.on(HttpMethod.GET, "/hello", (request, response) -> {
    response.writeText("Hello, World!");
});
```

### `HttpMethod`

Перечисление поддерживаемых методов:

```java
GET, POST, PUT, PATCH, DELETE
```

Метод `HttpMethod.from(String raw)` преобразует строку из HTTP-запроса в элемент
перечисления.

### `RouteHandler`

Функциональный интерфейс обработчика:

```java
void handle(HttpRequest request, HttpResponse response) throws Exception;
```

Если внутри обработчика возникает исключение, сервер возвращает ответ `500 Internal Server Error`.

### `HttpRequest`

Объект входящего запроса. Доступные методы:

- `method()` - HTTP-метод.
- `path()` - путь без query-строки.
- `version()` - версия протокола из стартовой строки, например `HTTP/1.1`.
- `headers()` - заголовки в виде `Map<String, String>`.
- `query()` - query-параметры из URL.
- `formFields()` - текстовые поля из `multipart/form-data`.
- `parts()` - все части `multipart/form-data`, включая файловые части.
- `body()` - тело запроса в байтах.
- `bodyAsString()` - тело запроса как UTF-8 строка.

Заголовки сохраняются в нижнем регистре, например `content-type` и `content-length`.

### `HttpResponse`

Объект ответа. Доступные методы:

- `status(int code)` - задает HTTP-статус.
- `header(String name, String value)` - добавляет заголовок.
- `headers()` - возвращает read-only карту заголовков ответа.
- `writeBytes(byte[] data)` - записывает тело ответа в байтах.
- `writeText(String text)` - записывает UTF-8 текст и автоматически ставит
  `Content-Type: text/plain; charset=utf-8`, если он еще не задан.

`Content-Length` добавляется автоматически при отправке ответа.

## Поддерживаемая часть HTTP/1.1

Сервер разбирает стартовую строку запроса:

```text
METHOD /path?key=value HTTP/1.1
```

Далее читаются заголовки до пустой строки:

```text
Header-Name: value
```

Тело запроса читается по заголовку `Content-Length`. Это подходит для обычных
`POST`, `PUT`, `PATCH` запросов и простых HTML-форм.

Ответ формируется в формате:

```text
HTTP/1.1 200 OK
Content-Type: text/plain; charset=utf-8
Content-Length: 13

Hello, World!
```

Поддерживаемые коды:

- `200 OK`
- `201 Created`
- `204 No Content`
- `400 Bad Request`
- `401 Unauthorized`
- `403 Forbidden`
- `404 Not Found`
- `405 Method Not Allowed`
- `500 Internal Server Error`

## Multipart form data

Поддержка `multipart/form-data` реализована для текстовых полей формы и файловых частей.
Сервер берет `boundary` из заголовка `Content-Type`, разбивает тело запроса на
части и складывает поля с `Content-Disposition: form-data; name="..."` в
`request.formFields()`. Все части формы также доступны через `request.parts()`;
для файлов там сохраняются `filename`, `content-type`, headers и body в байтах.

Пример:

```bash
curl.exe -X POST http://localhost:55555/form -F "name=Aleksei"
```

```bash
curl.exe -X POST http://localhost:55555/upload -F "file=@test.txt"
```

В обработчике:

```java
String name = request.formFields().getOrDefault("name", "unknown");
```

Пример доступа к файлу:

```java
MultipartPart file = request.parts().get("file");
String filename = file.filename();
byte[] bytes = file.body();
```

## Многопоточность

Основной поток `http-accept` принимает соединения через `ServerSocketChannel`.
Каждое соединение передается в `ExecutorService`.

Обычный режим:

```java
new HttpServer("localhost", 55555, 4, false);
```

В этом режиме используется фиксированный пул на `4` потока.

Режим виртуальных потоков:

```java
new HttpServer("localhost", 55555, 4, true);
```

В этом режиме используется виртуальный поток на каждую задачу, а параметр
`threads` остается частью общей конфигурации конструктора, но не ограничивает
количество виртуальных потоков.
