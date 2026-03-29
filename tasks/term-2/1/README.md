# Парсер для JSON на JAVA

Библиотека для парсинга JSON строк в объекты Java и для сериализации объектов
в JSON.

## Сборка и использование
Для сборки библиотеки необходимо использовать следующие команды (находясь в 
корне проекта):

1. Компиляция файлов .java в .class
```bash
javac -d out-test src\main\java\ru\derikey\json\*.java
```
2. Сборка .jar файла библиотеки
```bash
cd out-test
```
```bash
jar cvf json-lib.jar ru\derikey\json\*.class
```

После сборки библиотеку можно использовать, поместив jar файл в ClassPath или 
передав ее напрямую через `-cp`, ниже приведен пример сборки и запуска тестового
файла с использованием написанной библиотеки:
```bash
javac -cp "out-test/json-lib.jar;." src\test\java\Test.java  
```
```bash
java -cp "out-test/json-lib.jar;src/test/java" Test
```