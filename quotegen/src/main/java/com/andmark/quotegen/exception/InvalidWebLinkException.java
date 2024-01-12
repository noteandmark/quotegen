package com.andmark.quotegen.exception;

public class InvalidWebLinkException extends RuntimeException {

    public InvalidWebLinkException() {
        super();
    }

    public InvalidWebLinkException(String message) {
        super(message);
    }

    public InvalidWebLinkException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidWebLinkException(Throwable cause) {
        super(cause);
    }
}