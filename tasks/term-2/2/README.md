# HTTP Server

Для взаимодействия с проектом необходимо открыть терминал и перейти в папку с файлом Main. Запуск сервера производится на порту 8081 localhost.

Доступен по адресу: http://localhost:8081

## Компиляция файлов

javac papka_HTTP/*.java Main.java

## Запуск

java Main

## Тестирование

curl http://localhost:8081/

Ответ: Hi

curl "http://localhost:8081/search?name=Bob&age=41"

Ответ: Поиск: Bob возраст: 41

curl -X POST http://localhost:8081/submit -d "Hello Server" 

curl -X PUT http://localhost:8081/users/update -d "ASA"

curl -X PATCH http://localhost:8081/users/patch -d "age=30"

curl -X DELETE http://localhost:8081/users/delete








