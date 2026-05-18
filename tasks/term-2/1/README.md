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

## Сборка и запуск

1. Откройте терминал и перейдите в папку с проектом:

    Пример для Windows (cmd):
    ```
    cd ./tasks/term-2/1
    ```
2. Скомпилируйте все исходные файлы:

    Пример для Windows (cmd):
    ```
    javac jsonlib\*.java Main.java
    ```
3. Запустите программу:
    ```
    java Main
    ```

    