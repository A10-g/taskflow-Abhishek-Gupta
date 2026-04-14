# TaskFlow Backend

## 1. Overview

This repository contains my backend-only submission for the TaskFlow take-home assignment.

TaskFlow is a minimal project and task management API where users can:
- register and log in
- create projects
- view projects they own or are assigned work in
- create, filter, update, and delete tasks
- view project-level task statistics

### Tech stack

- Java 21
- Spring Boot 3
- Spring Security with JWT
- Spring Data JPA
- PostgreSQL 16
- Liquibase for migrations and rollback definitions
- Docker Compose for local setup
- Maven for build and test execution

## 2. Architecture Decisions

### Why Spring Boot instead of Go

The assignment prefers Go, but explicitly allows a language the candidate is stronger in. I chose Spring Boot because I could deliver a more complete, reviewable, and correct backend within the timebox.

### Structure

The backend is organized into focused modules:
- `auth`: register, login, JWT handling, current-user resolution
- `project`: project CRUD and stats endpoint
- `task`: task CRUD, filtering, and authorization
- `user`: user persistence
- `common`: exception handling and pagination helpers
- `config`: security configuration

The code follows a simple controller -> service -> repository flow:
- controllers handle HTTP concerns
- services hold business rules and permission checks
- repositories handle persistence queries

### Data modeling choices

The required entities are implemented with PostgreSQL as the source of truth. I added `created_by` to `tasks` because the assignment requires task deletion to be allowed for the project owner or task creator only.

For task `status` and `priority`, I used Java enums with JPA attribute converters. This keeps the Java model explicit while preserving the lowercase database values required by the API contract.

### Migrations choice

I used Liquibase instead of Flyway because the assignment explicitly asks for up and down migration support. Liquibase formatted SQL lets me keep migrations readable while still defining rollback behavior.

### Tradeoffs / intentional omissions

I kept the implementation focused on the assignment scope and did not add:
- refresh tokens
- role-based access control beyond the required ownership rules
- Swagger/OpenAPI generation
- frontend client
- advanced observability or production deployment infrastructure

The current tests are focused and useful, but they are not yet full Testcontainers-backed database integration tests.

## 3. Running Locally

### Prerequisites

- Docker Desktop (or Docker Engine with Compose support)

### Commands

```bash
git clone https://github.com/A10-g/taskflow-Abhishek-Gupta.git
cd taskflow-Abhishek-Gupta
cp .env.example .env
docker compose up --build
```

### Services

