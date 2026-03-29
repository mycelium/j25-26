package ru.derikey.json;

/**
 * Common naming strategies.
 */
public final class FieldNamingStrategies {
    public static final FieldNamingStrategy IDENTITY = name -> name;
    public static final FieldNamingStrategy SNAKE_CASE = name -> name.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    public static final FieldNamingStrategy LOWER_CASE = String::toLowerCase;

    private FieldNamingStrategies() {}
}

