Требования: java 21+


Компиляция из корня проекта:

```bash
javac -cp "libs\gson-2.10.1.jar" src\main\java\com\httpserver\api\*.java src\main\java\com\httpserver\core\*.java src\main\java\org\example\json\*.java src\main\java\com\loadtest\*.java
```

Запуск:

```bash
java -cp "src\main\java;libs\gson-2.10.1.jar" com.loadtest.LoadTestServer
```


Конфигурация экспериментов (переключение между Virtual / Classic Threads и Own Parser / GSON) осуществляется путем изменения констант `USE_VIRTUAL_THREADS` и `USE_GSON` в исходном коде класса `LoadTestServer.java` перед компиляцией.

## 2. Experiment description
Цель эксперимента — сравнить производительность классического пула потоков (`FixedThreadPool`) и Виртуальных потоков (Java 21), а также сопоставить скорость работы самописного JSON-парсера (с использованием Reflection API из Lab 1) и библиотеки `Gson`.

Сервер предоставляет два эндпоинта для разных типов нагрузки:
* **Request-1 (I/O-bound):** Сервер принимает POST-запрос с JSON (объект `DbRecord`), десериализует его, производит синхронную дозапись данных в текстовый файл (`database.txt`) на жестком диске, а затем полностью считывает этот файл в память и возвращает ответ. Имитируется задержка при работе с диском (I/O блокировка).
* **Request-2 (CPU-bound):** Сервер принимает POST-запрос с JSON (объект `CalcPayload` с числом N), десериализует его, рассчитывает N-е число Фибоначчи рекурсивным алгоритмом (намеренно долгие вычисления), формирует новый объект с результатом и сериализует его обратно в JSON-ответ. Имитируется тяжелая нагрузка на процессор.

## 3. Hardware description
* **CPU:** Intel Core i5-12400F
* **RAM:** 16 GB
* **Storage:** NVMe SSD
* **OS:** Windows 11
* **JVM:** Java 21

## 4. Experiment parameters
Нагрузочное тестирование проводилось с использованием инструмента JMeter. Тесты запускались локально.

* **Number of Threads (Concurrent Users):** 100
* **Loop Count (Requests per User):** 10
* **Total Requests per endpoint:** 1000
* **Payload (Request 1):** `{"id": "tester", "data": "load testing"}`
* **Payload (Request 2):** `{"number": 32}`
* **HTTP Keep-Alive:** Enabled
* **Content-Type:** `application/json`

## 5. Resulting Table
В таблице представлено среднее время обработки одного запроса (**Average time per request**) в миллисекундах.

| req                 | Virtual + own parser | Virtual + GSON | Classic + own parser | Classic + GSON |
|---------------------|----------------------|----------------|----------------------|----------------|
| **Request-1 (I/O)** | 817 ms               | 725 ms         | 781 ms               | 737 ms         |
| **Request-2 (CPU)** | 95 ms                | 107 ms         | 105 ms               | 111 ms         |

