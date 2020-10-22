package com.github.facade.ethrereum.exceptions;

public class BalanceException extends RuntimeException {

    private int code;

    public BalanceException() {
    }

    public BalanceException(String message) {
        super(message);
    }

    public BalanceException(int code, String message) {
        super(message);
        this.code = code;
    }
}
