package com.oussama.sovereignty.infrastructure.adapters.out.parsers;

import com.oussama.sovereignty.application.ports.out.DocumentParserPort;

public class PdfDocumentParserAdapter implements DocumentParserPort {
    @Override
    public boolean supports(String extension) {
        return "pdf".equals(extension);
    }

    @Override
    public String parse(byte[] content) {
        return "To be implemented...";
    }
}
