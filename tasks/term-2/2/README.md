# HTTP Server Library

### Сборка и запуск

1. Переход в корень проекта:
```bash
cd tasks\term-2\2
```

2. Компиляция:
```bash
javac -d out src/main/java/com/webserver/*.java src/*.java
```

3. Запуск:
```bash
java -cp out com.webserver.Main
```

### Тестирование
Откройте новое окно терминала, не закрывая сервер.

#### GET запрос
```bash
curl http://localhost:8081/hello
```
Ответ: `Hello, World!`

#### POST запрос
```bash
curl -X POST -d "test data" http://localhost:8081/data
```
Ответ: `Created: test data`

#### PUT запрос
```bash
curl -X PUT -d "new value" http://localhost:8081/data/123
```
Ответ: `Updated: new value`

#### PATCH запрос
```bash
curl -X PATCH -d "patch data" http://localhost:8081/data/123
```
Ответ: `Patched: patch data`

#### DELETE запрос
```bash
curl -X DELETE http://localhost:8081/data/123
```
Ответ: `Deleted`

#### Просмотр всех данных:
```bash
curl http://localhost:8081/data
```
Ответ: `All data`

#### Просмотр заголовков ответа:
```bash
curl -i http://localhost:8081/hello
```
Ответ:
```bash
HTTP/1.1 200 OK
Content-Length: 13

Hello, World!
```

#### Несуществующий путь:
```bash
curl http://localhost:8081/unknown
```
Ответ: `404 - Not Found`

Остановить сервер: `Ctrl+C` в окне с сервером.

## Особенности реализации

### Принцип работы

1. Сервер запускается на указанном порту и ждёт входящих соединений
2. Каждое соединение обрабатывается в отдельном потоке через `ExecutorService`
3. Парсинг HTTP-запроса вынесен в отдельный класс `RequestParser` (метод, путь, заголовки, тело)
4. Маршрутизация выполняется через класс `Router`, который хранит обработчики по методу и пути
5. Обработчик формирует объект `Response`, который сериализуется и отправляется клиенту

### Структура классов

| Класс | Описание |
|---|---|
| `WebServer` | Основной класс сервера: приём соединений, диспетчеризация |
| `WebServerConfig` | Конфигурация: хост, порт, пул потоков, тип потоков |
| `Router` | Таблица маршрутов: регистрация и поиск обработчиков |
| `RequestParser` | Парсинг сырого HTTP-запроса из `InputStream` |
| `Request` | Иммутабельное представление входящего запроса |
| `Response` | Формирование и сериализация HTTP-ответа |
| `WebServerException` | Исключение для ошибок сервера |
| `Main` | Точка входа с примером использования |
