package com.github.wrapper.ethrereum.exceptions;

public class NonceException extends RuntimeException {

    private int code;

    public NonceException() {
    }

    public NonceException(String message) {
        super(message);
    }

    public NonceException(int code, String message) {
        super(message);
        this.code = code;
    }
}
