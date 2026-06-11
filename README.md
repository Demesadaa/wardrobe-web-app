# Wardrobe Web App Midterm Project

This project is a full-stack wardrobe management application built for the midterm requirements. The backend is a Java Spring Boot REST API with layered architecture, DTOs, validation, JPA/Hibernate persistence, entity relationships, CRUD operations, and Swagger UI. The frontend is a React application that lets users register, log in, save clothing pieces, and generate outfits.

## Project Idea

The application helps users build a digital wardrobe. A user can create an account, manage a profile, add clothing pieces, tag each piece as footwear, pants, torso, or headwear, and view randomized outfit combinations on a simple character lobby.

The frontend supports both camera capture and uploading pre-taken clothing photos. Uploaded or captured photos are processed on the client side to attempt background removal and clothing category recognition before being saved.

## Midterm Requirements Coverage

| Requirement | Implementation |
| --- | --- |
| Layered architecture | Backend uses Controller, Service, and Repository layers |
| REST controllers | APIs use POST, GET, PUT, and DELETE methods |
| Database connection | Spring Data JPA/Hibernate with H2 database |
| At least 2 entities | `AppUser`, `Profile`, and `ClothingPiece` |
| Entity relationship | `AppUser` to `Profile` is one-to-one; `AppUser` to `ClothingPiece` is one-to-many/many-to-one |
| CRUD operations | CRUD endpoints exist for users, profiles, and clothing pieces |
| Validation | Request DTOs use annotations such as `@NotBlank`, `@NotNull`, `@Size`, and `@Email` |
| Exception handling | Invalid requests return Spring error responses instead of crashing the app |
| DTOs | APIs use request and response DTOs instead of returning entities directly |
| Swagger integration | Swagger UI is available and documents the REST endpoints |

## Backend Architecture

The Spring Boot backend is organized by responsibility:

```text
backend/src/main/java/com/wardrobe
├── config
│   ├── OpenApiConfig.java
│   └── WebConfig.java
├── security
│   └── SecurityConfig.java
├── user
│   ├── controllers, services, repositories, DTOs, and user/profile entities
└── wardrobe
    ├── controllers, services, repositories, DTOs, and clothing entities
```

Main backend layers:

- Controller layer receives HTTP requests and returns DTO responses.
- Service layer contains core business logic.
- Repository layer handles database access using Spring Data JPA.
- Entity layer defines persistent database models and relationships.
- DTO layer separates API input/output from database entities.

## Main Entities

### AppUser

Represents a registered user account.

Fields include:

- `id`
- `username`
- `email`
- `passwordHash`
- `profile`

Relationships:

- One user has one profile.
- One user can own many clothing pieces.

### Profile

Represents public user profile information.

Fields include:

- `id`
- `user`
- `displayName`
- `bio`

Relationship:

- Many profile records are not allowed for one user; each profile belongs to one user.

### ClothingPiece

Represents a saved wardrobe item.

Fields include:

- `id`
- `owner`
- `category`
- `imageUrl`
- `storagePath`
- `originalFilename`
- `createdAt`

Relationship:

- Many clothing pieces belong to one user.

## REST API Overview

Authentication:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/me`

Users:

- `POST /api/users`
- `GET /api/users`
- `GET /api/users/{id}`
- `PUT /api/users/{id}`
- `DELETE /api/users/{id}`

Profiles:

- `GET /api/profile`
- `PUT /api/profile`
- `POST /api/profiles`
- `GET /api/profiles`
- `GET /api/profiles/{id}`
- `PUT /api/profiles/{id}`
- `DELETE /api/profiles/{id}`

Clothing pieces:

- `POST /api/pieces`
- `GET /api/pieces`
- `GET /api/pieces/{id}`
- `PUT /api/pieces/{id}`
- `DELETE /api/pieces/{id}`
- `GET /api/clothing-pieces`
- `GET /api/clothing-pieces/{id}`
- `GET /api/outfit/random`

## Swagger UI

After starting the backend, Swagger UI is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

Swagger can be used to view and test the backend REST endpoints.

## Frontend Features

- Register and log in.
- Edit profile information.
- Add clothing pieces by camera capture.
- Add clothing pieces by uploading existing photos.
- Attempt client-side background removal.
- Attempt client-side clothing category recognition.
- Manually override clothing category tags.
- View a lobby character wearing selected wardrobe pieces.
- Randomize an outfit.
- Cycle left and right through pieces in each category.

## Technology Stack

Backend:

- Java 17
- Spring Boot
- Spring Web
- Spring Security
- Spring Data JPA
- Hibernate
- H2 Database
- Bean Validation
- Swagger/OpenAPI

Frontend:

- React
- TypeScript
- Vite
- Browser camera APIs
- Client-side image processing libraries

Deployment:

- Backend prepared for Render using Docker
- Frontend prepared for Vercel

## Run Locally

Start the backend:

```powershell
cd backend
mvn spring-boot:run
```

The default backend profile is `dev`. To choose a profile explicitly from the command line:

```powershell
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

