package com.andmark.quotebot.domain.enums;

public enum BookStatus {
    ACTIVE("active"), DELETED("deleted");

    private final String value;

    BookStatus(final String value) {
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
