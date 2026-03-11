package org.example.dataset;

/**
 * Модель отзыва из датасета.
 * Хранит текст отзыва и его метку тональности.
 */
public record Review(String text, String sentiment) {}