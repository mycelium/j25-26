# Lab 5: Sentiment Analysis

## Описание

Лабораторная работа по анализу тональности текста с использованием Stanford CoreNLP.

Программа анализирует отзывы из файла и определяет их тональность (positive, negative, neutral).

## Требования

- Java 21
- Gradle 8.14.3 или выше

## Запуск

### Сборка проекта

Unix/Linux/macOS:
```bash
./gradlew build
```

Windows (PowerShell/CMD):
```bash
.\gradlew.bat build
```

### Запуск приложения

Unix/Linux/macOS:
```bash
./gradlew run
```

Windows (PowerShell/CMD):
```bash
.\gradlew.bat run
```

### Запуск с указанием файла

Unix/Linux/macOS:
```bash
./gradlew run --args="путь/к/файлу.txt"
```

Windows (PowerShell/CMD):
```bash
.\gradlew.bat run --args="путь/к/файлу.txt"
```

## Формат входных данных

Программа ожидает файл `short_reviews.txt` в корневой директории проекта или путь к файлу в качестве аргумента командной строки.

Файл должен содержать отзывы, по одному на строку. Первая строка игнорируется (заголовок).

## Зависимости

- Stanford CoreNLP 4.5.7
- Stanford CoreNLP Models 4.5.7

## Выходные данные

Для каждого отзыва программа выводит:
- Текст отзыва
- Определенную тональность (positive, negative, neutral)

