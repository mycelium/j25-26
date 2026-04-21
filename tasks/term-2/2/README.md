# HTTP Server Library

### Сборка и запуск 

1. Переход в корень проекта:
```bash
cd tasks\term-2\2
```

2. Компиляция:
```bash
javac -d out src/main/java/com/httpserver/*.java src/*.java
```

3. Запуск:
```bash
java -cp out com.httpserver.Main
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

#### С заголовками:
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

1. Сервер запускается на указанном порту и ждет соединений
2. Каждое новое соединение обрабатывается в отдельном потоке
3. Парсится HTTP запрос: метод, путь, заголовки, тело
4. По методу и пути находится соответствующий обработчик
5. Обработчик возвращает ответ, который отправляется клиенту