From an IDE, set the active Spring profile to `dev` or `prod` in the run configuration.

Profiles:

- `dev` uses an in-memory H2 database, creates/drops schema for local runs, enables the H2 console, seeds test users, and sets detailed `com.wardrobe` logging.
- `prod` uses PostgreSQL settings from environment variables, disables the H2 console, validates the schema, uses stricter cookie defaults, and reduces log verbosity.

Start the frontend:

```powershell
cd frontend
npm install
npm run dev
```

Local URLs:

```text
Backend: http://localhost:8080
Frontend: http://localhost:5173
Swagger: http://localhost:8080/swagger-ui/index.html
```

Camera access usually requires `localhost` or HTTPS.

## Advanced Configuration

The backend defines validated custom configuration properties in `AppSettingsProperties` using the `app.settings` prefix:

- `app.settings.title` controls the displayed API title.
- `app.settings.default-page-limit` controls the configured default page size and is validated with minimum/maximum bounds.
- `app.settings.support-email` stores the support contact email and is validated as an email.
- `app.settings.external-style-service-url` stores the external styling service URL.
- `app.settings.ai-suggestions-enabled` controls the AI suggestion feature flag.

These settings are injected into `GET /api/meta`, which returns dynamic API metadata.

## Internationalization

The API supports localized response and error messages through UTF-8 resource bundles:

```text
backend/src/main/resources/messages.properties
backend/src/main/resources/messages_en.properties
```

The backend uses `AcceptHeaderLocaleResolver`, so requests can select a language with the standard `Accept-Language` header.

Examples:

```powershell
curl -H "Accept-Language: en" http://localhost:8080/api/meta
curl -H "Accept-Language: ka" http://localhost:8080/api/meta
```

Localized validation errors can be tested by sending invalid data to endpoints such as `POST /api/auth/register` or `PUT /api/profile`. Validation annotations use message keys like `{validation.username.required}` and `{validation.password.size}`.

## Structured Logging

The backend uses SLF4J logging in services and exception handling. Logs are written to the console and to:

```text
backend/logs/app.log
```

Rolling log files use this pattern:

```text
backend/logs/app-yyyy-MM-dd.N.log
```

The `dev` profile enables more detailed package logging, and the `prod` profile uses stricter root logging levels.

## Database

The project uses H2 by default for simple local setup.

Database configuration is in:

```text
backend/src/main/resources/application.properties
```

The local database is stored under `backend/data`, which is ignored by Git.

## Deployment Notes

Backend on Render:

- Service type: Web Service
- Runtime: Docker
- Root directory: `backend`
- Dockerfile path: `Dockerfile`

Important Render environment variables:

```text
FRONTEND_ORIGINS=https://wardrobe-web-app.vercel.app,http://localhost:5173,http://127.0.0.1:5173
SESSION_COOKIE_SAME_SITE=none
SESSION_COOKIE_SECURE=true
```

Frontend on Vercel:

- Root directory: `frontend`
- Build command: `npm run build`
- Output directory: `dist`

Important Vercel environment variable:

```text
VITE_API_BASE_URL=https://wardrobe-web-app.onrender.com
```

## Repository

GitHub repository:

```text
https://github.com/Demesadaa/wardrobe-web-app
```
