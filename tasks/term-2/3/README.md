# Lab 3: Load Testing Report

## 1. How to configure and launch
1. Открыть проект (у меня в IntelliJ IDEA 2025.2.1, gson 2.10.1, java version "24.0.2" 2025-07-15)
2. Настроить нужный режим в `TestServer.java` (выставить флаги `USE_VIRTUAL_THREADS` и `USE_GSON` в `true`/`false`).
3. Запустить `TestServer.java` для старта сервера.
4. Выбрать целевой маршрут (`/req1` или `/req2`) в переменной `TARGET_PATH` в файле `LoadTester.java`.
5. Во время работы сервера запустить `LoadTester.java` для генерации нагрузки и определения среднего времени ответа.

## 2. Experiment description
Эксперимент сравнивает производительность самописного HTTP-сервера при обработке 1000 запросов.
Мы тестируем два типа операций:
* **Request 1 (Зависит от диска / I/O Bound):** Сервер парсит входящий JSON, записывает данные в физический текстовый файл, читает их обратно из файла и возвращает JSON-ответ.
* **Request 2 (Зависит от процессора / CPU & Memory Bound):** Сервер парсит входящий JSON, выполняет математические вычисления в цикле в оперативной памяти и возвращает сгенерированный JSON с результатом.

Мы сравниваем производительность при использовании классических и виртуальных потоков, а также сравниваем скорость работы самописного JSON-парсера с промышленной библиотекой Google GSON.

## 3. Hardware description
* **OS:** Windows 10
* **CPU:** AMD Ryzen 7 5800U
* **RAM:** 16 GB

## 4. Experiment parameters
* **Number of threads (Concurrent users):** 50
* **Number of requests per thread:** 20
* **Total requests per test:** 1000
* **Amount of data:** Small JSON payloads (~50 bytes)

## 5. Resulting Table

| req       | Virtual + own parser | Virtual + GSON | Classic + own parser | Classic + GSON |
|-----------|----------------------|----------------|----------------------|----------------|
| Request-1 | 12,05 ms             | 15,60 ms       | 14,17 ms             | 14,92 ms       |
| Request-2 | 8,05  ms             | 8,16  ms       | 5,16  ms             | 8,63  ms       |