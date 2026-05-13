## JSON Parser

Библиотека для парсинга и сериализации JSON на Java.

### Структура

- `json.lexer` — разбивает строку на токены (`Lexer`, `Token`)
- `json.parser` — строит дерево из токенов (`JsonParser`, `JsonNode`, `JsonObject`, `JsonArray`, `JsonPrimitive`)
- `json.deserializer` — преобразует дерево в Map или в объект через рефлексию (`JsonDeserializer`, `ReflectionDeserializer`)
- `json.serializer` — сериализует объекты Java обратно в JSON-строку (`JsonSerializer`)
- `json.Json` — публичный API

### API

- `Json.parseToMap(String)` — парсинг в `Map<String, Object>`
- `Json.parse(String, Class<T>)` — парсинг в объект класса `T`
- `Json.toJson(Object)` — сериализация в строку

### Возможности

- Escape-последовательности: `\"`, `\\`, `\/`, `\n`, `\r`, `\t`, `\b`, `\f`
- Числа: `int`, `long`, `double`
- Массивы Java (`int[]`, `String[]`) и коллекции (`List<T>`)
- Наследование полей (обход суперклассов)
- Пропуск `static` и `transient` полей

### Запуск

```sh
javac -d bin src/json/lexer/*.java src/json/parser/*.java src/json/deserializer/*.java src/json/serializer/*.java src/json/Json.java Main.java
java -cp bin Main
```
