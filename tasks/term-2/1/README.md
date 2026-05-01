4 основные функции в классе `tokenizer.Json`:

- `Json.parse(json)`  
  Читает JSON-строку и возвращает Java-объект (`Map`, `List`, `String`, `Number`, `Boolean`, `null`).

- `Json.parseToMap(json)`  
  Читает JSON-строку и возвращает `Map<String, Object>`.

- `Json.parse(json, Class<T>)`  
  Читает JSON-строку и заполняет объект указанного класса.

- `Json.stringify(obj)`  
  Превращает Java-объект обратно в JSON-строку.


команды компиляции и запуска тестов

```bash
javac Main.java tokenizer/*.java tests/*.java
java tests.JsonLibraryTest
```
