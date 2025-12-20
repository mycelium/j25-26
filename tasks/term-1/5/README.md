### 5. Java: Sentiment Analysis with CoreNLP

- Используя датасет: https://drive.google.com/file/d/15oxF9_ifxKMBs56eUIaziD4nRH3VUV9E
- Используя Java библиотеку: https://mvnrepository.com/artifact/edu.stanford.nlp/stanford-corenlp
- Разработать приложение, которое принимает набор отзывов на фильмы и для каждого из них проставляет оценку:
  - positive
  - negative
  - neutral
  
Запуск с анализом первых 10 отзывов из датасета:

 ./gradlew run
 
 Запуск с желаемым кол-вом анализируемых отзывов (в аргументах укажите целое число):
 
 ./gradlew run --args="20"

Датасет "IMDB Dataset.csv" должен находиться в папке 5/app (там же, где он находится в удаленном репозитории)