package ru.bakhtin.logging.httploggingstarter.exception;

public class TaskDtoValidationException extends RuntimeException {
    public TaskDtoValidationException(String message) {
        super(message);
    }
}
