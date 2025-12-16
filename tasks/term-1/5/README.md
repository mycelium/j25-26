# Sentiment Analysis

Анализ тональности текстовых отзывов с использованием Stanford CoreNLP.

## Подготовка данных

**Скачайте датасет IMDB:**
1. Перейдите на [Kaggle IMDB Dataset](https://www.kaggle.com/datasets/lakshmi25npathi/imdb-dataset-of-50k-movie-reviews)
2. Скачайте файл `IMDB Dataset.csv`
3. Создайте папку `data/` в корне проекта
4. Поместите файл `IMDB Dataset.csv` в папку `data/`

## Запуск

```bash
./gradlew run
```

Аргументы:
- путь к CSV файлу с отзывами (по умолчанию data/IMDB Dataset.csv)
- количество отзывов для анализа (по умолчанию 100)

Пример:
```bash
./gradlew run --args="data/IMDB Dataset.csv 500"
```

Пример с маленьким датасетом (для быстрой проверки):
```bash
./gradlew run --args="app/imdb_small.csv 10"
```

## Формат данных

CSV файл должен содержать отзывы в формате:
```
"Текст отзыва",sentiment
```

Где sentiment: positive, negative, neutral.