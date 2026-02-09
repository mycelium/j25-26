Для обучения и сохранения модели: 
```aiignore
gradle run
```

Для запуска обученной модели и получение предсказания:
```aiignore
gradle run --args="predict path/to/digit.png"
```

Запуск предсказания уже существующей картинки в репозитории:
```aiignore
gradle run --args="predict 7.png"
```