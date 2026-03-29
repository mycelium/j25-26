package ru.derikey.json;

/**
 * Strategy for mapping Java field names to JSON keys.
 */
@FunctionalInterface
public interface FieldNamingStrategy {
    String translateName(String fieldName);
}

