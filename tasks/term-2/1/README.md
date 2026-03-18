# JSON-парсер без зависимостей

Это кастомная библиотека для парсинга JSON, написанная на Java с нуля без внешних зависимостей.

## Описание классов

- **`Lexer.java`**: Сканирует исходную строку JSON и разбивает ее на последовательность токенов (например, `{`, `}`, `"key"`, `123`).
- **`Token.java`**: Представляет один токен, определенный Лексером, и хранит его тип (например, `STRING`, `NUMBER`) и значение.
- **`JsonParser.java`**: Читает поток токенов от Лексера и строит в памяти древовидную структуру объектов `JsonNode`.
- **`JsonNode.java`**: Абстрактный базовый класс для всех узлов в JSON-дереве, позволяющий рассматривать `JsonObject`, `JsonArray` и `JsonPrimitive` как один базовый тип.
- **`JsonObject.java`**: Представляет объект JSON (`{...}`), хранящий карту пар ключ-значение.
- **`JsonArray.java`**: Представляет массив JSON (`[...]`), хранящий список элементов `JsonNode`.
- **`JsonPrimitive.java`**: Представляет простое значение JSON, такое как строка, число, логическое значение или null.
- **`JsonDeserializer.java`**: Преобразует дерево `JsonNode` в стандартные коллекции Java, такие как `Map<String, Object>` и `List<Object>`.
- **`ReflectionDeserializer.java`**: Использует рефлексию Java для автоматического сопоставления дерева `JsonNode` с экземпляром пользовательского класса Java (POJO).
- **`JsonSerializer.java`**: Преобразует объект Java (например, `Map`, `List` или пользовательский класс) обратно в отформатированную строку JSON.
- **`Json.java`**: Основной публичный API (фасад) для библиотеки, предоставляющий простые статические методы для парсинга и сериализации JSON.
- **`Main.java`**: Простое приложение для демонстрации и тестирования функциональности библиотеки JSON.

## Как запустить

1. **Скомпилируйте код:**
   ```sh
   javac -d bin Main.java src/json/Json.java src/json/deserializer/JsonDeserializer.java src/json/deserializer/ReflectionDeserializer.java src/json/lexer/Lexer.java src/json/lexer/Token.java src/json/parser/JsonArray.java src/json/parser/JsonNode.java src/json/parser/JsonObject.java src/json/parser/JsonParser.java src/json/parser/JsonPrimitive.java src/json/serializer/JsonSerializer.java
   ```

2. **Запустите Main класс:**
   ```sh
   java -cp bin Main
   ```
