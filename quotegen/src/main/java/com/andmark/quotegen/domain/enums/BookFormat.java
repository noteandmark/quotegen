package com.andmark.quotegen.domain.enums;

public enum BookFormat {
    FB2("fb2"),
    EPUB("epub"),
    DOC("doc"),
    DOCX("docx"),
    PDF("pdf"),
    NOT_FOUND("not_found");

    private final String value;

    BookFormat(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public boolean equalsFormat(String otherFormat) {
        return value.equals(otherFormat);
    }

    @Override
    public String toString() {
        return value;
    }

    public static BookFormat fromString(String text) {
        for (final BookFormat bookFormat : values()) {
            if (bookFormat.value.equalsIgnoreCase(text)) {
                return bookFormat;
            }
        }
        return NOT_FOUND;
    }

}
