package com.library_with_kafka.event_consumer.exception;

public class RecoverableException extends RuntimeException {
    public RecoverableException() {
    }

    public RecoverableException(String message) {
        super(message);
    }
}
