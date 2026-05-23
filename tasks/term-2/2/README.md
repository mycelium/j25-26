# HTTP Server


## Структура

```
server/
  HttpMethod.java        # enum: GET, POST, PUT, PATCH, DELETE
  HttpRequest.java       # парсинг запроса: headers, body, path/query params, multipart
  HttpResponse.java      # ответ: статус, headers, body
  HttpHandler.java       # функциональный интерфейс для хендлеров
  MultipartPart.java     # одна часть multipart/form-data
  HttpServer.java        # ядро: ServerSocketChannel + роутинг
  HttpServerBuilder.java # builder для создания сервера
Main.java                # пример использования
```

## Компиляция и запуск

```bash
# Компилировать
javac -d out server/*.java Main.java

# Запустить
java -cp out Main
```
