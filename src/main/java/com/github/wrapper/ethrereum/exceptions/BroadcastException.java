package com.github.wrapper.ethrereum.exceptions;

public class BroadcastException extends RuntimeException {

    private int code;

    public BroadcastException() {
    }

    public BroadcastException(String message) {
        super(message);
    }

    public BroadcastException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
