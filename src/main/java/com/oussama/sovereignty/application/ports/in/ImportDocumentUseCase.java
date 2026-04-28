package com.oussama.sovereignty.application.ports.in;

public interface ImportDocumentUseCase {
    void importDocument(String fileName, String contentType, byte[] content);
}
