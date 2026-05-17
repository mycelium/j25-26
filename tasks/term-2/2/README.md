### 2. HTTP Server

- Do not use external libraries
- Implement part of HTTP 1.1 protocol using ServerSocketChannel (java.nio)
- Methods:
  - GET
  - POST
  - PUT
  - PATCH
  - Delete
- Headers (should be accessible as Map)
- Body
  - Bonus: multipart form data
- Your library should support:
  - Create and httpserver on specified host+port
  - Add listener to specific path and method
  - Access to request parameters (headers, method, etc)
  - Process HttpResponse
- Your library should support multi-threading
  - Number of thread should be configurable
  - Add boolean parameter: `isVirtual` - type of Executor
- It should be a library, so all interactions and configurations should be made through public API

### Сборка

Необходимо скомпилировать все файлы библиотеки

```bash
javac httpserver\*.java
```

Теперь можно создать .jar архив библиотеки

```bash
jar cvf Task2HttpServerLib.jar httpserver/*.class
```

Теперь нужно скомпилировать тестовый пример с использованием созданной библиотеки

```bash
javac -cp "Task2HttpServerLib.jar;." Main.java
```

Запуск Main
```bash
java -cp "Task2HttpServerLib.jar;." Main
```