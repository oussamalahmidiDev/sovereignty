# Sovereignty AI - Hexagonal RAG System

Sovereignty AI is a **Retrieval-Augmented Generation (RAG)** application designed for privacy and control. It allows users to index local documents and interact with them using a sovereign AI stack (Ollama). The project is built using **Hexagonal Architecture** (Ports & Adapters) to ensure a strict separation between business logic and infrastructure.

## 🚀 Quick Start

### 1. Prerequisites
Ensure you have the following installed:
* **Java 21**
* **Node.js 20+** & **Angular CLI**
* **Docker** & **Docker Compose**
* **Ollama** ([Download here](https://ollama.com/)) (Or you can plug any AI you want)

### 2. AI Model Setup (Ollama)
To avoid the PGVector 2000-dimension limit for HNSW indexes, we use a dedicated embedding model:
```bash
# Model for chat/generation
ollama pull llama3

# Model for embeddings (768 dimensions - required for PGVector HNSW compatibility)
ollama pull nomic-embed-text
```

### 3. Kafka Setup
By default, the application expects a topic named document-topic. You can create it manually using the following command:
```bash
docker exec -it sovereignty-kafka kafka-topics --create \
  --topic test-topic \
  --bootstrap-server localhost:9092 \
  --partitions 1 \
  --replication-factor 1
```