
** Требования **

Java Версия 21 или выше и `curl`


## Компиляция и запуск

Компиляция (из корня проекта) (windows PowerShell):

```bash
javac src\main\java\com\httpserver\api\*.java src\main\java\com\httpserver\core\*.java src\main\java\com\httpserver\Main.java
```

Запуск сервера

```bash
java -cp src\main\java com.httpserver.Main
```


** Работа с программой (Windows PowerShell) **


Оставьте терминал с запущенным сервером открытым. Откройте новое окно терминала, чтобы отправлять запросы с помощью `curl`. Флаг `-i` чтобы видеть ещё и HTTP-заголовки.

### 1: GET-запрос, доступ к Headers и параметрам

```bash
curl.exe -i -X GET "http://127.0.0.1:8080/api/info?id=42" -H "User-Agent: AwesomeTestClient" --output -
```


### 2: POST-запрос и чтение Body

```bash
curl.exe -i -X POST "http://127.0.0.1:8080/api/data" -d "This is a new test record" --output - 
```

### 3: PUT-запрос

```bash
curl.exe -i -X PUT "http://127.0.0.1:8080/api/data" -d "Updated full record" --output - 
```

### 4: PATCH-запрос

```bash
curl.exe -i -X PATCH "http://127.0.0.1:8080/api/data" -d "Partial update data" --output - 
```
### 5: DELETE-запрос

```bash
curl.exe -i -X DELETE "http://127.0.0.1:8080/api/data" --output - 
```

### 6: Multipart Form Data

Для этой проверки сначала создайте пустой текстовый файл в той же папке, откуда запускаете `curl`:

```bash
echo "Hello, this is a file content!" > dummy.txt
```

Далее отправьте форму с текстовым полем и прикрепленным файлом:

```bash
curl.exe -i -X POST "http://127.0.0.1:8080/api/upload" -F "username=admin" -F "document=@dummy.txt" --output - 
```

Ожидаемый результат: Сервер вернет код `200 OK` и распечатает распарсенные части (Parts).


 **Остановка сервера**
 
`Ctrl + C` в окне, где запущен сервер.