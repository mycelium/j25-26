# JSON Parser Library

## Что делает программа

Библиотека для парсинга JSON в Java:

- JSON → Map<String, Object>
- JSON → Java объект (указанного класса)
- Java объект → JSON строка

**Поддерживает:** примитивы, обертки, строки, null, массивы, коллекции, вложенные объекты.

## Сборка и запуск

### 1. Перейдите в папку проекта
```bash
cd tasks\term-2\1
```

### 2. Скомпилируйте
```bash
javac -d out src/main/java/com/jsonparser/*.java src/main/java/com/jsonparser/core/*.java src/main/java/com/jsonparser/model/*.java src/main/java/com/jsonparser/exception/*.java
```

### 3. Запустите
```bash
java -cp out com.jsonparser.Main
```
