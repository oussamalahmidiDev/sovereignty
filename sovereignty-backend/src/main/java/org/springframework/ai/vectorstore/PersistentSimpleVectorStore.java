package org.springframework.ai.vectorstore;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PersistentSimpleVectorStore extends SimpleVectorStore {

    private final File file;

    public PersistentSimpleVectorStore(EmbeddingModel embeddingModel, File file) {
        super(SimpleVectorStore.builder(embeddingModel));
        this.file = file;

        // Load initial state if file exists
        if (file.exists()) {
            this.load(file);
        }
    }

    private void saveToFile() {
        try {
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            this.save(file);
        } catch (Exception e) {
            throw new RuntimeException("Failed to persist vector store to file: " + file.getAbsolutePath(), e);
        }
    }

    @Override
    public void doAdd(List<Document> documents) {
        super.doAdd(documents);
        saveToFile();
    }

    @Override
    public void doDelete(List<String> idList) {
        super.doDelete(idList);
        saveToFile();
    }

    public void cleanByDocumentId(UUID documentId) {
        String docIdStr = documentId.toString();

        List<String> idsToRemove = new ArrayList<>();
        Map<String, SimpleVectorStoreContent> storeMap = this.store;
        if (storeMap != null) {
            for (Map.Entry<String, SimpleVectorStoreContent> entry : storeMap.entrySet()) {
                SimpleVectorStoreContent content = entry.getValue();
                if (content != null && content.getMetadata() != null) {
                    Object val = content.getMetadata().get("documentId");
                    if (docIdStr.equals(val)) {
                        idsToRemove.add(entry.getKey());
                    }
                }
            }
        }

        if (!idsToRemove.isEmpty()) {
            this.delete(idsToRemove);
        }
    }
}
