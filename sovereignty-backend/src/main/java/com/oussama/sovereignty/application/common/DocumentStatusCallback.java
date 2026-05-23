package com.oussama.sovereignty.application.common;

public interface DocumentStatusCallback {
    void onStatusChanged(String status);
    void onComplete();
    void onError(Throwable throwable);
}
