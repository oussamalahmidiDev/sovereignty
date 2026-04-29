package com.oussama.sovereignty.infrastructure.adapters.out.storage;

import com.oussama.sovereignty.application.ports.out.DocumentStoragePort;

public class S3DocumentStorage implements DocumentStoragePort {

    @Override
    public byte[] load(String fileName) {
        return new byte[0];
    }

    @Override
    public void store(byte[] content, String fileName) {

    }

    @Override
    public void delete(String fileName) {

    }
}
