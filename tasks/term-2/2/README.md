
Вот краткий и готовый к копированию `README.md`.

```markdown
# HTTP Server (Java NIO)

Сервер на чистом Java NIO без внешних библиотек. Поддерживает GET, POST, PUT, PATCH, DELETE и multipart/form-data.

## Запуск

1. **Компиляция:**
   ```bash
   javac org/example/http/*.java
   ```

2. **Запуск сервера:**
   ```bash
   java org.example.http.Main
   ```
   Сервер запустится на `http://localhost:8082`.

## Тестирование методов (curl)

**GET** (Простой запрос)
```bash
curl http://localhost:8082/hello
```

**POST** (Отправка текста)
```bash
curl -X POST http://localhost:8082/echo -d "test data"
```

**PUT** (Обновление ресурса)
```bash
curl -X PUT http://localhost:8082/update -d "full update"
```

**PATCH** (Частичное обновление)
```bash
curl -X PATCH http://localhost:8082/update -d "patch data"
```

**DELETE** (Удаление)
```bash
curl -X DELETE http://localhost:8082/remove
```

**Multipart** (Загрузка файла, бонус)
*Создайте файл `test.txt` перед запуском.*
```bash
curl -X POST http://localhost:8082/upload -F "user=Student" -F "file=@test.txt"
```

**Многопоточность**
*Запустите 3 запроса одновременно, чтобы увидеть разные потоки в консоли сервера:*
```bash
curl http://localhost:8082/long & curl http://localhost:8082/long & curl http://localhost:8082/long
```
```
