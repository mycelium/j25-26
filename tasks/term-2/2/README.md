# HTTP Server Library

## Что делает программа

Библиотека для создания HTTP сервера на чистой Java:

- Поддержка HTTP методов: GET, POST, PUT, PATCH, DELETE
- Доступ к заголовкам (headers) через Map
- Доступ к телу запроса (body)
- Конфигурируемый host и port
- Добавление обработчиков на конкретный путь и метод
- Многопоточная обработка запросов
- Конфигурируемое количество потоков
- Поддержка Virtual Threads (Java 21+)
- Возврат HTTP ответа с произвольным статусом и телом

Поддерживает: все основные HTTP методы, заголовки без учета регистра, потокобезопасное хранение маршрутов, обработку 404 ошибок, Unicode в теле ответа.

## Сборка и запуск

### 1. Перейдите в папку проекта

```
cd tasks/term-2/2
```

### 2. Скомпилируйте библиотеку

```
javac scr/com/httpserver/*.java
```

### 3. Запустите демонстрацию

```
java -cp scr com.httpserver.Main
```

### 4. Остановите сервер
Нажмите Ctrl + C в терминале

## Примеры использования

### GET запрос

```
curl http://localhost:8081/hello
```
Ответ: ``` Hello, World! ```

### POST запрос

```
curl -X POST http://localhost:8081/data -d "some data"
```
Ответ: ``` Created: some data ```


### PUT запрос

```
curl -X PUT http://localhost:8081/data/123 -d "updated content"
```
Ответ: ``` Updated: updated content ```

### PATCH запрос

```
curl -X PATCH http://localhost:8081/data/123 -d "patched content"
```
Ответ: ``` Patched: patched content ```

### DELETE запрос

```
curl -X DELETE http://localhost:8081/data/123
```
Ответ: ``` Deleted ```

### Несуществующий путь (404) 

```
curl http://localhost:8081/not-exist
```
Ответ: ``` 404 - Not Found ```
