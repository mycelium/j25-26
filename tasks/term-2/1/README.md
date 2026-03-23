# Term 2 / Task 1: JSON Library

Custom JSON parser/serializer in Java without external JSON libraries.

## Project structure

```text
1/
+-- Main.java
+-- README.md
`-- library/
    `-- json/
        +-- Json.java
        +-- JsonConfig.java
        +-- JsonMapper.java
        +-- JsonParser.java
        +-- JsonWriter.java
        +-- ReflectionUtils.java
        +-- TypeConverter.java
        `-- exceptions/
            +-- JsonException.java
            +-- JsonParseException.java
            `-- JsonMappingException.java
```

`Json`, `JsonMapper` and `JsonConfig` form the public API. The helper classes
inside package `json` are package-private implementation details and are not
intended for direct use from outside the library.

## Features

- Parse JSON string into generic Java structure (`Object`)
- Parse JSON string into `Map<String, Object>`
- Parse JSON string into a Java class using reflection
- Serialize Java objects into JSON string

Supported:

- Primitive fields and boxing types
- `String`, `Character`, `Boolean`
- `null`
- Primitive arrays and object arrays
- Collections
- Maps with JSON-compatible keys
- Nested POJO objects
- Enums

## Limitations

- Cyclic object references are not supported when cycle detection is enabled
- `NaN` and `Infinity` cannot be serialized to JSON
- Unsupported `Map` key types are rejected during serialization
- For object mapping, target classes should have an accessible no-arg constructor

## Public API

- `json.Json` for static convenience methods
- `json.JsonMapper` for configurable read/write operations
- `json.JsonConfig` for mapper configuration

## Exceptions

- `json.exceptions.JsonException` is the base runtime exception
- `json.exceptions.JsonParseException` is used for JSON syntax errors
- `json.exceptions.JsonMappingException` is used for object conversion and serialization errors

## Quick example

```java
import json.Json;
import json.JsonConfig;
import json.JsonMapper;

String json = "{\"name\":\"Alice\",\"age\":21}";
User user = Json.parse(json, User.class);

JsonMapper mapper = Json.mapper(JsonConfig.builder()
        .includeNullFields(false)
        .failOnUnknownProperties(true)
        .build());

String out = mapper.write(user);
```

See `Main.java` for a full runnable example.

## Compile and run

Compile in PowerShell:

```powershell
javac -d out Main.java (Get-ChildItem -Recurse -Path library -Filter *.java | ForEach-Object { $_.FullName })
```

Run:

```powershell
java -cp out Main
```
