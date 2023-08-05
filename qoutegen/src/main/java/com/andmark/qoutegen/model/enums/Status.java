package com.andmark.qoutegen.model.enums;

public enum Status {
    ACTIVE("active"), DELETED("deleted");

    private final String value;

    Status(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

}
