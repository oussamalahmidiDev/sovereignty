package com.oussama.sovereignty.application.common;

public interface StreamCallback {
    void onNext(String token);
    void onComplete();
    void onError(Throwable throwable);
}
