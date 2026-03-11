package org.example.dataset;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс для чтения датасета отзывов из CSV-файла.
 * Из каждой строки файла извлекается текст отзыва и его метка тональности
 */
public class DatasetReader
{
    /**
     * Читает CSV-файл с отзывами и возвращает список объектов Review.
     *
     * @param path путь к файлу датасета
     * @return список отзывов
     */
    public static List<Review> read(String path) throws IOException
    {
        List<Review> reviews = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(path)))
        {
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null)
            {
                String[] parts = parseLine(line);
                reviews.add(new Review(cleanText(parts[0]), parts[1]));
            }
        }

        return reviews;
    }

    /**
     * Разбирает строку CSV и разделяет её на текст отзыва и метку тональности.
     *
     * @param line строка из CSV-файла
     * @return массив из двух элементов: текст отзыва и метка
     */
    private static String[] parseLine(String line)
    {
        int lastComma = line.lastIndexOf(',');

        String review = line.substring(0, lastComma);
        String sentiment = line.substring(lastComma + 1).trim();

        if (review.startsWith("\"") && review.endsWith("\""))
            review = review.substring(1, review.length() - 1);

        review = review.replace("\"\"", "\"");

        return new String[]{review, sentiment};
    }

    /**
     * Очищает текст от HTML-разметки и лишних пробелов.
     *
     * @param text исходный текст отзыва
     * @return очищенный текст
     */
    private static String cleanText(String text)
    {
        text = text.replace("<br /><br />", " ");
        text = text.replace("<br />", " ");
        text = text.replace("<br>", " ");
        text = text.replaceAll("\\s+", " ");
        return text.trim();
    }
}