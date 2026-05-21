# 2. HTTP Server

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

# Простой HTTP сервер на Java

Библиотека для создания встраиваемого HTTP сервера с поддержкой основных методовcd и обработчиков запросов.

## Сборка и запуск

1. Откройте терминал и перейдите в папку с проектом:

    Пример для Windows (cmd):
    ```bash
    cd ./tasks/term-2/2
    ```

2. Скомпилируйте все исходные файлы:

    Пример для Windows (cmd):
    ```bash 
    javac httpserver\*.java Main.java
    ```

3. Запустите программу:

    ```bash
    java Main
    ```