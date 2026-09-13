# Product Management Service

A production-ready Product Management web application and RESTful API built with Java 17 and Spring Boot 3. The system implements clean architecture principles, CQRS pattern, JWT authentication, in-memory caching with Caffeine, asynchronous audit processing, comprehensive exception handling, and a clean Bootstrap frontend.

---

## Architecture & Technical Highlights

- **Framework**: Spring Boot 3.3.5 (Java 17 LTS)
- **Architecture Pattern**:
  - **CQRS (Command Query Responsibility Segregation)**: Separates state-mutating operations (`ProductCommandService`) from read queries (`ProductQueryService`).
  - **Repository Pattern**: Spring Data JPA abstraction layer with JPA Specifications for dynamic search and filtering.
- **Security**: Stateless Spring Security with JSON Web Tokens (JWT) and Role-Based Access Control (`ROLE_USER`, `ROLE_ADMIN`).
- **Caching**: In-memory caching with **Caffeine** (`@Cacheable`, `@CacheEvict`) for high-throughput query performance.
- **Asynchronous Processing**: Dedicated `ThreadPoolTaskExecutor` for non-blocking asynchronous audit logging (`@Async`).
- **Database**: PostgreSQL with runtime compatibility for H2 In-Memory database.
- **Documentation**: Interactive OpenAPI 3 / Swagger UI.
- **Frontend**: Clean and responsive web interface built with Thymeleaf and Bootstrap 5.
- **Testing**: Comprehensive unit and integration test suite using JUnit 5, Mockito, and MockMvc with JaCoCo code coverage > 80%.

---

## Quick Start (Run Locally in 5 Steps)

Follow these 5 simple steps to get the application up and running on your local machine (Windows, Linux, or macOS).

### Step 1: Clone Repository
```bash
git clone https://github.com/RyanSitorus/product-management.git
cd product-management
```

### Step 2: Database Setup
Create a PostgreSQL database named `product_db`:
```sql
CREATE DATABASE product_db;
```
> **Note**: If PostgreSQL is not installed, the application will seamlessly fall back to an embedded in-memory H2 database during tests and local evaluation.

### Step 3: Configure Database Credentials (Optional)
Check or adjust credentials in `src/main/resources/application.properties` if your PostgreSQL setup differs from the default:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/product_db
spring.datasource.username=postgres
spring.datasource.password=postgres
```

### Step 4: Build and Run Application
Run the application using the included Maven wrapper:

**On Windows (Command Prompt / PowerShell):**
```bash
.\mvnw.cmd spring-boot:run
```

**On Linux / macOS:**
```bash
chmod +x mvnw
./mvnw spring-boot:run
```

### Step 5: Access the Application
Once started, open your web browser:
- **Web Dashboard**: [http://localhost:8080](http://localhost:8080) (or `/login`)
- **Swagger OpenAPI UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **API Docs (JSON)**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## Default User Accounts

The database comes ready for instant testing. You can register a new account via the UI / API, or use the following default credentials:

| Role | Username | Password | Access Rights |
|---|---|---|---|
| **Administrator** | `admin` | `admin123` | Full access (Create, Read, Update, Delete) |
| **Standard User** | `user` | `user123` | Read, Search, and Create access |

---

## REST API Specification

All API endpoints reside under `/api/v1/`. Responses follow a standardized envelope structure:
```json
{
  "success": true,
  "status": 200,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2026-09-13T10:00:00.000+00:00"
}
```

### Authentication Endpoints

| Method | Endpoint | Auth Required | Description |
|---|---|:---:|---|
| `POST` | `/api/v1/auth/register` | No | Register a new user account |
| `POST` | `/api/v1/auth/login` | No | Authenticate and obtain JWT Bearer token |

### Product Management Endpoints

| Method | Endpoint | Auth Required | Description |
|---|---|:---:|---|
| `POST` | `/api/v1/products` | Yes (`Bearer`) | Create a new product |
| `GET` | `/api/v1/products` | Yes (`Bearer`) | Retrieve all products (cached) |
| `GET` | `/api/v1/products/{id}` | Yes (`Bearer`) | Retrieve single product by ID (cached) |
| `GET` | `/api/v1/products/search` | Yes (`Bearer`) | Search by name, price range with pagination & sort |
| `PUT` | `/api/v1/products/{id}` | Yes (`Bearer`) | Update existing product by ID |
| `DELETE` | `/api/v1/products/{id}` | Yes (`Bearer`) | Delete product by ID |

---

## Sample cURL Requests

### 1. Register User
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "password123",
    "role": "ROLE_USER"
  }'
```

### 2. Login & Obtain JWT Token
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123"
  }'
```
*Save the `token` value from the JSON response to use in the `Authorization: Bearer <TOKEN>` header for subsequent requests.*

### 3. Create Product
```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -d '{
    "name": "Wireless Mechanical Keyboard",
    "description": "RGB backlit mechanical keyboard with hot-swappable switches",
    "price": 89.99
  }'
```

### 4. Search and Filter Products
```bash
curl -X GET "http://localhost:8080/api/v1/products/search?name=Keyboard&minPrice=50&maxPrice=150&page=0&size=10&sortBy=price&sortDir=asc" \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

### 5. Update Product
```bash
curl -X PUT http://localhost:8080/api/v1/products/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -d '{
    "name": "Wireless Mechanical Keyboard V2",
    "description": "Upgraded battery and switches",
    "price": 99.99
  }'
```

### 6. Delete Product
```bash
curl -X DELETE http://localhost:8080/api/v1/products/1 \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

---

## Testing & Code Coverage

The project includes an extensive test suite covering controllers, services, security filters, validation, and error scenarios.

Run all tests via Maven:
```bash
.\mvnw.cmd test         # Windows
./mvnw test             # Linux / macOS
```

### JaCoCo Code Coverage Report
Code coverage reports are automatically generated during the test phase:
```bash
.\mvnw.cmd test jacoco:report
```
To inspect the visual HTML report, open:
```
target/site/jacoco/index.html
```
- **Total Instruction Coverage**: > 80%
- **Total Line Coverage**: > 80%

---

## Configuration Reference

Key application parameters configured in `src/main/resources/application.properties`:

| Property | Default Value | Description |
|---|---|---|
| `server.port` | `8080` | HTTP port |
| `jwt.secret` | `9a6727...` | Secret key for HS512 JWT signing |
| `jwt.expiration-ms` | `86400000` | Token expiration time (24 hours) |
| `app.cache.caffeine.ttl-minutes` | `10` | Cache time-to-live |
| `app.cache.caffeine.max-size` | `500` | Maximum number of cached items |
| `logging.level.com.assessment.product` | `INFO` | Application log level |
