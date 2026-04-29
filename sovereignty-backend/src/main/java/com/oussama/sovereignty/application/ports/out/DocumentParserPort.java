package com.oussama.sovereignty.application.ports.out;

public interface DocumentParserPort {
    boolean supports(String extension);
    String parse(byte[] content);
}
