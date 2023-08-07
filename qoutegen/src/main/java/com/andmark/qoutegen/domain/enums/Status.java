package com.andmark.qoutegen.domain.enums;

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