- API: [http://localhost:8080](http://localhost:8080)
- Health check: [http://localhost:8080/api/health](http://localhost:8080/api/health)
- PostgreSQL: `localhost:5432`

### Notes

- Liquibase runs automatically when the backend container starts.
- Seed data is applied automatically through the migration chain.
- The submission is backend-only, so there is no frontend container.

## 4. Running Migrations

Migrations run automatically on application startup via Liquibase.

Migration files live in:
- [db.changelog-master.yaml](./backend/src/main/resources/db/changelog/db.changelog-master.yaml)
- [V1__create_users.sql](./backend/src/main/resources/db/changelog/V1__create_users.sql)
- [V2__create_projects.sql](./backend/src/main/resources/db/changelog/V2__create_projects.sql)
- [V3__create_tasks.sql](./backend/src/main/resources/db/changelog/V3__create_tasks.sql)
- [V4__seed_data.sql](./backend/src/main/resources/db/changelog/V4__seed_data.sql)

Rollback definitions are included in the Liquibase SQL files.

## 5. Test Credentials

Seed user:

```text
Email:    test@example.com
Password: password123
```

## 6. API Reference

A Postman collection is also included: [taskflow-backend.postman_collection.json](./postman/taskflow-backend.postman_collection.json)

All protected endpoints require `Authorization: Bearer <token>`.

---

### Authentication

#### `POST /auth/register`

```json
// Request
{
  "name": "Test User",
  "email": "test@example.com",
  "password": "password123"
}

// Response 201
{
  "token": "<jwt>",
  "user": {
    "id": "11111111-1111-1111-1111-111111111111",
    "name": "Test User",
    "email": "test@example.com"
  }
}
```

#### `POST /auth/login`

```json
// Request
{
  "email": "test@example.com",
  "password": "password123"
}

// Response 200
{
  "token": "<jwt>",
  "user": {
    "id": "11111111-1111-1111-1111-111111111111",
    "name": "Test User",
    "email": "test@example.com"
  }
}
```

---

### Projects

#### `GET /projects?page=1&limit=20`

```json
// Response 200
{
  "projects": [
    {
      "id": "22222222-2222-2222-2222-222222222222",
      "name": "Greening India Launch",
      "description": "Initial seeded project for reviewer verification.",
      "ownerId": "11111111-1111-1111-1111-111111111111",
      "createdAt": "2026-04-14T10:00:00Z"
    }
  ],
  "page": 1,
  "limit": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

#### `POST /projects`

```json
// Request
{
  "name": "New Project",
  "description": "Optional description"
}

// Response 201
{
  "id": "22222222-2222-2222-2222-222222222222",
  "name": "New Project",
  "description": "Optional description",
  "ownerId": "11111111-1111-1111-1111-111111111111",
  "createdAt": "2026-04-14T10:00:00Z"
}
```

#### `GET /projects/{id}`

```json
// Response 200
{
  "id": "22222222-2222-2222-2222-222222222222",
  "name": "Greening India Launch",
  "description": "Initial seeded project for reviewer verification.",
  "ownerId": "11111111-1111-1111-1111-111111111111",
  "createdAt": "2026-04-14T10:00:00Z",
  "tasks": [
    {
      "id": "33333333-3333-3333-3333-333333333331",
      "title": "Draft API contract",
      "description": "Prepare the first version of the TaskFlow backend API contract.",
      "status": "todo",
      "priority": "high",
      "projectId": "22222222-2222-2222-2222-222222222222",
      "assigneeId": "11111111-1111-1111-1111-111111111111",
      "dueDate": "2026-04-17",
      "createdAt": "2026-04-14T10:00:00Z",
      "updatedAt": "2026-04-14T10:00:00Z"
    }
  ]
}
```

#### `GET /projects/{id}/stats`

```json
// Response 200
{
  "projectId": "22222222-2222-2222-2222-222222222222",
  "byStatus": {
    "todo": 1,
    "in_progress": 1,
    "done": 1
  },
  "byAssignee": [
    {
      "assigneeId": "11111111-1111-1111-1111-111111111111",
      "name": "Test User",
      "count": 3
    }
  ],
  "total": 3
}
```

#### `PATCH /projects/{id}` — owner only

```json
// Request (all fields optional)
{
  "name": "Updated Name",
  "description": "Updated description"
}

// Response 200 — returns updated project object
```

#### `DELETE /projects/{id}` — owner only

```
// Response 204 No Content
```

---

### Tasks

#### `GET /projects/{id}/tasks?status=todo&assignee=<uuid>&page=1&limit=20`

```json
// Response 200
{
  "tasks": [
    {
      "id": "33333333-3333-3333-3333-333333333331",
      "title": "Draft API contract",
      "status": "todo",
      "priority": "high",
      "projectId": "22222222-2222-2222-2222-222222222222",
      "assigneeId": "11111111-1111-1111-1111-111111111111",
      "dueDate": "2026-04-17",
      "createdAt": "2026-04-14T10:00:00Z",
      "updatedAt": "2026-04-14T10:00:00Z"
    }
  ],
  "page": 1,
  "limit": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

#### `POST /projects/{id}/tasks`

```json
// Request
{
  "title": "Draft API contract",
  "description": "Prepare the first version of the TaskFlow backend API contract.",
  "priority": "high",
  "assigneeId": "11111111-1111-1111-1111-111111111111",
  "dueDate": "2026-04-17"
}

// Response 201
{
  "id": "33333333-3333-3333-3333-333333333331",
  "title": "Draft API contract",
  "description": "Prepare the first version of the TaskFlow backend API contract.",
  "status": "todo",
  "priority": "high",
  "projectId": "22222222-2222-2222-2222-222222222222",
  "assigneeId": "11111111-1111-1111-1111-111111111111",
  "dueDate": "2026-04-17",
  "createdAt": "2026-04-14T10:00:00Z",
  "updatedAt": "2026-04-14T10:00:00Z"
}
```

#### `PATCH /tasks/{id}`

```json
// Request (all fields optional)
{
  "title": "Updated title",
  "status": "in_progress",
  "priority": "medium",
  "assigneeId": "11111111-1111-1111-1111-111111111111",
  "dueDate": "2026-04-20"
}

// Response 200 — returns updated task object
```

#### `DELETE /tasks/{id}` — project owner or task creator only

```
// Response 204 No Content
```

---

### Error contract

| Status | When |
|--------|------|
| 400 | Validation failed |
| 401 | Missing or invalid token |
| 403 | Authenticated but not authorized (e.g. not project owner) |
| 404 | Resource not found |

```json
// 400
{ "error": "validation failed", "fields": { "title": "must not be blank" } }

// 401
{ "error": "unauthorized" }

// 403
{ "error": "forbidden" }

// 404
{ "error": "not found" }
```

## 7. Tests

### Run tests locally

```bash
cd backend
mvn test
```

### Included tests

- context bootstrap test for the application
- auth login controller test
- protected endpoint unauthorized test
- task delete authorization test

## 8. Bonus Features Implemented

- Pagination on list endpoints
- `GET /projects/{id}/stats`
- 3 focused tests covering auth and authorization behavior

## 9. What I'd Do With More Time

If I had more time, I would improve the submission in these areas:
- replace the focused mock-based tests with fuller Testcontainers-backed integration tests against PostgreSQL
- add a Postman environment file and Bruno collection as well for reviewer convenience
- add structured request logging and correlation IDs
- add update support for explicitly clearing nullable task fields like `assignee_id` and `due_date`
- add a lightweight OpenAPI spec for easier endpoint exploration

Overall, I prioritized correctness, clear authorization rules, migration safety, and a clean reviewer experience over adding broader platform features.
