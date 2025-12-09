# 🚀 Spring Boot API Starter Kit (Production Ready)

A solid, enterprise-grade foundation for building REST APIs with **Spring Boot 3** and **Java 21**.

This project removes the repetitive setup of new back-end services. It goes beyond "Hello World" by including **JWT authentication**, **Docker Multi-stage builds**, **secure secret management**, **soft deletes**, and a **clean architecture**.

---

## ⚡ Main Features

- **Security & Auth**
    - Stateless JWT authentication (Spring Security + jjwt)
    - BCrypt password hashing
    - **Secure Secrets Management** via `.env` files (Git ignored)
    - CORS configured for production environments

- **Data Handling**
    - **Soft Delete**: Entities use an `active` flag (data is never physically deleted)
    - **Pagination**: Native support via Spring Data `Pageable`
    - **Hybrid DB**: H2 (In-Memory) for Dev / PostgreSQL 16 for Production

- **DevOps & Infra**
    - **Docker Multi-stage Build**: optimized, small-footprint images (Eclipse Temurin Alpine)
    - **Docker Compose**: Orchestrates the API and PostgreSQL with persistent volumes
    - **Health Checks**: Database dependency management — the API waits for DB to be ready

- **Observability**
    - Swagger UI (OpenAPI 3)
    - Spring Actuator (Health & Metrics)

---

## 🛠️ Tech Stack

- **Core**: Java 21, Spring Boot 3.3
- **Data**: Spring Data JPA (Hibernate), PostgreSQL 16, H2
- **Security**: Spring Security, JJWT 0.12
- **Tooling**: Lombok, Docker, Maven Wrapper
- **Testing**: JUnit 5, Mockito

---

## 🚀 How to Run

### Prerequisites
- **Dev Mode**: Java 21 SDK
- **Production Mode**: Docker Desktop (no local Java required)

### 1. 💻 Dev Mode (Local Development)

Uses in-memory H2 database. Ideal for fast iteration.

```bash
git clone https://github.com/GusGaiotti/api-starter-kit.git
cd api-starter-kit
./mvnw spring-boot:run
```

Once running:

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- H2 Console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:testdb`)

### 2. 🐳 Production Mode (Docker — Real World Simulation)

Runs the compiled JAR + real PostgreSQL in containers (exactly like cloud deployment).

#### Step 1: Create secrets file
Create a `.env` file in the project root (same level as `pom.xml`).  
This file is **ignored by Git** for security.

```env
# .env
PROD_DB_PASSWORD=change_this_to_a_secure_password
PROD_JWT_SECRET=change_this_to_a_very_long_random_string_min_32_chars
```

#### Step 2: Start everything

```bash
docker compose up --build
```

App will be available at: http://localhost:8080

Data is persisted in a Docker volume.

**Stop / Reset**:

```bash
docker compose down          # Stop containers
docker compose down -v       # Stop + delete database volume (full reset)
```

---
## 💾 Database Connection (PostgreSQL)

To inspect the database running inside the Docker container using external tools (DBeaver, IntelliJ Database Tool, etc.), use the following credentials:

| Setting | Value |
| :--- | :--- |
| **Host** | `localhost` |
| **Port** | `5432` |
| **Database** | `apidb` |
| **User** | `postgres` |
| **Password** | *Value from `PROD_DB_PASSWORD` in your `.env` file* |

Alternatively, you can access the SQL shell directly via the Docker CLI:

```bash
docker exec -it api-postgres psql -U postgres -d apidb
```
---

## 🔐 Authentication Flow

All endpoints except `/api/v1/auth/**` and docs are protected.

1. **Register**
   ```
   POST /api/v1/auth/register
   ```
   ```json
   {
     "name": "Admin",
     "email": "admin@test.com",
     "password": "securePass123"
   }
   ```

2. **Login**
   ```
   POST /api/v1/auth/login
   ```
   ```json
   {
     "token": "eyJhbGciOiJIUzI1NiJ9.xY..."
   }
   ```

3. **Use in Swagger**
    - Click **Authorize** (top right)
    - Enter: `Bearer <your_jwt_token>`

---

## ⚙️ Configuration & Secrets

Configuration priority:  
`application.yml` (dev defaults) → `application-prod.yml` (prod overrides) → Environment variables (Docker)

| Docker Env Var /.env     | Spring Property               | Description                                      | Default (Behavior) |
|--------------------------|-------------------------------|--------------------------------------------------|--------------------|
| `PROD_JWT_SECRET`        | `app.jwt.secret`              | **Base64 Encoded** signing key (min 256-bit)     | *Required in Prod* |
| `PROD_DB_PASSWORD`       | `spring.datasource.password`  | PostgreSQL password                              | *Required in Prod* |
| `DB_URL` (auto-set)      | `spring.datasource.url`       | JDBC URL — automatically configured in Docker    | *Auto-configured* |
| `CORS_ORIGINS`           | `app.cors.allowed-origins`    | Allowed domains (e.g. `https://myapp.com`)       | **Blocks All** (if empty) |

> **Tip:** To generate a secure Base64 JWT secret, run this in your terminal:
> `openssl rand -base64 32`
---

## 🧪 Running Tests

Unit tests cover security, soft delete, password handling, etc.

```bash
./mvnw test
```

---

## 📄 License

Licensed under the **MIT License**.
