package com.yandex.kanban.exeptions;

public class IntersectionException extends RuntimeException {
    public IntersectionException(String message) {
        super(message);
    }

    public IntersectionException() {
    }
}
