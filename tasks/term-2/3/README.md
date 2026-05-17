### 3. Load testing report

- Create a separate project to measure performance of your HTTP server and JSON parser
- You can use load testing frameworks (like JMeter for example) or write your own scripts
- HTTP server from `lab-2` should be configured to:
  - `Request 1`: Accept request, parse JSON,  store something in file (or in database, you could use something like SQLite, but **NOT** in-memory DB), retrieve something from file
  - `Request 2`: Accept request, parse JSON, get something from memory (or perform calculations), create and return JSON
- Combine different variants: Virtual/Classic Threads, your JSON library (`lab-1`) / Jackson or Gson
- It will be good to run load tests on separate machine (one for HTTP server and another for tests)
- The report must contain:
  - How to configure and launch (README)
  - Experiment description
  - Hardware description
  - Experiment parameters (number of threads, number of requests, amount of data etc)
  - resulting table
- The table with results must contains:

## Сборка и запуск

### Требования
- Java 21+ (для поддержки виртуальных потоков; сборка и тестирование проводились на Java 25)
- `gson-2.10.1.jar` — скачать с [Maven Central](https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar)

### Сборка

Перейти в корневую папку проекта `3\`

Далее нужно скомпилировать файлы с использованием библиотек из папки `lib\`
```bash
javac -cp "lib/*" src/server/PerformanceServer.java src/test/LoadTest.java
```

Теперь необходимо запустить сервер
```bash
java -cp "lib/*;src" server/PerformanceServer <port> <virtual|classic> <own|gson> <poolSize>

java -cp "lib/*;src" server/PerformanceServer 8080 classic own 16
```

Аргументы:

| Аргумент          | Описание                                          |
|-------------------|---------------------------------------------------|
| `port`            | Порт, на котором будет слушать сервер             |
| `virtual/classic` | Тип потоков (виртуальные / классические)          |
| `own/gson`        | JSON-парсер (собственный из 1 / Gson)             |
| `poolSize`        | Размер пула потоков (используется при `classic`)  |

После запуска сервера можно проводить тестирование
```bash
java -cp "lib/*;src" test/LoadTest <host> <port>

java -cp "lib/*;src" test/LoadTest http://localhost 8080
```

---

### Описание эксперимента

Сравниваются **4 конфигурации**:

| # | Потоки    | JSON-парсер |
|---|-----------|-------------|
| 1 | Virtual   | Собственный |
| 2 | Virtual   | Gson        |
| 3 | Classic   | Собственный |
| 4 | Classic   | Gson        |

Для каждой конфигурации измеряется среднее время обработки двух типов запросов:

**Request-1 (I/O-bound)** — `POST /io`
```json
{"data":"..."}
```
Сервер парсит JSON, записывает поле `data` в уникальный временный файл (имя содержит UUID), читает файл обратно, удаляет его и возвращает:
```json
{"stored":"...","retrieved":"..."}
```

**Request-2 (compute-bound)** — `POST /compute`
```json
{"a":...}
```
Сервер парсит JSON, вычисляет сумму квадратов чисел от 1 до `a` с кэшированием результата в `ConcurrentHashMap`, и возвращает:
```json
{"result":...}
```

### Аппаратное обеспечение

- **Операционная система**: Windows 11
- **Процессор**: Intel Core i7
- **JVM**: Java 23.0.1
- **Оперативная память**: 16 ГБ

(запуск производился на одном ПК)

### Параметры эксперимента

| Параметр                                  | Значение  |
|-------------------------------------------|-----------|
| Прогревочные запросы                      | 1 000     |
| Измеряемые запросы                        | 5 000     |
| Конкурентность (клиентских потоков)       | 30        |
| Размер поля `data` в Request-1            | 512 байт |
| Аргумент `a` в Request-2                  | 10 000    |
| Размер классического пула потоков сервера | 100       |

### Таблица результатов

| req       | Virtual + own parser | Virtual + GSON | Classic + own parser | Classic + GSON |
|-----------|----------------------|----------------|----------------------|----------------|
| Request-1 | 35.59 ms             | 35.48 ms       | 29.37 ms             | 32.23 ms       |
| Request-2 | 11.21 ms             | 10.31 ms       | 10.13 ms             |  9.10 ms       |