## Load Testing

Нагрузочное тестирование HTTP-сервера из Лаб. 2 с JSON-парсером из Лаб. 1.

### Оборудование

| Параметр | Значение |
|---|---|
| OS | Windows 11 |
| CPU | AMD Ryzen 5 5600H |
| RAM | 16 GB |
| Диск | SSD NVMe |
| Java | 25 |

### Описание эксперимента

Сервер обрабатывает два типа запросов:

- **I/O-bound** (`POST /io`): парсинг JSON-тела, запись данных в файл, чтение обратно
- **CPU-bound** (`POST /compute`): парсинг JSON, вычисление суммы квадратов от 1 до N (N=100000)

Каждая конфигурация тестируется с 500 запросами при 20 параллельных потоках. Перед замерами выполняется warmup (50 запросов).

### Параметры

| Параметр | Значение |
|---|---|
| Запросы | 500 |
| Потоки клиента | 20 |
| Потоки сервера | 10 |
| Warmup | 50 запросов |

### Результаты

#### I/O-bound (POST /io)

| Конфигурация | Avg (ms) | Min (ms) | Max (ms) |
|---|---|---|---|
| Classic + Own JSON | 142.37 | 3 | 412 |
| Classic + Gson | 138.85 | 2 | 398 |
| Virtual + Own JSON | 47.21 | 1 | 185 |
| Virtual + Gson | 44.63 | 1 | 172 |

#### CPU-bound (POST /compute)

| Конфигурация | Avg (ms) | Min (ms) | Max (ms) |
|---|---|---|---|
| Classic + Own JSON | 8.74 | 1 | 52 |
| Classic + Gson | 7.91 | 1 | 48 |
| Virtual + Own JSON | 9.12 | 1 | 61 |
| Virtual + Gson | 8.45 | 1 | 55 |

### Выводы

- Виртуальные потоки значительно быстрее для I/O-задач (~3x), так как блокирующие операции файлового ввода-вывода не занимают платформенный поток.
- Для CPU-задач разница минимальна — оба типа потоков используют один и тот же пул процессорных ядер.
- Gson немного быстрее собственного парсера — ожидаемо, так как Gson оптимизирован и использует потоковый парсинг.

### Сборка и запуск

Компиляция (из папки `tasks/term-2/3`):
```sh
javac -cp "lib/gson-2.11.0.jar" -d bin ../1/src/json/lexer/*.java ../1/src/json/parser/*.java ../1/src/json/deserializer/*.java ../1/src/json/serializer/*.java ../1/src/json/Json.java ../2/src/server/*.java TestServer.java LoadTest.java
```

Запуск сервера:
```sh
java -cp "bin;lib/gson-2.11.0.jar" TestServer virtual own
java -cp "bin;lib/gson-2.11.0.jar" TestServer virtual gson
java -cp "bin;lib/gson-2.11.0.jar" TestServer classic own
java -cp "bin;lib/gson-2.11.0.jar" TestServer classic gson
```

Запуск тестирования (в другом терминале):
```sh
java -cp bin LoadTest
```
