# Task 1

## 1. JSON parser

- Do not use external libraries
- Read JSON string
  - To Java Object
  - To Map<String, Object>
  - *To specified class*
- Convert Java object to JSON string
- Library should support
  - Classes with fields (primitives, boxing types, null, arrays, classes)
  - Arrays
  - Collections
- Limitations (you may skip implementation)
  - Cyclic dependencies
  - non-representable in JSON types
- It should be a library, so all interactions and configurations should be made through public API

## Explanation

### Public API

Единая точка входа — класс `Json` (`jsonparser/Json.java`).
Все взаимодействие с библиотекой происходит через его статические методы.

| Требование | Метод |
|---|---|
| JSON → Java Object | `Json.parse(String)` |
| JSON → `Map<String, Object>` | `Json.parseToMap(String)` |
| JSON → указанный класс | `Json.parse(String, Class<T>)` |
| Java объект → JSON строка | `Json.stringify(Object)` |

### Внутренняя реализация

| Класс | Файл | Роль |
|---|---|---|
| `JsonTokenizer` | `jsonparser/JsonTokenizer.java` | Лексер: разбивает строку на токены (`{`, `"str"`, `42`, `true`, `null`, …) |
| `JsonParser` | `jsonparser/JsonParser.java` | Рекурсивный спуск: из токенов строит граф объектов (`Map`, `List`, `String`, `Integer`/`Long`/`Double`, `Boolean`, `null`) |
| `JsonGenerator` | `jsonparser/JsonGenerator.java` | Сериализует произвольный Java-объект в JSON-строку через рефлексию |
| `ObjectMapper` | `jsonparser/ObjectMapper.java` | Преобразует разобранный граф (`Map<String,Object>`) в экземпляр конкретного класса через рефлексию |
| `JsonException` | `jsonparser/JsonException.java` | `RuntimeException` для всех ошибок парсинга и маппинга |
| `TokenType` | `jsonparser/TokenType.java` | Enum типов токенов (package-private, детали реализации) |

### Поддерживаемые типы

**Чтение (`JsonParser` + `ObjectMapper`):**
- примитивы и их обёртки: `int`/`Integer`, `long`/`Long`, `double`/`Double`, `float`/`Float`, `boolean`/`Boolean`
- `String`, `null`
- массивы: `int[]`, `long[]`, `double[]`, `float[]`, `boolean[]`, `Object[]`, `String[]` и т. д.
- `List` и подтипы
- вложенные классы (рекурсивно через `ObjectMapper.fromMap`)

**Запись (`JsonGenerator`):**
- все перечисленные выше типы
- `Map` — ключи конвертируются в строки
- `Enum` — сериализуется как строка (`name()`)
- произвольный объект — поля сериализуются через рефлексию (`static` и `transient` поля пропускаются)