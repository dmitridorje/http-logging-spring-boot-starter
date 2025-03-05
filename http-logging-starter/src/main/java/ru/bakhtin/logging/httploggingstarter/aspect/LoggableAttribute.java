package ru.bakhtin.logging.httploggingstarter.aspect;

public interface LoggableAttribute<T> {
    String getMessageTemplate();
    Object getValue(T requestOrResponse);
}

