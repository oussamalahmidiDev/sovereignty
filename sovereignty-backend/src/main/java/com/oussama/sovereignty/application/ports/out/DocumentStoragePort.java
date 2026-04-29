package com.oussama.sovereignty.application.ports.out;

public interface DocumentStoragePort {
    byte[] load(String fileName);
    void store(byte[] content, String fileName);
    void delete(String fileName);
}
