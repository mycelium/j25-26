package com.labs.config;


public enum ParserMode {

    OWN("own"),


    GSON("gson");

    private final String label;

    ParserMode(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isOwn() {
        return this == OWN;
    }

    public boolean isGson() {
        return this == GSON;
    }

    @Override
    public String toString() {
        return label.toUpperCase();
    }
}