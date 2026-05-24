# Lab 1 — JSON Parser

Проект выполнен на Java 25.

## Требования

- JDK 25
- javac 25
- Maven 3.9+ опционально

## Условия задания

- Не используются внешние JSON-библиотеки.
- JSON-строка читается собственным парсером.
- JSON можно преобразовать:
  - в Java Object;
  - в `Map<String, Object>`;
  - в заданный класс.
- Java-объект можно преобразовать обратно в JSON-строку.
- Поддерживаются:
  - классы с полями;
  - primitive types и boxing types;
  - `null`;
  - массивы;
  - вложенные классы;
  - коллекции.
- Циклические зависимости и типы, которые нельзя представить в JSON, не реализованы как допустимое ограничение задания.

## Структура

```text
src/main/java/lab1
├── Address.java
├── Main.java
├── User.java
└── json
    ├── Json.java
    ├── JsonException.java
    ├── JsonMapper.java
    ├── JsonParser.java
    └── JsonSerializer.java
```

Основной публичный API находится в классе `lab1.json.Json`.
Внутренние классы парсера, маппера и сериализатора не являются публичным API библиотеки.

## Компиляция через javac

```bash
javac --release 25 -d out $(find src/main/java -name "*.java")
```

## Запуск

```bash
java -cp out lab1.Main
```

## Компиляция через Maven

```bash
mvn clean package
```

В `pom.xml` указано:

```xml
<maven.compiler.release>25</maven.compiler.release>
```

Это фиксирует компиляцию проекта под Java 25.
