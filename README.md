# Headless CMS — UCSal

A lightweight, zero-dependency Headless CMS REST API built with pure Java 21 and PostgreSQL. Developed for the Universidade Católica do Salvador (UCSal) to manage academic content: courses, professors, news, articles, projects, and events.

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Layers Explained](#layers-explained)
- [Database Schema](#database-schema)
- [API Reference](#api-reference)
- [Setup & Running](#setup--running)
- [Request & Response Examples](#request--response-examples)

---

## Overview

This project is a **Headless CMS**, meaning it exposes a JSON REST API with no frontend attached. Any client (web app, mobile app, or another service) can consume the API to read and write content.

**Key design decisions:**
- No frameworks (no Spring, no Quarkus) — uses only the Java Standard Library's built-in `HttpServer`
- No ORM — uses plain JDBC with `PreparedStatement` for all database operations
- Single external dependency: the PostgreSQL JDBC driver

---

## Architecture

The application follows a classic **3-layer architecture**:

```
HTTP Request
     │
     ▼
┌─────────────┐
│   Handler   │  ← Receives HTTP, parses JSON, returns HTTP response
│  (Layer 1)  │
└──────┬──────┘
       │ calls
       ▼
┌─────────────┐
│ Repository  │  ← Executes SQL queries against PostgreSQL
│  (Layer 2)  │
└──────┬──────┘
       │ uses
       ▼
┌─────────────┐
│    Model    │  ← Plain Java objects (POJOs) that represent data
│  (Layer 3)  │
└─────────────┘
```

Each layer has a single responsibility:
- **Handler** — HTTP concerns only (routing, status codes, JSON in/out)
- **Repository** — Database concerns only (SQL, result mapping)
- **Model** — Data structure only (fields, serialization to JSON)

---

## Project Structure

```
src/main/java/com/cms/
│
├── App.java                          # Entry point — boots the server
│
├── db/
│   └── Database.java                 # PostgreSQL connection singleton
│
├── server/
│   ├── Router.java                   # Maps URL paths → Handlers
│   └── JsonUtil.java                 # JSON read/write/escape helpers
│
├── model/
│   ├── enums/
│   │   ├── UserRole.java             # admin | editor | professor | student | viewer
│   │   ├── ContentStatus.java        # draft | in_review | published | archived
│   │   ├── EventModality.java        # presencial | online | hibrido
│   │   └── SemesterPeriod.java       # S1 ("1") | S2 ("2")
│   │
│   ├── User.java
│   ├── Course.java
│   ├── Professor.java
│   ├── Discipline.java
│   ├── DisciplineOffering.java
│   ├── Category.java
│   ├── Tag.java
│   ├── News.java
│   ├── Article.java
│   ├── Project.java
│   ├── Event.java
│   ├── FileEntity.java               # Named FileEntity to avoid collision with java.io.File
│   └── AuditLog.java
│
├── repository/
│   ├── UserRepository.java
│   ├── CourseRepository.java
│   ├── NewsRepository.java
│   ├── ArticleRepository.java
│   ├── ProjectRepository.java
│   └── EventRepository.java
│
└── handler/
    ├── UserHandler.java
    ├── CourseHandler.java
    ├── NewsHandler.java
    ├── ArticleHandler.java
    ├── ProjectHandler.java
    └── EventHandler.java
```

---

## Layers Explained

### `App.java` — Entry Point

Starts the application in three steps:
1. Calls `Database.testConnection()` to validate the PostgreSQL connection — **fails fast** if the DB is unreachable
2. Creates a `HttpServer` on port `8080`
3. Delegates route registration to `Router` and starts listening

### `Router.java` — Route Table

Registers each URL prefix with its corresponding handler using `server.createContext(path, handler)`. The `HttpServer` routes any request whose path *starts with* the registered prefix to that handler.

```
/api/users    → UserHandler
/api/courses  → CourseHandler
/api/news     → NewsHandler
/api/articles → ArticleHandler
/api/projects → ProjectHandler
/api/events   → EventHandler
/health       → inline lambda
```

### `Database.java` — Connection

A simple factory that opens a new JDBC connection per request. Configuration comes from environment variables:

| Variable      | Default                                      |
|---------------|----------------------------------------------|
| `DB_URL`      | `jdbc:postgresql://localhost:5432/ucsal_cms` |
| `DB_USER`     | `postgres`                                   |
| `DB_PASSWORD` | `postgres`                                   |

> **Usage pattern:** every repository uses `try-with-resources` to guarantee the connection is closed after each query.

### `JsonUtil.java` — JSON Utilities

Since there is no JSON library, this class handles all JSON I/O manually:

| Method             | Purpose                                           |
|--------------------|---------------------------------------------------|
| `sendJson`         | Writes a JSON string as an HTTP response body     |
| `sendError`        | Writes `{"error":"..."}` with a given status code |
| `readBody`         | Reads the raw request body as a String            |
| `extractField`     | Extracts a single string value from a JSON string |
| `escapeJson`       | Escapes `"`, `\`, `\n`, `\r`, `\t` in strings    |
| `setCorsHeaders`   | Adds CORS headers to allow browser clients        |

> **Limitation:** `extractField` only handles flat string fields. Nested objects or arrays must be handled separately.

### Models — POJOs

Each model class represents one database table. All models follow the same pattern:

```java
// 1. Auto-generate ID and timestamps in the default constructor
public User() {
    this.id        = UUID.randomUUID().toString();
    this.createdAt = Instant.now().toString();
    this.updatedAt = this.createdAt;
}

// 2. toJson() serializes the object to a JSON string manually
public String toJson() { ... }

// 3. Private helper q() handles null-safe JSON string quoting
private static String q(Object v) {
    if (v == null) return "null";
    return "\"" + JsonUtil.escapeJson(v.toString()) + "\"";
}
```

### Repositories — Data Access

Each repository follows the same CRUD structure:

| Method          | SQL operation             |
|-----------------|---------------------------|
| `findAll()`     | `SELECT ... ORDER BY ...` |
| `findById(id)`  | `SELECT ... WHERE id=?`   |
| `findBySlug(s)` | `SELECT ... WHERE slug=?` |
| `save(entity)`  | `INSERT ... RETURNING id` |
| `update(entity)`| `UPDATE ... WHERE id=?`   |
| `delete(id)`    | `DELETE WHERE id=?`       |

PostgreSQL ENUMs are cast explicitly in SQL using `?::user_role`, `?::content_status`, etc. UUIDs are cast with `?::uuid`.

The `RETURNING id, created_at, updated_at` clause on `INSERT` lets the repository populate the generated fields back onto the Java object without a second query.

### Handlers — HTTP Controllers

Each handler implements `HttpHandler` and follows the same flow:

```
1. Set CORS headers
2. If OPTIONS → respond 204 (preflight)
3. Read method (GET/POST/PUT/DELETE)
4. Extract ID from path (if present)
5. Dispatch to the right private method
6. Catch all exceptions → 500 Internal Server Error
```

The ID is extracted from the URL path by splitting on `/`:
- `/api/news`      → `parts[2] = "news"` → no ID
- `/api/news/{id}` → `parts[3] = "{id}"` → ID present

---

## Database Schema

The PostgreSQL schema (`Anotacao.java`) defines **11 main tables** and **~10 junction tables**:

### ENUMs

| Type              | Values                                              |
|-------------------|-----------------------------------------------------|
| `user_role`       | `admin`, `editor`, `professor`, `student`, `viewer` |
| `content_status`  | `draft`, `in_review`, `published`, `archived`       |
| `event_modality`  | `presencial`, `online`, `hibrido`                   |
| `semester_period` | `'1'`, `'2'`                                        |

### Main Tables

| Table                  | Description                                         |
|------------------------|-----------------------------------------------------|
| `users`                | All system users — authentication + profile         |
| `courses`              | Academic courses (graduation/postgraduate)          |
| `professors`           | Academic profile extending `users`                  |
| `disciplines`          | Course subjects/classes                             |
| `discipline_offerings` | Which professor teaches which subject in which term |
| `categories`           | Hierarchical content categories (parent/child)      |
| `tags`                 | Free-form content tags                              |
| `news`                 | University news posts                               |
| `articles`             | Academic publications/blog posts                    |
| `projects`             | Research and extension projects                     |
| `events`               | Lectures, seminars, academic weeks                  |
| `files`                | Centralized file/media repository                   |
| `audit_logs`           | Action trail — who did what and when                |

### Junction Tables (N:N relationships)

| Table                 | Connects               |
|-----------------------|------------------------|
| `professor_courses`   | professors ↔ courses   |
| `course_disciplines`  | courses ↔ disciplines  |
| `news_tags`           | news ↔ tags            |
| `news_categories`     | news ↔ categories      |
| `news_files`          | news ↔ files           |
| `article_authors`     | articles ↔ users       |
| `article_tags`        | articles ↔ tags        |
| `article_categories`  | articles ↔ categories  |
| `article_files`       | articles ↔ files       |
| `project_members`     | projects ↔ users       |
| `project_tags`        | projects ↔ tags        |
| `project_files`       | projects ↔ files       |
| `event_tags`          | events ↔ tags          |
| `event_files`         | events ↔ files         |
| `discipline_files`    | disciplines ↔ files    |

### Auto-update Trigger

A PostgreSQL trigger (`trg_<table>_updated_at`) automatically sets `updated_at = NOW()` on every `UPDATE` across all main tables. This means the application never needs to manually set `updated_at` when updating a record.

---

## API Reference

All endpoints return `application/json`. All request bodies must be `application/json`.

### Health Check

```
GET /health
→ 200 {"status":"ok"}
```

### Users — `/api/users`

| Method | Path             | Description          | Required fields                        |
|--------|------------------|----------------------|----------------------------------------|
| GET    | `/api/users`     | List all users       | —                                      |
| GET    | `/api/users/{id}`| Get user by ID       | —                                      |
| POST   | `/api/users`     | Create user          | `fullName`, `email`, `passwordHash`    |
| PUT    | `/api/users/{id}`| Update user          | any of: `fullName`, `avatarUrl`, `bio`, `role` |
| DELETE | `/api/users/{id}`| Delete user          | —                                      |

### Courses — `/api/courses`

| Method | Path               | Required fields    |
|--------|--------------------|--------------------|
| GET    | `/api/courses`     | —                  |
| GET    | `/api/courses/{id}`| —                  |
| POST   | `/api/courses`     | `name`, `slug`     |
| PUT    | `/api/courses/{id}`| any field          |
| DELETE | `/api/courses/{id}`| —                  |

### News — `/api/news`

| Method | Path             | Notes                                    |
|--------|------------------|------------------------------------------|
| GET    | `/api/news`      | Optional query: `?status=published`      |
| GET    | `/api/news/{id}` | —                                        |
| POST   | `/api/news`      | Required: `title`, `slug`, `body`, `authorId` |
| PUT    | `/api/news/{id}` | any field                                |
| DELETE | `/api/news/{id}` | —                                        |

### Articles — `/api/articles`

Same structure as News. Required on POST: `title`, `slug`, `body`, `authorId`.

Optional query on GET: `?status=published`

### Projects — `/api/projects`

Same structure. Required on POST: `title`, `slug`, `description`.

### Events — `/api/events`

Same structure. Required on POST: `title`, `slug`, `description`, `startsAt`.

### Status Codes

| Code | Meaning                              |
|------|--------------------------------------|
| 200  | OK                                   |
| 201  | Created                              |
| 204  | No Content (CORS preflight)          |
| 400  | Bad Request (missing required field) |
| 404  | Not Found                            |
| 405  | Method Not Allowed                   |
| 500  | Internal Server Error                |

---

## Setup & Running

### Prerequisites

- Java 21+
- Maven 3.6+
- PostgreSQL 14+

### 1. Create the database

```sql
CREATE DATABASE ucsal_cms;
\c ucsal_cms
-- run the full schema from Anotacao.java
```

### 2. Configure environment variables

```bash
export DB_URL=jdbc:postgresql://localhost:5432/ucsal_cms
export DB_USER=postgres
export DB_PASSWORD=your_password
```

### 3. Build and run

```bash
mvn compile exec:java
```

The server starts on `http://localhost:8080`.

---

## Request & Response Examples

### Create a course

```http
POST /api/courses
Content-Type: application/json

{
  "name": "Ciência da Computação",
  "slug": "ciencia-da-computacao",
  "code": "CC-001",
  "durationSemesters": "8"
}
```

```json
HTTP/1.1 201 Created

{
  "id": "3f2a1b4c-...",
  "name": "Ciência da Computação",
  "slug": "ciencia-da-computacao",
  "code": "CC-001",
  "description": null,
  "durationSemesters": 8,
  "coordinatorId": null,
  "isActive": true,
  "createdAt": "2026-04-04T12:00:00Z",
  "updatedAt": "2026-04-04T12:00:00Z"
}
```

### Create a news post

```http
POST /api/news
Content-Type: application/json

{
  "title": "Semana de TI 2026",
  "slug": "semana-de-ti-2026",
  "body": "A UCSal realiza a Semana de TI com palestras e workshops.",
  "authorId": "user-uuid-here",
  "status": "published"
}
```

### Filter published news

```http
GET /api/news?status=published
```

### Update a project status

```http
PUT /api/projects/some-uuid
Content-Type: application/json

{
  "status": "published"
}
```

### Delete a user

```http
DELETE /api/users/some-uuid

→ 200 {"deleted": true}
```
