# Lab 3 - Нагрузочное тестирование HTTP-сервера и JSON-парсера

### Требования

- Java 25+
- Gradle 8+

## How to configure

```bash
./gradlew serverJar loadTestJar
```

Артефакты появятся в `build/libs/`:
- `performance-server.jar`
- `load-test.jar`

## How to launch

```bash
java -jar build/libs/performance-server.jar <port> <virtual|classic> <own|gson> <poolSize>
```

Все четыре конфигурации:

```bash
# Virtual threads + собственный парсер
java -jar build/libs/performance-server.jar 8080 virtual own 8

# Virtual threads + Gson
java -jar build/libs/performance-server.jar 8080 virtual gson 8

# Classic threads + собственный парсер
java -jar build/libs/performance-server.jar 8080 classic own 8

# Classic threads + Gson
java -jar build/libs/performance-server.jar 8080 classic gson 8
```

Запуск нагрузочного теста

```bash
java -jar build/libs/load-test.jar <host> <port> <concurrency> <warmupRequests> <measuredRequests>
```

Пример:

```bash
java -jar build/libs/load-test.jar localhost 8080 50 1000 5000
```

---

## Experiment description

### Эндпоинты

**Request-1 - I/O-bound (`POST /request1`)**

Сервер принимает JSON с полем `data`, парсит его выбранным парсером, записывает данные в уникальный
временный файл (имя на основе UUID, директория `java.io.tmpdir`), читает файл обратно, удаляет его
и возвращает JSON с количеством прочитанных символов. Операции выполняются на реальной файловой
системе без кеширования.

Тело запроса:
```json
{"data": "<360 символов UUID>", "n": 0}
```

Тело ответа:
```json
{"status": "ok", "value": 360}
```

**Request-2 - compute-bound (`POST /request2`)**

Сервер принимает JSON с полем `n` (случайное число от 20 до 30), вычисляет число Фибоначчи F(n)
итеративно, кеширует результат в `ConcurrentHashMap` (первый вызов - вычисление, последующие -
чтение из памяти), возвращает результат в JSON.

Тело запроса:
```json
{"data": "", "n": 25}
```

Тело ответа:
```json
{"status": "ok", "value": 75025}
```

- Перед измерением выполняется **фаза прогрева**: 1000 запросов на каждый эндпоинт.
- Измеряемая фаза: **5000 запросов** на каждый эндпоинт.
- Запросы отправляются параллельно из **50 конкурентных потоков**.
- Каждый запрос использует уникальный UUID в поле `data` - исключает кеширование на уровне ОС.
- Метрики: среднее время на запрос, минимум, максимум, количество успешных / неуспешных запросов.
- Сервер и клиент запускались на одной машине (localhost).

---

## Hardware description

| Параметр   | Значение                               |
|------------|----------------------------------------|
| CPU        | AMD Ryzen 5 7535HS, 6 ядер / 12 потоков |
| RAM        | 16 GB                             |
| OS         | Windows 10                             |
| JVM        | JDK 25                             |
| Диск       | SSD                                    |
| Сеть       | localhost                              |

---

## Experiment parameters

| Параметр                      | Значение         |
|-------------------------------|------------------|
| Количество запросов (прогрев) | 1000 на эндпоинт |
| Количество запросов (замер)   | 5000 на эндпоинт |
| Конкурентность клиента        | 50 потоков       |
| Размер пула потоков сервера   | 8                |
| Таймаут запроса               | 10 сек           |
| Размер тела Request-1         | ~360 байт        |
| Значение n для Request-2      | 20–30 (случайно) |

---

## Resulting table

| req       | Virtual + own parser | Virtual + Gson | Classic + own parser | Classic + Gson |
|-----------|---------------------:|---------------:|---------------------:|---------------:|
| Request-1 | avg=33.09 ms         | avg=28.14 ms   | avg=25.89 ms         | avg=25.47 ms   |
| Request-2 | avg=22.53 ms         | avg=19.81 ms   | avg=23.53 ms         | avg=22.76 ms   |

Подробная статистика:

| Конфигурация          | Эндпоинт  | avg      | min   | max    | ok   | fail |
|-----------------------|-----------|----------|-------|--------|------|------|
| Virtual + own parser  | Request-1 | 33.09 ms | 3 ms  | 334 ms | 5000 | 0    |
| Virtual + own parser  | Request-2 | 22.53 ms | 1 ms  | 265 ms | 5000 | 0    |
| Virtual + Gson        | Request-1 | 28.14 ms | 3 ms  | 168 ms | 5000 | 0    |
| Virtual + Gson        | Request-2 | 19.81 ms | 2 ms  | 68 ms  | 5000 | 0    |
| Classic + own parser  | Request-1 | 25.89 ms | 3 ms  | 95 ms  | 5000 | 0    |
| Classic + own parser  | Request-2 | 23.53 ms | 2 ms  | 67 ms  | 5000 | 0    |
| Classic + Gson        | Request-1 | 25.47 ms | 3 ms  | 396 ms | 5000 | 0    |
| Classic + Gson        | Request-2 | 22.76 ms | 2 ms  | 343 ms | 5000 | 0    |

---

## Анализ результатов

На localhost с быстрым SSD classic threads оказались незначительно быстрее virtual: преимущество
виртуальных потоков проявляется при высоких задержках I/O (сеть, медленная БД), которых в данном
тесте нет. Gson показал результаты, близкие к собственному парсеру — разница не превышает 15%,
что говорит о сопоставимой производительности реализаций на малых объёмах данных.
Все 5000 запросов в каждой конфигурации завершились успешно (fail=0).

