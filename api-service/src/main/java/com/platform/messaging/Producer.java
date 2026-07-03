package com.platform.messaging;

public interface Producer<T> {
    void publish(T event);
}
