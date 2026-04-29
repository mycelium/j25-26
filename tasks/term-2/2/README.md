# 2. HTTP Server
## Настройка 
1. Для того чтобы изменить порт, на котором запускается сервер необходимо изменить порт 8085 на необходимый  в данной строке файла Main: Server server = new Server("localhost", 8085, 4, true);

2. Для виртуальных потоков последний параметр  должен быть true (sVirtual = true): Server server = new Server("localhost", 8085, 4, true);

3. Для фиксированного пула потоков   последний параметр  должен быть false (sVirtual = false), а параметр перед ним равен числу потоков: Server server = new Server("localhost", 8085, 4, false);


## Компиляция 
  1. В командной строке перейти в корневую папку проекта  ...\term-2\2.
  2. В данной папке выполнить через терминал команду javac src/myhttpserver/*.java 

## Запуск
  1. В командной строке перейти в корневую папку проекта  ...\term-2\2 .
  2. В данной папке выполнить через терминал команду java -cp src myhttpserver.Main
  3. После данной команды сервер запустится на порту 8085.

## Проверка работоспособности 
Не закрывая терминал с запущенным сервером, необходимо открыть второй термианал для выполнения команд.
### Метод Get
1. Команда:  curl.exe -i http://localhost:8085/

Результат: Hello world from server!

2. Команда:  curl.exe -i "http://localhost:8085/hello?name=Alena"

Результат: Hello, Alena!

3. Команда:  curl.exe -i "http://localhost:8085/hello"

Результат: Hello, user_name!

### Метод Post
1. Команда:  curl.exe -i -X POST http://localhost:8085/register -F "name=Petr"  -F "login=Petr80" -F "password=password"

Результат: User registered: Petr
В директории появится файл users.txt с записью Petr Petr80 password.

2. Команда:  curl.exe -i -X POST http://localhost:8085/register -F "name=Petr"

Результат: HTTP/1.1 400 Bad Request
           Missing fields

### Метод Put
1. Команда:  curl.exe -i -X PUT http://localhost:8085/update -d "new info"

Результат: Updated with body (length 8): new info

2. Команда:  curl.exe -i -X PUT http://localhost:8085/update

Результат: Nothing to update


### Метод Patch
1. Команда:  curl.exe -i -X PATCH http://localhost:8085/patch

Результат: Patched

### Метод Delete
1. Команда:  curl.exe -i -X DELETE http://localhost:8085/delete


Результат: Deleted

### Несуществующий путь
1. Команда:  curl.exe -i http://localhost:8085/unknown


Результат: HTTP/1.1 404 Not Found
           Not Found
