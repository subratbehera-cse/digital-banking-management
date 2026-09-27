# SecureBank — Digital Banking Management System

A full-stack banking application built as a Java Full Stack Developer portfolio project.

- **Backend:** Java 21, Spring Boot 3, Spring Security + JWT, Spring Data JPA / Hibernate, MySQL, Maven
- **Frontend:** React 18, React Router 6, Axios, plain CSS (no UI framework) — responsive, banking-style UI

---

## 1. Features

- User registration & secure login with JWT (BCrypt password hashing)
- Role-based access control: `ROLE_CUSTOMER` and `ROLE_ADMIN`
- Customer profile management
- Bank account creation & management (Savings / Current)
- Balance tracking and full transaction history, paginated
- Deposit, withdraw, and transfer money between accounts
- Beneficiary management for faster repeat transfers
- Transaction validation: insufficient balance, inactive account, self-transfer, etc.
- Unique, traceable transaction reference IDs on every transaction (including failed attempts)
- Admin dashboard with bank-wide statistics
- Admin customer management (search, activate/deactivate)
- Admin account management (search, activate/deactivate/close)
- Admin transaction monitoring with type/status filters
- Search & pagination everywhere data grows unbounded
- Centralized/global exception handling with meaningful HTTP status codes
- Bean Validation (`jakarta.validation`) on every request DTO
- `@Transactional` database transactions around every money-movement operation, with pessimistic row locking to prevent race conditions on concurrent transfers
- Audit fields (`createdAt` / `updatedAt`) on all major entities
- Structured logging (SLF4J) across services and security filters

---

## 2. Project Structure

```
digital-banking-system/
├── backend/                     # Spring Boot REST API
│   ├── pom.xml
│   └── src/main/java/com/bankapp/
│       ├── config/              # Security, CORS, data seeding
│       ├── controller/          # REST controllers
│       ├── dto/request/         # Request DTOs (validated)
│       ├── dto/response/        # Response DTOs
│       ├── entity/              # JPA entities + enums
│       ├── repository/          # Spring Data JPA repositories
│       ├── service/             # Service interfaces
│       ├── service/impl/        # Service implementations
│       ├── security/            # JWT filter, util, UserDetails
│       ├── exception/           # Custom exceptions + global handler
│       ├── mapper/               # Entity <-> DTO mappers
│       └── util/                # Account number / transaction ref generators
├── database/
│   ├── schema.sql               # Reference DDL (Hibernate also auto-manages this)
│   └── sample-data.sql          # Optional demo data
├── frontend/                    # React SPA
│   └── src/
│       ├── api/                 # Axios instance + API service functions
│       ├── components/          # Reusable UI components
│       ├── context/             # AuthContext (global auth state)
│       ├── pages/                # One file per page (see below)
│       ├── pages/admin/         # Admin-only pages
│       ├── routes/              # ProtectedRoute guard
│       └── styles/global.css    # Design system / responsive styles
└── API_DOCUMENTATION.md
```

---

## 3. Prerequisites

- JDK 21+
- Maven 3.9+
- MySQL 8+
- Node.js 18+ and npm 9+

---

## 4. Running the Backend

1. Create the database (or let Hibernate auto-create it — `createDatabaseIfNotExist=true` is already set):

   ```sql
   CREATE DATABASE banking_db;
   ```

   Optionally run `database/schema.sql` yourself if you prefer explicit DDL.

2. Configure credentials, either by editing `backend/src/main/resources/application.yml`
   or exporting environment variables (recommended):

   ```bash
   export DB_USERNAME=root
   export DB_PASSWORD=your_password
   export JWT_SECRET=$(openssl rand -hex 32)
   export CORS_ORIGINS=http://localhost:3000
   ```

3. Build and run:

   ```bash
   cd backend
   mvn clean install
   mvn spring-boot:run
   ```

   The API starts on **http://localhost:8080**.

   On first boot, `DataInitializer` seeds the `ROLE_CUSTOMER` / `ROLE_ADMIN` roles and a
   default admin account:

   ```
   username: admin
   password: Admin@123
   ```

   **Change or remove this account before deploying anywhere near production.**

4. Run the test suite (unit + integration, using an in-memory H2 database):

   ```bash
   mvn test
   ```

---

## 5. Running the Frontend

```bash
cd frontend
npm install
cp .env.example .env      # adjust REACT_APP_API_BASE_URL if needed
npm start
```

The app runs on **http://localhost:3000** and talks to the backend at the URL configured
in `.env` (defaults to `http://localhost:8080/api`).

---

## 6. Default / Sample Logins

| Role     | Username     | Password    | Notes                                  |
|----------|--------------|-------------|-----------------------------------------|
| Admin    | `admin`      | `Admin@123` | Seeded automatically on first startup   |
| Customer | *(register)* | *(your choice)* | Use the **Register** page, or POST to `/api/auth/register` |

---

## 7. Security Notes

- Passwords are hashed with **BCrypt** (strength 12) — plaintext passwords are never stored or logged.
- JWTs are signed with HMAC-SHA256; the secret is externalized via `app.jwt.secret`.
- All `/api/**` endpoints require a valid JWT except `/api/auth/**`; `/api/admin/**` additionally requires `ROLE_ADMIN`, enforced both at the security-filter level and with `@PreAuthorize` on the controller.
- Customers can only access **their own** accounts, transactions and beneficiaries — every service method checks resource ownership and throws a `403 Forbidden` (`UnauthorizedAccessException`) otherwise.
- Money operations run inside a single `@Transactional` boundary with pessimistic row locks (`SELECT ... FOR UPDATE`) on the account rows involved, so a balance update and its transaction record commit or roll back together, and concurrent transfers on the same account cannot corrupt the balance.

---

## 8. Tech Decisions (kept intentionally simple)

- Manual DTO/mapper classes instead of MapStruct — fewer moving parts, easier to read for a portfolio project.
- Plain CSS instead of a UI framework — full control over the banking look-and-feel without extra dependencies.
- `ddl-auto: update` for convenience in dev; `database/schema.sql` is provided for teams that prefer managing DDL by hand or via a migration tool (Flyway/Liquibase) in a real production setup.

---

## 9. API Documentation

See [`API_DOCUMENTATION.md`](./API_DOCUMENTATION.md) for the full list of endpoints, request/response shapes, and status codes.
