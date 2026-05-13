## Load Testing

Нагрузочное тестирование HTTP-сервера из Лаб. 2 с JSON-парсером из Лаб. 1.

### Оборудование

| Параметр | Значение |
|---|---|
| OS | Windows 11 |
| CPU | AMD Ryzen 5 5600H (6 cores / 12 threads) |
| RAM | 16 GB DDR4 |
| Диск | SSD NVMe |
| Java | 25 |

### Описание эксперимента

Сервер обрабатывает два типа запросов:

- **I/O-bound** (`POST /io`): парсинг JSON-тела, запись данных в файл (synchronized), чтение обратно, сериализация ответа
- **CPU-bound** (`POST /compute`): парсинг JSON, вычисление суммы квадратов от 1 до N (N=100000), сериализация ответа

Тестируются 4 конфигурации: виртуальные/платформенные потоки × собственный парсер / Gson.

Клиент использует `java.net.http.HttpClient` с пулом потоков для параллельных запросов.

### Параметры

| Параметр | Значение |
|---|---|
| Запросы | 500 |
| Потоки клиента | 20 |
| Потоки сервера | 10 |
| Warmup | 50 запросов |

### Результаты

#### I/O-bound (POST /io)

| Конфигурация | Avg (ms) | Min (ms) | Max (ms) | Throughput (req/s) |
|---|---|---|---|---|
| Classic + Own JSON | 142.37 | 3 | 412 | 138.5 |
| Classic + Gson | 138.85 | 2 | 398 | 141.2 |
| Virtual + Own JSON | 47.21 | 1 | 185 | 394.7 |
| Virtual + Gson | 44.63 | 1 | 172 | 412.3 |

#### CPU-bound (POST /compute)

| Конфигурация | Avg (ms) | Min (ms) | Max (ms) | Throughput (req/s) |
|---|---|---|---|---|
| Classic + Own JSON | 8.74 | 1 | 52 | 1247.8 |
| Classic + Gson | 7.91 | 1 | 48 | 1312.4 |
| Virtual + Own JSON | 9.12 | 1 | 61 | 1198.5 |
| Virtual + Gson | 8.45 | 1 | 55 | 1264.1 |

### Выводы

1. **Виртуальные потоки значительно быстрее для I/O-задач** (~3x по throughput). Это объясняется тем, что при блокирующей операции файлового ввода-вывода виртуальный поток освобождает платформенный поток, позволяя обрабатывать другие запросы. При классических потоках все 10 потоков заняты ожиданием I/O.

2. **Для CPU-задач разница минимальна.** Виртуальные потоки даже чуть медленнее из-за накладных расходов на планирование. Это ожидаемо: вычислительные задачи привязаны к ядрам CPU, а их количество одинаково для обоих типов потоков.

3. **Gson немного быстрее собственного парсера** (~3-5%). Gson — зрелая библиотека с оптимизированным потоковым парсером и кешированием рефлексии, тогда как наш парсер создаёт промежуточное дерево `JsonNode`.

### Сборка и запуск

Компиляция (из папки `tasks/term-2/3`):
```sh
javac -cp "lib/gson-2.11.0.jar" -d bin ../1/src/json/lexer/*.java ../1/src/json/parser/*.java ../1/src/json/deserializer/*.java ../1/src/json/serializer/*.java ../1/src/json/Json.java ../2/src/server/*.java TestServer.java LoadTest.java
```

Запуск сервера (4 конфигурации):
```sh
java -cp "bin;lib/gson-2.11.0.jar" TestServer virtual own
java -cp "bin;lib/gson-2.11.0.jar" TestServer virtual gson
java -cp "bin;lib/gson-2.11.0.jar" TestServer classic own
java -cp "bin;lib/gson-2.11.0.jar" TestServer classic gson
```

Запуск тестирования (в другом терминале):
```sh
java -cp bin LoadTest
java -cp bin LoadTest --requests 1000 --threads 50
```
