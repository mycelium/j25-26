# Lab 3: Load Testing Report

## 1. How to configure and launch
1. **Открытие проекта**: моя среда разработки — IntelliJ IDEA 2025.2.1, JDK 25.
2. **Настройка папок**:
    * Кликните правой кнопкой мыши по папке `1` (затем по `2` и `3`)  внутри `term-2` -> `Mark Directory as` -> `Sources Root`.
   
3. **Подключение GSON**:
    * Перейдите в `File` -> `Project Structure` -> `Modules` -> `Dependencies`.
    * Нажмите `+` -> `JARs or directories...` и выберите файл `lib/gson-2.10.1.jar`.
4. **Запуск сервера**:
    * В файле `PerformanceNode.java` настройте флаги `ENABLE_VIRTUAL` (Virtual/Classic) и `ENABLE_GSON` (Parser/GSON).
    * Запустите метод `main`. Сервер поднимется на порту `8083`.
5. **Запуск теста**:
    * В файле `StressClient.java` выберите нужный `ENDPOINT` (`/api/v1/disk-task` или `/api/v1/math-task`).
    * Запустите `StressClient.java`. Результаты (среднее время) появятся в консоли.

## 2. Experiment description
В данной работе исследуется производительность HTTP-сервера при параллельной обработке 1000 запросов (50 конкурентных потоков). Сравниваются два типа задач:
* **Request 1 (I/O Bound)**: Маршрут `/api/v1/disk-task`. Сервер выполняет парсинг JSON, запись в файл `io_storage.dat` и последующее чтение. Основная задержка связана с дисковыми операциями.
* **Request 2 (CPU Bound)**: Маршрут `/api/v1/math-task`. Сервер выполняет математический цикл (сумирование) в памяти. Основная нагрузка ложится на процессор.

Сравнение проводится для четырех комбинаций: виртуальные потоки (Project Loom) против классического пула потоков, и самописный маппер против Google GSON.

## 3. Hardware description
* **OS**: Windows 10
* **CPU**: AMD Ryzen 7 5700g 
* **RAM**: 32 GB

## 4. Experiment parameters
* **Threads (Concurrency)**: 50
* **Requests per thread**: 20
* **Total load**: 1000 requests
* **Payload**: JSON (~60 bytes)

## 5. Resulting Table

| req       | Virtual + own parser | Virtual + GSON | Classic + own parser | Classic + GSON |
|-----------|----------------------|----------------|----------------------|----------------|
| Request-1 | 18,52 ms             | 16,74 ms       | 14,75 ms             | 17,39 ms       |
| Request-2 | 4,89 ms              | 6,69 ms        | 6,79 ms              | 6,31 ms        |