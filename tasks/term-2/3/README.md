
### Компиляция


```bash
javac -cp "src/gson-2.10.1.jar" -d out $(find src -name "*.java")
```

---

### Запуск сервера


#### Virtual Threads + Custom Parser

```bash
java -cp "out:src/gson-2.10.1.jar" Main virtual own
```

#### Virtual Threads + Gson

```bash
java -cp "out:src/gson-2.10.1.jar" Main virtual gson
```

#### Classic Threads + Custom Parser

```bash
java -cp "out:src/gson-2.10.1.jar" Main classic own
```

#### Classic Threads + Gson

```bash
java -cp "out:src/gson-2.10.1.jar" Main classic gson
```

сервер стартует на

```text
http://localhost:8080
```

---

### Нагрузочные тесты

#### Request-1

```bash
java -cp "out:src/gson-2.10.1.jar" load_test.LoadTest store
```

#### Request-2 

```bash
java -cp "out:src/gson-2.10.1.jar" load_test.LoadTest compute
```

---

### Процедура запуска

Для каждой конфигурации

1. Запустить сервер с нужной конфигурацией 
2. запустить `store` тест
3. запустить `compute` тест

---



| req | Virtual + own parser | Virtual + Gson | Classic + own parser | Classic + Gson |
|---|---:|---:|---:|---:|
| Request-1 (/api/store) | 112.04 ms | 161.92 ms | 206.74 ms | 261.08 ms |
| Request-2 (/api/compute) | 52.76 ms | 50.99 ms | 85.28 ms | 140.71 ms |