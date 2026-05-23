package com.oussama.sovereignty.application.common;

import java.util.concurrent.Callable;
import java.util.function.Function;

public final class ExceptionUtils {

    private ExceptionUtils() {}

    public static <T, E extends RuntimeException> T runOrThrow(Callable<T> action, Function<Throwable, E> exceptionFactory) {
        try {
            return action.call();
        } catch (Exception ex) {
            throw exceptionFactory.apply(ex);
        }
    }

    public static <E extends RuntimeException> void runOrThrow(ThrowingRunnable action, Function<Throwable, E> exceptionFactory) {
        try {
            action.run();
        } catch (Exception ex) {
            throw exceptionFactory.apply(ex);
        }
    }
}
