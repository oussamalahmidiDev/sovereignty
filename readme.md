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

### 3. Frontend

Run Angular frontend locally:
```bash
npm install
npm start
```

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