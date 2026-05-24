package com.labs.config;

public enum ThreadMode {
    VIRTUAL("virtual"),

    CLASSIC("classic");

    private final String label;

    ThreadMode(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
    public boolean isVirtual() {
        return this == VIRTUAL;
    }
    public boolean isClassic() {
        return this == CLASSIC;
    }

    @Override
    public String toString() {
        return label.toUpperCase();
    }
}