package com.andmark.quotegen.domain.enums;

public enum QuoteStatus {
    FREE("free"),
    PENDING("pending"),
    PUBLISHED("published");

    private final String value;

    QuoteStatus(final String value) {
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
