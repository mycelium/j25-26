## Запуск проекта

### Вариант 1.
1. Склонировать репозиторий.
2. Импортировать проект как существующий Java-проект.
3. Открыть и запустить файл `src/ru/lab/json/Main.java`.

### Вариант 2.
Из корневой папки (JsonParser) выполнить две команды:

```bash
javac -d bin src/ru/lab/json/*.java src/ru/lab/json/exception/*.java src/ru/lab/json/mapper/*.java src/ru/lab/json/parser/*.java src/ru/lab/json/serializer/*.java

java -cp bin ru.lab.json.Main