# HTTP Server
## Запуск сервера
Откройте терминал или командную строку и перейдите в корневую директорию проекта.
### 1. Компиляция
```bash
javac httpserver/*.java Main.java
```
### 2. Запуск
```bash
java Main
```
Сервер запустится на http://127.0.0.1:9876

### 3. Тестирование  
```bash
# GET запрос
curl http://127.0.0.1:9876/api/status

# POST запросы
curl -X POST http://127.0.0.1:9876/api/mirror -d "Hello World!"
curl -X POST http://127.0.0.1:9876/api/upload -F "username=Arto"
curl -X POST http://127.0.0.1:9876/api/form -d "name=Gregory&email=grg@gmail.com"

# PATCH запрос
curl -X PATCH "http://127.0.0.1:9876/api/storage?key=name" -d "Arto"
curl -X PATCH "http://127.0.0.1:9876/api/storage?key=city" -d "SPB"
curl "http://127.0.0.1:9876/api/storage"

# DELETE запрос
curl -X DELETE "http://127.0.0.1:9876/api/storage?key=city"
curl "http://127.0.0.1:9876/api/storage"
```
