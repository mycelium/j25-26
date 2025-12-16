## Запуск программы
Используется gradlew (Gradle Wrapper), чтобы обеспечить независимость проекта от окружения.
### Вариант 1: Запуск по умолчанию
По умолчанию программа будет выполняться на файле `testss.csv` и создаст `results.csv`.
```bash
./gradlew run
```
### Вариант 2: Запуск с указанием файлов
Можно передать пути (полные) к входному и выходному файлам как аргументы при запуске.
```bash 
./gradlew run --args="input_file.csv output_file.csv"
```
Также изменен файл build.gradle.kts: в application теперь mainClass.set("lab.sentiment.SentimentAnalyzer") 
