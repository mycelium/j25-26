## Реализация JSON-парсера без использования сторонних библиотек

Поддерживается сериализация и десериализация примитивных типов (int, double, boolean), оберток (Integer, Double, Boolean), String, null, массивов, коллекций (List), Map<String, Object>, порльзовательских классов (через reflection).

В Main.java приведенены примеры с различными поддерживаемыми типами. 

#### Комппиляция

javac -d out src/json/*.java src/Main.java

#### Запуск

java -cp out Main