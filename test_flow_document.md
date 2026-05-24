# Sovereignty AI: Operational Guidelines and Architecture

Welcome to the Sovereignty AI testing document. This markdown file contains structured text, architectural notes, and operational procedures to validate the ingestion, splitting, and semantic search flows of the RAG (Retrieval-Augmented Generation) engine.

---

## 1. Core System Architecture

Sovereignty AI is designed around a strictly decoupled Hexagonal Architecture (Ports and Adapters). This ensures that core business logic remains independent of databases, web frameworks, message brokers, and AI orchestration engines.

### 1.1 Inbound Adapters (Primary Adapters)
Primary adapters initiate action on the application core. 
*   **Web Controllers:** Expose REST endpoints for document uploads, chat management, and health checks.
*   **Kafka Document Worker:** Listens to incoming document events (e.g., file uploaded notifications) and triggers downstream indexing.
*   **Schedulers:** Periodically poll outbox tables to publish events reliably.

### 1.2 Outbound Adapters (Secondary Adapters)
Secondary adapters are invoked by the application core to communicate with the outside world.
*   **Persistence Adapter:** Implements repository ports using Spring Data JPA and Postgres.
*   **Vector Store Adapter:** Integrates with PGVector using Spring AI to store high-dimensional chunk embeddings.
*   **Ollama AI Adapter:** Calls local LLM endpoints to perform embeddings and complete chat requests.

---

## 2. High-Performance Concurrency System

To optimize indexing speed while respecting hardware limitations of local AI services (like Ollama running on consumer CPUs or entry-level GPUs), the system implements Project Loom Virtual Threads throttled by a rate-limiting semaphore.

### 2.1 Virtual Threads (Project Loom)
By replacing platform-thread pools with a virtual thread executor (`Executors.newVirtualThreadPerTaskExecutor()`), the backend can spin up thousands of concurrent tasks with minimal memory overhead. This allows massive parallel processing of text chunks.

### 2.2 Ollama Concurrency Throttle
Ollama can easily choke if bombarded with hundreds of simultaneous vectorization HTTP calls.
*   We use a **Semaphore with 4 permits** to throttle parallel calls to the embedding model.
*   This ensures that no more than 4 virtual threads can hit the embedding service at the same time, preventing timeouts and socket exhaustion.

---

## 3. Transactional Consistency & Resiliency

To prevent inconsistencies when interacting with multiple persistent systems (like Postgres databases and Kafka brokers), we employ advanced transactional patterns.

### 3.1 Transactional Outbox Pattern
Instead of immediately publishing to a message broker inside an active database transaction:
1.  We write a record to the `outbox_events` table within the same transaction that persists the document entity.
2.  Once the transaction commits, the outbox record is guaranteed to be saved.
3.  A background thread polls the outbox table, publishes the message to Kafka, and transitions the state to `PROCESSED`.

### 3.2 Error Handling & DLT
*   **Transient Errors:** Failures like database timeouts or Ollama connection timeouts trigger up to 3 retries with exponential backoff.
*   **Fatal Errors:** Unsupported formats or malformed documents bypass retries to save CPU cycles.
*   **Dead Letter Topics:** Unrecoverable messages end up in the DLT, marking the aggregate as `FAILED`.

---

## 4. Sample Test Questions

You can copy and paste the following questions into the Chat interface to test the RAG retrieval accuracy:
1.  *What pattern does the system use to prevent dual-writes to the DB and Kafka?*
2.  *How many concurrent embedding calls are allowed to hit Ollama simultaneously?*
3.  *What mechanism does the system use to avoid carrier thread pinning in virtual threads?*
4.  *Under which package do the ListUtils and ExceptionUtils utilities reside?*
