#  JSON Parser



### Поддерживаемые типы

**Чтение (JSON → Java):**
- примитивы и обёртки: `int`/`Integer`, `long`/`Long`, `double`/`Double`, `float`/`Float`, `boolean`/`Boolean`
- `String`, `null`
- массивы: `int[]`, `double[]`, `boolean[]`, `String[]`, `Object[]` и т. д.
- `List` и другие коллекции
- вложенные классы (рекурсивно)

**Запись (Java → JSON):**
- все типы выше
- `Map` — ключи преобразуются в строки
- `Enum` — сериализуется как строка (`.name()`)
- произвольный объект — поля через рефлексию (`static` и `transient` пропускаются)


## Как запустить

### Требования
- Java 17 или выше
### Шаги

**1. Скомпилировать:**
```bash
javac -d out jsonparser/*.java Main.java
```

**2. Запустить:**
```bash
java -cp out Main
```

### Ожидаемый вывод

```
=== 1. JSON → Object ===
[hello, 42, true, null]
3.14

=== 2. JSON → Map<String, Object> ===
...

=== 3. JSON → Person.class ===
Person{name=Bob, age=25, active=false, score=7.8, ...}
city      = Riga
hobbies[0]= gaming

=== 4. Object → JSON ===
{"name":"Bob","age":25,...}

... и так далее
```
