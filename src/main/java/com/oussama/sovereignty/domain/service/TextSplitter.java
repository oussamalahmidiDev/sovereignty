package com.oussama.sovereignty.domain.service;

import java.util.ArrayList;
import java.util.List;

public class TextSplitter {

    private final int chunkSize;
    private final int overlap;

    public TextSplitter(int chunkSize, int overlap) {
        this.chunkSize = chunkSize;
        this.overlap = overlap;
    }

    public List<String> split(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) return chunks;

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            chunks.add(text.substring(start, end));

            // On avance de (chunkSize - overlap) pour créer le chevauchement
            start += (chunkSize - overlap);

            // Sécurité pour éviter la boucle infinie si overlap >= chunkSize
            if (start >= text.length() || chunkSize <= overlap) break;
        }
        return chunks;
    }
}
