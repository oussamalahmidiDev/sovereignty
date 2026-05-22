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

# Start Ollama
ollama serve
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

## Local Development

### 1. Infrastructure

The project uses **Spring Boot Docker Compose integration** for local development. 
When starting the backend locally from IntelliJ or commandLine, Spring Boot automatically starts the required infrastructure defined in: 
```text
compose.yaml
```
This includes:
* PostgreSQL + pgvector
* Kafka

### 2. ELK  & Monitoring (Optional)

The ELK stack is isolated in a separate compose file:
```text
compose-elk.yaml
```

To start observability services manually:
```bash
docker compose -f compose-elk.yaml up -d
```

This starts:
* Elasticsearch
* Logstash
* Kibana

Important: Make sure you run the app with this profile:
```text
elk
```

### 3. Frontend

Run Angular frontend locally:
```bash
npm install
npm start
```

## 🔌 API Endpoints

A detailed API documentation is available via Swagger UI when the backend is running. Below is a quick reference summary of the available endpoints:

| Endpoint | Description | Input Description |
| :--- | :--- | :--- |
| **OpenAPI / Documentation** | | |
| `GET /v3/api-docs` | Retrieve OpenAPI 3.0/3.1 JSON specification | None |
| `GET /swagger-ui.html` | Open interactive Swagger UI API playground | None |
| **Chat Session Management** | | |
| `GET /api/chat` | Retrieve all active chat conversations | None |
| `GET /api/chat/{chatId}/messages` | Retrieve full message history for a specific chat | `chatId` (Path parameter, UUID) |
| `DELETE /api/chat/{chatId}` | Delete a specific chat conversation and its messages | `chatId` (Path parameter, UUID) |
| `POST /api/chat/ask` | Submit a blocking query to the assistant and wait for the response | JSON body with `question` (string) and `chatId` (UUID) |
| `POST /api/chat/ask/stream` | Submit a streaming query to the assistant (Server-Sent Events) | JSON body with `question` (string) and `chatId` (UUID) |
| **Document Processing** | | |
| `POST /api/documents/upload` | Upload and index a text/markdown file | `file` (Multipart form-data) |
| `GET /api/documents` | List all uploaded documents and their ingestion status | None |
| `GET /api/documents/{documentId}/subscribe` | Subscribe to real-time status updates via SSE | `documentId` (Path parameter, UUID) |
| `GET /api/documents/{documentId}/download` | Download the raw content of an ingested document | `documentId` (Path parameter, UUID) |
| `DELETE /api/documents` | Remove a document, its metadata, and all vector embeddings | JSON body with `id` (UUID) and `fileName` (string) |
| **Monitoring & Administration** | | |
| `GET /api/health/status` | Check the health of Ollama, pgvector database, and Kafka | None |
| `POST /admin/jfr/start` | Start a Java Flight Recorder (JFR) profiling session | `duration` (Query parameter, seconds, default: 60) |
| `POST /admin/jfr/stop` | Stop the active JFR profiling session | None |
| `GET /admin/jfr/download` | Download the recorded JFR flight recording file | None |

## Run with Kubernetes & Helm

### 1. Overview

The application can also be deployed on Kubernetes using Helm, providing:
* Service discovery
* Ingress routing
* Config management (ConfigMap + Secret)
* Reproducible deployments

### 2. Prerequisites
Ensure you have the following installed:
* **Docker Desktop** with **Kubernetes** enabled
* **kubectl**
* **Helm**
* **Ollama installed locally** ([Download here](https://ollama.com/)) (Or you can plug any AI you want)

### 3. Install Ingress
```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.11.3/deploy/static/provider/cloud/deploy.yaml
```

### 4. Configure local domain
```bash
sudo nano /etc/hosts
```

### 5. Add this line
```
127.0.0.1 sovereignty.local
```

### 6. Build Docker images for frontend and backend
```bash
docker build -t sovereignty-ai-frontend:latest ./sovereignty-frontend
docker build -t sovereignty-ai-backend:latest ./sovereignty-backend
```

### 7. Install and deploy with Helm
```bash
helm install sovereignty ./helm
```

### 8. Access the application
```bash
http://sovereignty.local
```