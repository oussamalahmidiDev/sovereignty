package com.oussama.sovereignty.infrastructure.adapters.out.parsers;

import com.oussama.sovereignty.application.ports.out.DocumentParserPort;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.Set;

@Component
public class TextDocumentParserAdapter implements DocumentParserPort {

    @Override
    public boolean supports(String extension) {
        return Set.of("txt", "md").contains(extension);
    }

    @Override
    public String parse(byte[] content) {
        return new String(content, Charset.defaultCharset());
    }
}
