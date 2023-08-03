    package com.andmark.qoutegen.model.enums;

    public enum BookFormat {
        FB2("fb2"),
        EPUB("epub"),
        DOC("doc");

        private final String format;

        BookFormat(String format) {
            this.format = format;
        }

        public String getFormat() {
            return format;
        }

        public boolean equalsFormat(String otherFormat) {
            return format.equals(otherFormat);
        }

        public String toString() {
            return format;
        }

        public static BookFormat fromString(String text) {
            for (BookFormat format : BookFormat.values()) {
                if (format.format.equalsIgnoreCase(text)) {
                    return format;
                }
            }
            throw new IllegalArgumentException("Invalid BookFormat: " + text);
        }
    }
