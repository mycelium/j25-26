### 1. JSON parser

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

### Сборка

Необходимо скомпилировать все фалйы библиотеки

```bash
javac json\*.java
```

Теперь можно создать .jar архив библиотеки

```bash
jar cvf Task1JsonLib.jar json/*.class
```

Теперь нужно скомпилировать тестовый пример с использованием созданной библиотеки

```bash
javac -cp "Task1JsonLib.jar;." Main.java
```

Запуск Main
```bash
java -cp "Task1JsonLib.jar;." Main
```