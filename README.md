# Distributed Job Processing
Distributed, event-driven job processing platform built with Spring Boot, PostgreSQL, Kafka, and Docker/Kubernetes support.

## Overview
This repository contains a multi-module Java application that accepts job submissions through a REST API, persists job state in PostgreSQL, publishes job events to Kafka, and processes jobs asynchronously in a worker service.

The system is organized into three Maven modules:
- `api-service`: REST API for submitting, querying, listing, and deleting jobs.
- `worker-service`: Kafka consumer that picks up job events and processes them asynchronously.
- `common-library`: Shared domain models, events, exceptions, and repository interfaces used by both services.

Main features:
- Submit jobs through a versioned REST API.
- Track job lifecycle state in PostgreSQL.
- Publish job-created events to Kafka after the database transaction commits.
- Consume and process jobs asynchronously in a worker service.
- Filter and page through jobs by status or job type.
- Delete only terminal jobs (`COMPLETED`, `FAILED`, or `DLQ`).
- Containerized local development with Docker Compose.
- Kubernetes manifests for cluster deployment.

## Architecture
The diagram below shows how the API service, Kafka, PostgreSQL, and worker service fit together.

![Distributed Job Processing Architecture](./docs/Architecture%20Diagram.png)

Tech stack:
- Java 17
- Spring Boot 4.1.0
- Spring Web MVC
- Spring Data JPA
- Flyway
- Spring Kafka
- PostgreSQL 17
- Maven multi-module build
- Docker and Kubernetes

## Prerequisites
- Java 17 JDK
- Apache Maven 3.9+ or the Maven Wrapper included in the repo
- Docker and Docker Compose for local containerized setup
- PostgreSQL 17 if running services outside Docker
- Kafka 3.x-compatible broker if not using Docker Compose

System requirements:
- At least 4 GB of available RAM for running PostgreSQL, Kafka, and both Spring Boot services locally
- Open ports:
  - `8080` for the API service
  - `5432` for PostgreSQL inside the container network, or `5433` if using the provided Docker Compose mapping
  - `9092` for Kafka
  - `8090` for Kafka UI

## Installation
1. Clone the repository:

   ```bash
   git clone <repo-url>
   cd distributed-job-processing
   ```

2. Build the full multi-module project:

   ```bash
   ./mvnw clean install
   ```

   On Windows PowerShell:

   ```powershell
   .\mvnw.cmd clean install
   ```

3. If you want to run the services against local infrastructure, start PostgreSQL and Kafka first.

   Recommended local stack:

   ```bash
   docker compose -f docker/docker-compose.yml up -d
   ```

4. Configuration notes:
- `api-service/src/main/resources/application.properties`
- `worker-service/src/main/resources/application.properties`

  Key environment variables:
  - `SPRING_DATASOURCE_URL` - PostgreSQL JDBC URL
  - `SPRING_DATASOURCE_USERNAME` - Database username
  - `SPRING_DATASOURCE_PASSWORD` - Database password
  - `KAFKA_BOOTSTRAP_SERVERS` - Kafka bootstrap address
  - `KAFKA_DEFAULT_TOPIC` - Topic used by the API producer
  - `KAFKA_CONSUMER_GROUP` - Worker consumer group

  Default values are configured for Docker-based local runs.

## Running the Application
### Run with Docker Compose
Bring up the full local stack:

```bash
docker compose -f docker/docker-compose.yml up --build
```

Services exposed by the compose file:
- API service: `http://localhost:8080`
- Kafka UI: `http://localhost:8090`
- PostgreSQL host mapping: `localhost:5433`

### Run locally with Maven
Start the infrastructure first, then run each service in a separate terminal:

```bash
./mvnw -pl api-service spring-boot:run
./mvnw -pl worker-service spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd -pl api-service spring-boot:run
.\mvnw.cmd -pl worker-service spring-boot:run
```

### Docker images
The repository includes service-specific Dockerfiles:
- [`api-service/Dockerfile`](./api-service/Dockerfile)
- [`worker-service/Dockerfile`](./worker-service/Dockerfile)

### Kubernetes
The `k8s/` directory contains manifests and a `kustomization.yaml` for deploying the stack to Kubernetes, including:
- ConfigMap and Secret resources
- PostgreSQL stateful set and service
- Kafka deployment and service
- API and worker deployments
- API ingress

Apply the stack with:

```bash
kubectl apply -k k8s/
```

## Usage
### Submit a job
`POST /api/v1/jobs`

Example request:

```bash
curl -X POST http://localhost:8080/api/v1/jobs \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: req-123" \
  -d '{
    "jobType": "REPORT",
    "payload": {
      "reportId": 101
    },
    "clientReqId": "req-123",
    "tenantId": "tenant-1"
  }'
```

The API returns `202 Accepted` with a job ID and a self link.

### Get job status
`GET /api/v1/jobs/{id}`

Example:

```bash
curl http://localhost:8080/api/v1/jobs/<job-id>
```

### List jobs
`GET /api/v1/jobs?page=0&size=20&status=PENDING&jobType=REPORT`

Supported query parameters:
- `page`
- `size`
- `status`
- `jobType`

### Delete a job
`DELETE /api/v1/jobs/{id}`

Deletion is allowed only when the job is in a terminal state:
- `COMPLETED`
- `FAILED`
- `DLQ`

### Supported job processing
The worker currently includes a `REPORT` processor. Additional job types can be added by implementing the `JobProcessor` interface and registering the processor as a Spring bean.

### Sample flow
1. Submit a job to the API.
2. The API stores the job in PostgreSQL with `PENDING` status.
3. After the transaction commits, the API publishes a `JobCreatedEvent` to Kafka.
4. The worker consumes the event from the `job.requests` topic.
5. The worker loads the job record, marks it `PROCESSING`, runs the appropriate processor, and marks it `COMPLETED`.

## Resources
- Design document: [`docs/Design Document.pdf`](./docs/Design%20Document.pdf)
- Kubernetes manifests: [`k8s/`](./k8s/)
- Docker Compose setup: [`docker/docker-compose.yml`](./docker/docker-compose.yml)
- Kafka-only Compose setup: [`docker/docker-compose-kafka.yml`](./docker/docker-compose-kafka.yml)
- API module: [`api-service/`](./api-service/)
- Worker module: [`worker-service/`](./worker-service/)
- Shared module: [`common-library/`](./common-library/)

## Contributing
1. Create a feature branch.
2. Keep changes focused to one concern where possible.
3. Run tests before opening a pull request:

   ```bash
   ./mvnw test
   ```

4. If you change service behavior, update or add tests in the relevant module.
5. Prefer clear package boundaries:
   - API contracts in `api-service`
   - Shared domain types in `common-library`
   - Async processing logic in `worker-service`

Suggested workflow:
- Branch naming: `codex/<short-description>`
- Use Maven formatting and standard Spring Boot conventions
- Keep Kafka topic names and database schema changes documented

## License
No license file is currently present in the repository. Add a license file if you want to make the project's usage terms explicit.
