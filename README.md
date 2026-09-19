# RESTAPI--Library

A REST API for managing a library of books and authors, built with Spring Boot to practice secure, well-tested backend development: JWT authentication, role-based access, validation, soft delete and integration tests.

Anyone can browse books and authors. Signed-in users can add and edit them, and only admins can delete.

## Tech stack

- Java 17+, Spring Boot 4.1
- Spring Security with stateless JWT authentication (jjwt) and BCrypt password hashing
- Spring Data JPA with Hibernate, H2 in-memory database
- Bean Validation
- JUnit 5 and MockMvc integration tests

## Features

- Books and authors with full CRUD
- Paging and sorting, plus search by title, genre and author
- Roles: `USER` (add and edit) and `ADMIN` (also delete)
- Soft delete: deleted books and authors are hidden, and deleting an author also removes their books
- Consistent JSON errors: `401` for missing or invalid tokens, `403` for missing role, `404`, `409` for a taken username, and `400` with a message per invalid field

## Getting started

### Prerequisites

- JDK 17 or newer on your `PATH` (or `JAVA_HOME` set)

### Run

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Any system with Maven installed
mvn spring-boot:run
```

The API listens on `http://localhost:8080`. The database is in memory, so all data is reset on every restart.

### Configuration

No secrets are stored in the repo. All of these are optional environment variables:

| Variable | Purpose |
|----------|---------|
| `JWT_SECRET` | Base64 signing key of at least 32 bytes. Without it a random key is generated on every start. |
| `APP_ADMIN_USERNAME` | Username of an admin created at startup. |
| `APP_ADMIN_PASSWORD` | Password of that admin. |

Registration only ever creates regular users, so an admin can only be created through these two variables.

### Run the tests

```bash
.\mvnw.cmd test
```

## API overview

Protected endpoints need the header `Authorization: Bearer <token>`.

### Auth

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Create a user (username 3 to 50 characters, password 8 to 100) and receive a token |
| POST | `/auth/login` | Sign in and receive a token |

### Books

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/books` | Public | List books. Query: `page` (from 0), `size` (1 to 100), `sort` (`id`, `title` or `genre`), `authorId` |
| GET | `/books/{id}` | Public | Get one book |
| GET | `/books/search` | Public | Search by `title`, `genre` and `authorId`, with `page` and `size` |
| POST | `/books` | Signed in | Create a book |
| PUT | `/books/{id}` | Signed in | Update a book |
| DELETE | `/books/{id}` | Admin | Delete a book |

### Authors

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/authors` | Public | List authors |
| GET | `/authors/{id}` | Public | Get one author |
| POST | `/authors` | Signed in | Create an author |
| PUT | `/authors/{id}` | Signed in | Update an author |
| DELETE | `/authors/{id}` | Admin | Delete an author and their books |

## Usage example

```bash
# Register and copy the token from the response
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "demo", "password": "demo12345"}'

# Create an author, then a book by them
curl -X POST http://localhost:8080/authors \
  -H "Authorization: Bearer YOUR_TOKEN" -H "Content-Type: application/json" \
  -d '{"name": "Astrid Lindgren", "nationality": "Swedish"}'

curl -X POST http://localhost:8080/books \
  -H "Authorization: Bearer YOUR_TOKEN" -H "Content-Type: application/json" \
  -d '{"title": "Pippi Longstocking", "genre": "Children", "authorId": 1}'

# Browse without signing in
curl "http://localhost:8080/books?sort=title&size=5"
```
