package com.oussama.sovereignty.domain.exception;

public class TransientDocumentProcessingException extends RuntimeException {
    public TransientDocumentProcessingException(String message) {
        super(message);
    }

    public TransientDocumentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
