Компиляция и запуск сервера
javac *.java
java Main

Сервер запустится на `http://localhost:8080`

## Тестовые запросы с помощью curl

### GET — query parameters
```bash
curl http://localhost:8080/hello?name=Vika
```
Ожидаемый ответ: `Hello, Vika!`

Если не передать имя:
```bash
curl http://localhost:8080/hello
```
Ожидаемый ответ: `Hello, Guest!`

---

### POST — тело запроса
```bash
curl -X POST http://localhost:8080/data \
  -H "Content-Type: text/plain" \
  -d "some test data"
```
Ожидаемый ответ: `Data received successfully` (статус `201 Created`)

---

### PUT — полное обновление
```bash
curl -X PUT http://localhost:8080/update \
  -H "Content-Type: text/plain" \
  -d "full new data"
```
Ожидаемый ответ: `Resource fully updated`

---

### PATCH — частичное обновление
```bash
curl -X PATCH http://localhost:8080/update \
  -H "Content-Type: text/plain" \
  -d "partial update"
```
Ожидаемый ответ: `Resource partially updated`

---

### DELETE
```bash
curl -X DELETE http://localhost:8080/remove
```
Ожидаемый ответ: статус `204 No Content`

---

### Headers как Map
```bash
curl http://localhost:8080/info \
  -H "User-Agent: MyTestBrowser/1.0"
```
Ожидаемый ответ: `Your Browser: MyTestBrowser/1.0`  
В ответе будет заголовок: `X-Custom-Header: JavaServer-v1`

---

### Многопоточность
```bash
curl http://localhost:8080/long & curl http://localhost:8080/long & curl http://localhost:8080/long &
```
Все три запроса выполнятся параллельно и завершатся примерно через 3 секунды.  
Ожидаемый ответ: `Done! Handled by: pool-1-thread-N`

---

### 404 — несуществующий маршрут
```bash
curl http://localhost:8080/nonexistent
```
Ожидаемый ответ: `404 Page Not Found` (статус `404 Not Found`)

---

### Multipart form data 

Текстовые поля:
```bash
curl -X POST http://localhost:8080/upload \
  -F "username=Vi" \
  -F "age=20"
```
Ожидаемый ответ:
```
Field: username = Vi
Field: age = 20
```

Файл + текстовое поле:
```bash
curl -X POST http://localhost:8080/upload \
  -F "username=Vi" \
  -F "file=@/path/to/file.txt"
```
Ожидаемый ответ:
```
Field: username = Vi
File: file.txt, size: N bytes
```

## Конфигурация потоков
```java
// 10 потоков
HttpServ server = new HttpServ("localhost", 8080, 10, false);

// Виртуальные потоки
HttpServ server = new HttpServ("localhost", 8080, 10, true);
```

При виртуальных потоках имя потока в ответе `/long` будет пустым.

## Структура проекта

| Файл | Описание |
|---|---|
| `HttpServ.java` | Сервер: приём соединений, маршрутизация, многопоточность |
| `HttpReq.java` | Парсинг HTTP запроса: метод, путь, заголовки, тело, query params |
| `HttpRes.java` | Формирование HTTP ответа |
| `MultipartParser.java` | Парсинг multipart/form-data |
| `MultipartPart.java` | Одна часть multipart запроса |
| `Main.java` | Пример использования публичного API |