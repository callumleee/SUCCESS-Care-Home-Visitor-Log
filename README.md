# Care Home Visitor Log

A visitor sign-in system for care homes, replacing paper sign-in sheets.

Visitors sign in and out at the front desk. Staff can see who is currently in the building (important during a fire or emergency), look up a resident's visit history, and view visit statistics.

## Why
Many care homes still track visitors on paper. Paper logs are slow to fill in, hard to search, and make it hard to know who is in the building during an emergency.

## Features
- **Sign in / sign out** visitors; returning visitors are matched by name and phone number
- **Who's in the building** right now
- **Visit history** by day or by resident
- **Stats** for admins: total visits, average visit length, most visited resident, most frequent visitor
- **Resident management**: residents who move out are deactivated, not deleted, so history is kept
- **Staff logins with roles**: front desk vs. admin, with hashed passwords

## Tech stack
Java 21, Spring Boot 4, Spring Security, Spring Data JPA, PostgreSQL (H2 for local development), Flyway, JUnit 5, MockMvc, Maven

## API
All endpoints require a staff login (HTTP Basic).

| Method | Endpoint | Who | Description |
|---|---|---|---|
| POST | `/api/visits` | Staff | Sign a visitor in (`visitorName`, `visitorPhone`, `residentId`) |
| PATCH | `/api/visits/{id}/signout` | Staff | Sign a visitor out |
| GET | `/api/visits?active=true` | Staff | Visitors currently in the building |
| GET | `/api/visits?date=2026-10-13` | Staff | Visits on a given day |
| GET | `/api/residents` | Staff | Active residents |
| GET | `/api/residents/{id}/visits` | Staff | A resident's visit history |
| POST | `/api/residents` | Admin | Add a resident |
| DELETE | `/api/residents/{id}` | Admin | Deactivate a resident |
| GET | `/api/stats` | Admin | Visit statistics |
| GET / POST | `/api/staff` | Admin | List / create staff accounts |

Errors are returned as standard JSON problem details (400 invalid input, 401 not logged in, 403 not allowed, 404 not found, 409 conflict such as signing out twice).

## Running locally
Requires Java 21. Uses an in-memory database with a few fake demo residents.

```bash
./mvnw spring-boot:run
```

The API runs at http://localhost:8080. An admin account is created on first start (`admin` / `dev-admin-password`; set `ADMIN_PASSWORD` to change it).

```bash
curl -u admin:dev-admin-password localhost:8080/api/residents
curl -u admin:dev-admin-password -X POST localhost:8080/api/visits \
  -H 'Content-Type: application/json' \
  -d '{"visitorName":"Jane Doe","residentId":1}'
```

## Running with PostgreSQL
```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/visitorlog
export DATABASE_USERNAME=... DATABASE_PASSWORD=... ADMIN_PASSWORD=...
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```
The database schema is created and updated automatically by Flyway migrations (`src/main/resources/db/migration`).

## Tests
```bash
./mvnw test
```

## Roadmap
- Web app: React/Next.js tablet kiosk for sign-in and a staff dashboard
- QR code check-in for returning visitors
- Health screening question and visiting hours
- Docker, CI with GitHub Actions, and cloud deployment
