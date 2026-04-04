# JSON parser

Откройте командную строку и перейдите в корень пакетов — папку, внутри которой лежит папка parser.

## Компиляция
javac parser/*.java

## Запуск
java parser.Main

## Поддерживаемые возможности

### Сериализация (объект -> JSON)

- Примитивные типы (int, long, double, float, boolean, byte, char, short)

- Классы-обёртки (Integer, Long, Double, Float, Boolean, Byte, Character, Short)

- Строки (String)

- Массивы (любых типов, включая примитивные)

- Коллекции (Collection, List, Set – как JSON-массивы)

- Вложенные объекты (рекурсивная сериализация)

- Map (преобразуется в JSON-объект)

- null -> null в JSON

### Десериализация (JSON -> объект)

- JSON -> Map<String, Object>

- JSON -> объект указанного класса (с поддержкой вложенных объектов, коллекций, массивов)

- JSON-массивы -> Java-массивы (в т.ч. примитивные)

- JSON-массивы -> List

- Числа -> соответствующие числовые типы (int, long, double и т.д.)

- true/false -> boolean

- null -> null

