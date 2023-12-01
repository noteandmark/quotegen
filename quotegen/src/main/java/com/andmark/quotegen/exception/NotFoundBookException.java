package com.andmark.quotegen.exception;

public class NotFoundBookException extends RuntimeException{

    public NotFoundBookException() {
    }

    public NotFoundBookException(String message) {
        super(message);
    }

    public NotFoundBookException(String message, Throwable cause) {
        super(message, cause);
    }
}
