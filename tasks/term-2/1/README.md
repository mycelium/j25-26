### Запуск через jar:

1. Переход в в корень проекта (пример: `cd tasks\term-2\1`)
2. Компиляция:
`javac -d out json/*.java`
3. Создание манифеста: 
`echo Main-Class: json.Main> manifest.txt `
4. Сборка JAR:
`jar cfm myapp.jar manifest.txt -C out .`
5. Запуск:
`java -jar myapp.jar`