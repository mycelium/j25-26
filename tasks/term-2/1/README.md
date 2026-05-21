# JSON Parser Library

## Что делает программа

Библиотека для парсинга JSON в Java:

- JSON → Java Object (автоопределение типа)
- JSON → `Map<String, Object>`
- JSON → Java объект (указанного класса)
- Java объект → JSON строка

**Поддерживает:** примитивы, обертки, строки, null, массивы, коллекции, вложенные объекты, Unicode (`\uXXXX`).

## Сборка и запуск

### 1. Перейдите в папку проекта

```
cd tasks/term-2/1
```

### 2. Скомпилируйте библиотеку

```
javac com/jsonparser/Json.java com/jsonparser/core/*.java com/jsonparser/exception/*.java Main.java
```

### 3. Запустите демонстрацию

```
java Main
```