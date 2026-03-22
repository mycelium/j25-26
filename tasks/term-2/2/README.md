# HTTP Сервер

## Взаимодействие с программой
### Требования
Java 21+ для поддержки виртуальных потоков, установленный `curl`
### Запуск через jar:

1. Переход в в корень проекта (пример: `cd tasks\term-2\2`)
2. Компиляция:
`javac -d out http/*.java`
3. Создание манифеста: 
`echo Main-Class: http.Main> manifest.txt `
4. Сборка JAR:
`jar cfm myapp.jar manifest.txt -C out .`
5. Запуск:
`java -jar myapp.jar`

### Для ручного тестирования
Не закрывая текущий терминал и не прекращая работы сервера, открыть новое окно терминала. С помощью curl-запросов проверить методы. Чтобы остановить сервер, нужно нажать `Enter` в изначальном окне. 
* `curl -v` покажет полный процесс взаимодействия (> запросы, < ответы)
* `curl -i` покажет вывод с заголовками
* `curl` покажет только тело ответа

#### Запросы и примеры ответов:
1. GET-запрос `curl -i http://localhost:8082/hello` 

вывод: 
```http
HTTP/1.1 200 OK
Content-Length: 16
Content-Type: text/plain; charset=utf-8

GET: Hello World
```

2. POST-запрос `curl -i -X POST http://localhost:8082/data -d "Test POST body"` 

вывод: 
```http
HTTP/1.1 201 Created
Content-Length: 29
Content-Type: text/plain; charset=utf-8

POST received: Test POST body
```

2.1. POST-запрос с json `curl http://localhost:8082/json -H "Content-Type: application/json" -d "{\"name\":\"Lev\"}"`

вывод:
```http
JSON endpoint
Content-Type: application/json
Body: {"name":"Lev"}
```

3. PUT-запрос `curl -i -X PUT http://localhost:8082/update -d "PUT body"` 

вывод:
```http
HTTP/1.1 200 OK
Content-Length: 20
Content-Type: text/plain; charset=utf-8

PUT update: PUT body
```

4. PATCH-запрос `curl -i -X PATCH http://localhost:8082/partial -d "PATCH body"` 

вывод: 
```http
HTTP/1.1 200 OK
Content-Length: 25
Content-Type: text/plain; charset=utf-8

PATCH applied: PATCH body
```

5. DELETE-запрос `curl -i -X DELETE http://localhost:8082/remove` 

вывод: 
```http
HTTP/1.1 200 OK
Content-Length: 36
Content-Type: text/plain; charset=utf-8

DELETE successful - resource removed
```

6. POST-запрос multipart (поле + существующий файл) `curl -i -X POST http://localhost:8082/multipart -F "field1=value1" -F "file1=@a.txt"` 

вывод:
```http
HTTP/1.1 200 OK
Content-Length: 72
Content-Type: text/plain; charset=utf-8

Parsed multipart data:
field1=value1
file1 (file) size=15 bytes
```

7.  POST-запрос multipart (поле + поле) `curl -X POST http://localhost:8082/multipart -F "field1=hello from field 1" -F "field2=hi from field 2"` 

вывод: 
```http
Parsed multipart data:
field1=hello from field 1
field2=hi from field 2
```

8. GET-запрос `curl -i http://localhost:8082/unknown` 

вывод: 
```http
HTTP/1.1 404 Not Found
Content-Length: 43
Content-Type: text/plain; charset=utf-8

Path not implemented
```


## Особенности реализации

### Принцип работы
1. Сервер слушает входящие соединения, пока не вызвана команда `stop()` 
2. Каждое соединение передается обработчику в отдельный поток
3. Обработчик читает весь HTTP-запрос, парсит его и создает объект типа `HttpRequest`
4. В соответствии с методом и путем объекта определяется его `HttpHandler`
5. `HttpHandler` обрабатывает запрос и возвращает объект типа `HttpResponse`
6. Объект преобразуется в формат HTTP и отправляется обратно клиенту.
7. При завершении работы сервера по `stop()` завершаются все активные потоки и прием новых соединений закрывается.
### Классы:
`HttpHandler`: функциональный интерфейс, который обрабатывает маршруты. Он принимает запрос типа `HttpRequest` и возвращает нужный ответ типа `HttpResponse`

`HttpRequest` представляет HTTP-запрос. Он содержит метод, путь, версию HTTP, тело запроса и формы для multipart.

`HttpResponse` представляет HTTP-ответ. Он содержит код, тело и заголовки ответа. Текстовое описание устанавливается автоматически по коду статуса.

`HttpServer` предназначен для управления сервером: он начинает и заканчивает его работу, обрабатывает запросы. Он поддерживает работу с виртуальными потоками.

### Основные методы
`HttpServer(String host, int port, boolean isVirtual, int numThreads)` инициализирует сервер, где 
* `host` - адрес хоста, на котором сервер слушает соединения (например, `localhost`)
* `port` - номер порта для прослушивания входящих соединений
* `isVirtual` - флаг, показывающий, используются ли виртуальные потоки
* `numThreads` - количество потоков в пуле для обработки запросов

`addRoutes(String method, String path, HttpHandler handler)` добавляет новый маршрут в соответствии с методом и используемым путем.

`start()` запускает сервер и начинает прослушивать входящие соединения.

`stop()` завершает текущие процессы, закрывает соединения и останавливает сервер.

`handleClient(SocketChannel client)` читает строку, пришедшую от клиента, отправляет запрос, вызывает обработчик, получает ответ и отправляет его клиенту.
