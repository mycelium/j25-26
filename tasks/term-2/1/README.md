# JSON-библиотека на Java

Лёгкая библиотека для сериализации и десериализации JSON без внешних зависимостей.

## Возможности

- Парсинг JSON-строки в типизированный Java-объект
- Парсинг JSON-строки в `Map<String, Object>`
- Сериализация любого Java-объекта в JSON-строку
- Поддержка: примитивы, boxing-типы, строки, массивы, коллекции, вложенные объекты

## Сборка и запуск

1. Перейдите в папку с проектом:

    **Windows (cmd):**
    ```
    cd tasks\term-2\1
    ```

    **Linux / macOS:**
    ```
    cd tasks/term-2/1
    ```

2. Скомпилируйте исходные файлы:

    **Windows (cmd):**
    ```
    javac jsonlib\*.java Main.java
    ```

    **Linux / macOS:**
    ```
    javac jsonlib/*.java Main.java
    ```

3. Запустите программу:
    ```
    java Main
    ```

## Пример использования

```java
// Десериализация в класс
Game game = Json.fromJson(jsonString, Game.class);

// Десериализация в Map
Map<String, Object> map = Json.fromJsonToMap(jsonString);

// Сериализация объекта
String json = Json.toJson(game);
```

## Структура проекта

```
├── jsonlib/
│   ├── Json.java                # Публичное API библиотеки
│   ├── JsonParser.java          # Лексер + парсер (токенизация и разбор)
│   ├── JsonSerializer.java      # Сериализация объектов в JSON-строку
│   ├── JsonMapper.java          # Маппинг разобранных данных в Java-типы
│   └── JsonParseException.java  # Исключение для ошибок парсинга
└── Main.java                    # Демонстрация работы библиотеки
```
