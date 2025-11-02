package com.jmcore.core.command;

public interface DependencyProvider {
    <T> T get(Class<T> type);
}