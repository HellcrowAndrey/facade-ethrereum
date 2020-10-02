package com.github.wrapper.ethrereum.model;

import java.util.Objects;

public final class Response<T> {

    private int status;

    private String message;

    private T payload;

    private boolean error;

    public Response(int status, String message, T payload, boolean error) {
        this.status = status;
        this.message = message;
        this.payload = payload;
        this.error = error;
    }

    public Response(int status, String message, boolean error) {
        this.status = status;
        this.message = message;
        this.error = error;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getPayload() {
        return payload;
    }

    public boolean isError() {
        return error;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Response<?> response = (Response<?>) o;
        return status == response.status &&
                error == response.error &&
                Objects.equals(message, response.message) &&
                Objects.equals(payload, response.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, message, payload, error);
    }

    @Override
    public String toString() {
        return "Response{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", payload=" + payload +
                ", error=" + error +
                '}';
    }
}
