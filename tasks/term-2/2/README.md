## Компиляция и запуск

```bash
javac -d out Main.java server/Server.java
java -cp out Main
```

## Проверка запросов

### GET

```bash
curl -i http://localhost:8080/health
```

Ответ:

```text
HTTP/1.1 200 OK
Connection: close
Content-Length: 11
Content-Type: text/plain; charset=utf-8

I'm healthy!
```

### POST

```bash
curl -i -X POST http://localhost:8080/message -d "post data"
```

Ответ:

```text
HTTP/1.1 200 OK
Connection: close
Content-Length: 18
Content-Type: text/plain; charset=utf-8

POST: post data
```

### PUT

```bash
curl -i -X PUT http://localhost:8080/message -d "new data"
```

Ответ:

```text
HTTP/1.1 200 OK
Connection: close
Content-Length: 13
Content-Type: text/plain; charset=utf-8

PUT: new data
```

### PATCH

```bash
curl -i -X PATCH http://localhost:8080/message -d "patch data"
```

Ответ:

```text
HTTP/1.1 200 OK
Connection: close
Content-Length: 17
Content-Type: text/plain; charset=utf-8

PATCH: patch data
```

### DELETE

```bash
curl -i -X DELETE http://localhost:8080/message
```

Ответ:

```text
HTTP/1.1 200 OK
Connection: close
Content-Length: 23
Content-Type: text/plain; charset=utf-8

DELETE: message removed
```

### Query parameters

```bash
curl -i "http://localhost:8080/query?name=alex"
```

Ответ:

```text
HTTP/1.1 200 OK
Connection: close
Content-Length: 16
Content-Type: text/plain; charset=utf-8

Query: name=alex
```