package com.oussama.sovereignty.domain.exception;

public class FatalDocumentProcessingException extends RuntimeException {
    public FatalDocumentProcessingException(String message) {
        super(message);
    }

    public FatalDocumentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
