# 🚀 Spring Boot API Starter Kit (Production Ready)

A solid, enterprise-grade foundation for building REST APIs with **Spring Boot 3** and **Java 21**.

This project removes the repetitive setup of new back-end services. It goes beyond "Hello World" by including **JWT authentication**, **Docker Multi-stage builds**, **secure secret management**, **soft deletes**, and a **clean architecture**.

---

## ⚡ Key Technical Features

### 🛡️ Security (Security-First)
- **JWT Stateless**: Robust signing with **Base64** decoded keys for high entropy.
- **Strict CORS**: "Deny by Default" policy. Origins must be explicitly allowed via environment variables.
- **Production Hardening**: Swagger/OpenAPI automatically disabled in the `prod` profile.

### 🏗️ Architecture & Data
- **Database Migrations (Flyway)**: SQL schema versioning. No `ddl-auto` in production.
- **Soft Delete**: Data is never physically deleted (`active = false`), ensuring auditability and referential integrity.
- **Native Pagination**: Optimized endpoints with `Pageable`.
- **Immutable DTOs**: Use of Java Records (Java 21) for secure data transfer.

### 🐳 DevOps & Infra
- **Docker Multi-stage Build**: Final lightweight images (Alpine Linux) without JDK, just JRE.
- **Health Checks**: Resilient orchestration via Docker Compose (The API waits for the database to be healthy).
- **12-Factor App**: Sensitive configurations externalized via environment variables.

---

## 🛠️ Tech Stack

- **Core**: Java 21, Spring Boot 3.3
- **Data**: PostgreSQL 16 (Prod), H2 (Dev), **Flyway**, Spring Data JPA
- **Security**: Spring Security, JJWT 0.12
- **Tooling**: Lombok, Docker, Maven Wrapper
- **Testing**: JUnit 5, Mockito

---

## 🚀 How to Run

### Prerequisites
- **Dev Mode**: Java 21 SDK
- **Prod Mode**: Docker & Docker Compose

### 1. 💻 Dev Mode (Local Development)

Uses in-memory database (H2). Ideal for rapid iteration. The database is recreated on each restart.

```bash
git clone https://github.com/GusGaiotti/api-starter-kit.git
cd api-starter-kit
./mvnw spring-boot:run
```

* Swagger UI: http://localhost:8080/swagger-ui/index.html

* H2 Console: http://localhost:8080/h2-console

    * URL: jdbc:h2:mem:testdb

    * User: sa / Pass: (empty)

### 2. 🐳 Production Mode (Docker — Real-World Simulation)
Simulates a real environment. The database is persisted in volumes and managed by Flyway.

#### Step 1: Create secrets file
Create a `.env` file in the project root (already ignored by Git):

```
# .env
PROD_DB_PASSWORD=your_secure_password_here
PROD_JWT_SECRET=generate_a_base64_key_here_see_below_how
CORS_ORIGINS=http://localhost:3000,https://mysite.com
```

Note: The `PROD_JWT_SECRET` must be a valid Base64 string (min 32 chars).

#### Step 2: Start the containers

```bash
docker compose up --build
```

The API will be available at http://localhost:8080.

Note: Swagger is intentionally disabled in the production profile.

#### Stop / Reset:

```bash
docker compose down          # Stop containers
docker compose down -v       # Stop + delete database volume (full reset)
```

## 📦 Database Management (Flyway)
We do not use `hibernate.ddl-auto = update` to avoid data corruption in production.
All database changes must be made via versioned SQL.

1. Create a file in `src/main/resources/db/migration`

2. Name it following the pattern: `V{version}__{description}.sql`

    * Ex: `V1__create_users_table.sql`

    * Ex: `V2__add_phone_column.sql`

3. On application restart, Flyway will automatically apply pending changes.

## ⚙️ Environment Variables
Priority configuration for Docker/Prod:

| Docker Var / .env | Spring Property | Description | Default Behavior |
|-------------------|-----------------|-------------|------------------|
| `PROD_JWT_SECRET` | `app.jwt.secret` | Base64 Signing Key | Fails if not defined (Security) |
| `PROD_DB_PASSWORD` | `spring.datasource.password` | PostgreSQL Password | Required in Prod |
| `CORS_ORIGINS` | `app.cors.allowed-origins` | Allowed Domains (csv) | Blocks Everything (if empty) |

Tip: To generate a secure JWT key in Base64:
```bash
openssl rand -base64 64
```

## 🧪 Tests
The project has a suite of unit tests covering business rules and security.

```bash
./mvnw test
```

Critical Scenarios Covered:

* ✅ Password encryption on registration.

* ✅ Soft Delete: Checks if status changes instead of deleting the record.

* ✅ Authorization: Ensures a user cannot modify/delete another user's account.

* ✅ Unique email validation.

## 📄 License
Licensed under the MIT License.