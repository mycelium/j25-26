# HTTP Server

Откройте командную строку и перейдите в корень пакетов — папку, внутри которой лежит папка httpserver.

## Компиляция
javac httpserver/*.java

## Запуск
java httpserver.Main

Поддерживает методы GET, POST, PUT, PATCH, DELETE, доступ к заголовкам и телу, а также бонусную обработку multipart/form-data.

## Проверка работоспособности (с помощью curl)

Запустите сервер и в другом окне терминала выполните команды.

1. GET /

curl http://localhost:8080/

Ожидаемый ответ: Hello from HTTP server!

2. POST /register (multipart/form-data)

curl -X POST http://localhost:8080/register \
  -F "name=Ivan" \
  -F "login=ivan123" \
  -F "password=qwerty"
  
Ожидаемый ответ: User registered: Ivan (или аналогичный текст из вашего обработчика)

3. PUT /update

curl -X PUT http://localhost:8080/update

Ожидаемый ответ: Updated

4. PATCH /patch

curl -X PATCH http://localhost:8080/patch

Ожидаемый ответ: Patched

5. DELETE /delete

curl -X DELETE http://localhost:8080/delete

Ожидаемый ответ: Deleted

6. Несуществующий путь (404)

curl http://localhost:8080/unknown

Ожидаемый ответ: Not Found (статус 404)

7. Неправильный метод (например, POST на /)

curl -X POST http://localhost:8080/

Ожидаемый ответ: Not Found (т.к. для / зарегистрирован только GET)

## Настройка многопоточности

Server server = new Server("localhost", 8080, 10, true);
// параметры: host, port, размер пула (для классических), isVirtual
isVirtual = true  // виртуальные потоки (Java 21+)
isVirtual = false // классический фиксированный пул с размером nThreads

