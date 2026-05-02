## Реализация HTTP/1.1 сервера без использования сторонних библиотек
Сервер построен на Java NIO. Selector в отдельном потоке принимает входящие соединения и передаёт каждое в пул воркеров, где происходит чтение байт, парсинг запроса и вызов зарегистрированного обработчика.

HttpServer.java — главный публичный класс библиотеки. Содержит Builder для конфигурации (хост, порт, количество потоков, тип потоков), методы start(), stop(), addRoute(), а также вложенный интерфейс Handler для регистрации обработчиков маршрутов.
HttpRequest.java — иммутабельное представление входящего запроса. Содержит метод, путь, заголовки, тело, query-параметры. Здесь же вложенный класс Part для частей multipart/form-data и вся логика парсинга сырых байт в объект запроса.
HttpResponse.java — мутабельный объект ответа с fluent API. Позволяет задать статус, заголовки и тело, после чего сериализует всё в байты для отправки по сети.
HttpRequestParser.java — читает байты из SocketChannel, определяет границу между заголовками и телом по Content-Length, передаёт байты в HttpRequest.parse() и диспетчеризует запрос к нужному обработчику.
HttpMethod.java — enum пяти HTTP-методов с методом fromString() для парсинга строки из request line.
RouteKey.java — внутренний ключ для Map<RouteKey, Handler>. Хранит метод и путь, реализует equals и hashCode для корректной работы в HashMap.
В Main.java приведены примеры регистрации маршрутов для всех поддерживаемых методов, чтения query-параметров, заголовков и multipart-данных.
#### Сборка
./gradlew build
#### Запуск
./gradlew run
#### Примеры ввода (PowerShell)
Invoke-WebRequest -Uri http://localhost:8081/hello

Invoke-WebRequest -Uri http://localhost:8081/echo -Method POST -Body "текст" -ContentType "text/plain"

Invoke-WebRequest -Uri http://localhost:8081/put -Method PUT -Body "данные"

Invoke-WebRequest -Uri http://localhost:8081/patch -Method PATCH -Body "delta"

Invoke-WebRequest -Uri http://localhost:8081/delete -Method DELETE

Invoke-WebRequest -Uri "http://localhost:8081/qs?name=Anyname"
