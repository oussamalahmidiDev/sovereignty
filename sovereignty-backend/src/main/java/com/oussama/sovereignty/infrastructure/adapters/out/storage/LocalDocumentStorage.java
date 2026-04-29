package com.oussama.sovereignty.infrastructure.adapters.out.storage;

import com.oussama.sovereignty.application.ports.out.DocumentStoragePort;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RequiredArgsConstructor
@Slf4j
public class LocalDocumentStorage implements DocumentStoragePort {

    private final Path rootLocation;

    @PostConstruct
    private void init() {
        try {
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
                log.info("Files will be uploaded to : {}", rootLocation);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initiate directory", e);
        }
    }

    @Override
    public byte[] load(String fileName) {
        try {
            return Files.readAllBytes(this.rootLocation.resolve(fileName));
        } catch (IOException e) {
            throw new RuntimeException("Cannot read the file : " + fileName, e);
        }
    }

    @Override
    public void store(byte[] content, String fileName) {
        try {
            Files.write(this.rootLocation.resolve(fileName), content);
        } catch (IOException e) {
            throw new RuntimeException("Error while reading the file : " + fileName, e);
        }
    }

    @Override
    public void delete(String fileName) {
        try {
            Files.deleteIfExists(this.rootLocation.resolve(fileName));
        } catch (IOException e) {
            throw new RuntimeException("Error while deleting the file : " + fileName, e);
        }
    }
}
