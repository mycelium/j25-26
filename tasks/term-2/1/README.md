# 1. JSON parser

## Компиляция 
  1. В командной строке перейти в корневую папку проекта  ...\term-2\1 .
  2. В данной папке выполнить через терминал команду javac src/myjson/*.java 

## Запуск
  1. В командной строке перейти в корневую папку проекта  ...\term-2\1 .
  2. Вы данной папке выполнить через терминал команду java -cp src myjson.Main


## Результат предыдущей проверки до исправлений: 
Score: 6/10
  1. Working library with public API: `toJson`, `parse`, `parseToMap`, `parse(String, Class<T>)`.
  2. `InternalParser` is an inner class — reasonable encapsulation.
  3. Handles most types: primitives, boxed types, arrays, collections, nested objects, nulls.
  4. `parseNumber` returns `Long` for integer values — slightly nonstandard but acceptable.
  5. Old-style `switch` statement throughout; no switch expressions.
  6. `parseToMap` has an unchecked cast with no `@SuppressWarnings` annotation.
  7. No `\\uXXXX` escape support.
  8. No modern Java features (no `var`, records, text blocks, pattern matching).
  9. `Main` class tests all scenarios thoroughly.
