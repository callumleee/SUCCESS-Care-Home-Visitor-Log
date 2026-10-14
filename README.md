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

## Web app
- **Kiosk** (`/kiosk`): a tablet screen at the front door. Visitors tap "I'm arriving", enter their name, and pick the resident, or tap "I'm leaving" and find their name.
- **Dashboard** (`/dashboard`): who's in the building right now (auto-refreshes, printable for evacuations), and visit history by day with search and CSV export.
- **Admin** (`/admin`): visit stats, busiest-times and visits-per-resident charts, and managing residents and staff accounts.
- Works on tablets, desktops, and phones.

## Tech stack
- **Frontend:** Next.js, React, TypeScript, Tailwind CSS
- **Backend:** Java 21, Spring Boot 4, Spring Security (JWT login), Spring Data JPA, Flyway, Maven
- **Database:** PostgreSQL (H2 for local development)
- **Testing:** JUnit 5, MockMvc

## API
Staff log in with `POST /api/auth/login`, which returns a token sent as `Authorization: Bearer <token>`. HTTP Basic also works for scripts and curl.

| Method | Endpoint | Who | Description |
|---|---|---|---|
| POST | `/api/auth/login` | Anyone | Log in and get a token |
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
Requires Java 21 and Node.js. The API uses an in-memory database with a few fake demo residents.

```bash
./mvnw spring-boot:run          # API at http://localhost:8080
cd web && npm install && npm run dev   # website at http://localhost:3000
```

Log in on the website as `admin` / `dev-admin-password`. An admin account is created on first start (`admin` / `dev-admin-password`; set `ADMIN_PASSWORD` to change it).

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
export JWT_SECRET=...  # 32+ random characters
export CORS_ALLOWED_ORIGINS=https://your-website-domain
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```
The database schema is created and updated automatically by Flyway migrations (`src/main/resources/db/migration`).

## Tests and checks
```bash
./mvnw test                 # API tests
cd web && npm run lint && npm run build
```

## Roadmap
- QR code check-in for returning visitors
- Health screening question and visiting hours
- Automatic sign-out at closing time
- Docker, CI with GitHub Actions, and cloud deployment
