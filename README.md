# Care Home Visitor Log

A digital replacement for paper visitor sign-in sheets at care homes.

Visitors sign in and out when visiting a resident. Each visit records the visitor, the resident, and the time in and out. Staff can see who is currently in the building and view stats such as average visit length, the most visited residents, and the most frequent visitors.

## Why
Many care homes, including the one my grandparents live in, still track visitors on paper. Paper logs are slow to fill in, hard to search, and make it hard to know who is in the building during an emergency.

## Current features
- Sign visitors in and out
- See all visitors currently signed in
- Visit statistics: average visit length, most visited resident, most frequent visitor
- Every sign-in and sign-out is saved automatically to a SQLite database
- Event log of sign-ins and sign-outs
- Desktop GUI (Java Swing)

## Tech stack
Java 21, Maven, Swing, SQLite (JDBC), JUnit 5

## Running it
Requires Java 21 and Maven.
```bash
mvn test            # run the tests
mvn compile exec:java   # run the app (creates data/visitorlog.db on first run)
```

## Roadmap
- Web app: React/Next.js tablet kiosk for sign-in and a staff dashboard
- REST API with Spring Boot and PostgreSQL
- Staff login with roles
- QR code check-in for returning visitors
- Docker, CI with GitHub Actions, and cloud deployment
