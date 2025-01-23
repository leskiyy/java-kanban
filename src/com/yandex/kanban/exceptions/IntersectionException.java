package com.yandex.kanban.exceptions;

public class IntersectionException extends RuntimeException {
    public IntersectionException(String message) {
        super(message);
    }

    public IntersectionException() {
    }
}
