package com.oussama.sovereignty.application.common;

import java.util.UUID;

public interface StreamCallback {
    default void onStart(UUID chatId) {
    }

    void onNext(String token);
    void onComplete();
    void onError(Throwable throwable);
}
