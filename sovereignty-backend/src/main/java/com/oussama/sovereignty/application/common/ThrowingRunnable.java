package com.oussama.sovereignty.application.common;

@FunctionalInterface
public interface ThrowingRunnable {
    void run() throws Exception;
}
