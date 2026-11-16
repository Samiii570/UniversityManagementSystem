# University Management System (CSE 3230 – Lab 4)

Spring Boot 3 + Spring Security + PostgreSQL + Docker Compose.
Three roles: **ADMIN**, **TEACHER**, **STUDENT**.

## Project structure
```
src/main/java/com/kuet/ums
├── entity/       BaseUser (common fields), Admin, Teacher, Student, Role
├── repository/   JPA repositories
├── dto/          *Request (input + validation), *Response (output, no password)
├── service/      business logic, BCrypt hashing, duplicate checks
├── controller/   REST controllers (+ AuthController /api/auth/me)
├── security/     SecurityConfig (RBAC, login/logout), CustomUserDetailsService, DataSeeder
└── exception/    GlobalExceptionHandler -> consistent JSON errors
```

## Role-based access
| Endpoint            | ADMIN | TEACHER | STUDENT |
|---------------------|:-----:|:-------:|:-------:|
| `/api/admins/**`    |  ✅   |   ❌    |   ❌    |
| `/api/teachers/**`  |  ✅   |   ✅    |   ❌    |
| `/api/students/**`  |  ✅   |   ❌    |   ✅    |

## REST endpoints (same for admins / teachers / students)
| Method | URL                    | Success           | Errors           |
|--------|------------------------|-------------------|------------------|
| GET    | `/api/{resource}`      | 200 OK            | 401, 403         |
| GET    | `/api/{resource}/{id}` | 200 OK            | 404              |
| POST   | `/api/{resource}`      | 201 Created + `Location` | 400, 409  |
| PUT    | `/api/{resource}/{id}` | 200 OK            | 400, 404, 409    |
| DELETE | `/api/{resource}/{id}` | 204 No Content    | 404              |

## Authentication
- **Login:** `POST /api/auth/login` (form fields `username`, `password`) → JSON + `JSESSIONID` cookie
- **Logout:** `POST /api/auth/logout`
- **Current user:** `GET /api/auth/me`
- HTTP Basic auth also works on every request (handy in Postman).
- Passwords are hashed with **BCrypt**; responses never include the password.
- A default admin (`admin` / `admin123`, configurable in `.env`) is created on first start.

## Run with Docker
```bash
docker compose up --build
```
App → http://localhost:8081 , Postgres → localhost:5432

## Run locally from IntelliJ (no install needed)
Just run `UmsApplication`. Default profile is **dev** → in-memory H2 database
(data is lost when the app stops).

## Run locally against Postgres
```bash
docker compose up -d postgres
# set env SPRING_PROFILES_ACTIVE=postgres, then run UmsApplication
```

## Tests
`./mvnw test` — integration tests on in-memory H2 (CRUD, status codes, RBAC, login/logout).

Sample requests: `api-requests.http`.
