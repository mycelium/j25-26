# Парсер для JSON на JAVA

Библиотека для парсинга JSON строк в объекты Java и для сериализации объектов
в JSON.

## Сборка и использование

1. Компиляция файлов .java в .class
```bash
javac -d output src\jsonlib\*.java
```
2. Сборка .jar файла библиотеки
```bash
cd output
```
```bash
jar cvf json-lib.jar jsonlib\*.class  
```

3. Тестирование и запуск
```bash
javac -cp "output/json-lib.jar;." src\Test\java\Test.java  
```
```bash
java -cp "output/json-lib.jar;src/test/java" Test          
```