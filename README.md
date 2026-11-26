# RAF Cloud Backend

A Kotlin / Spring Boot backend that simulates the lifecycle of cloud virtual machines. Users create machines and start, stop or restart them. The operations run asynchronously, and status changes are pushed to clients in real time.

## Features

- **JWT authentication with permission claims.** Login issues an HS256 token carrying the user id and permissions. Passwords are hashed with BCrypt.
- **Custom `@CheckSecurity` annotation.** An AOP aspect enforces the required permissions (create, start, stop, restart, destroy and search machines, manage users) on controller methods.
- **Async state machines.** Start, stop and restart move a machine through transitional states (for example `STARTING` -> `RUNNING`, `RESTARTING` -> `STOPPED` -> `RUNNING`) on a dedicated thread pool. A delay of a few seconds simulates the work.
- **Optimistic locking.** The machine entity uses a JPA `@Version` field and checks the expected previous status, so a concurrent action aborts the stale one. Failures are written to an error log table.
- **Live status updates.** Every status change is published over STOMP WebSocket (endpoint `/ws`, topics under `/topic/machines/...`).
- **Scheduled operations.** Users can queue an operation for a later time. A scheduler runs every 30 seconds, picks up due operations, validates the machine state and dispatches them.

## Tech stack

Kotlin 1.9, Spring Boot 3.4, Spring Data JPA (Hibernate), MySQL, Spring AOP, Spring WebSocket (STOMP), jjwt, jBCrypt, Gradle, JDK 21.

## Running locally

1. Install JDK 21 and MySQL 8, and start MySQL on `localhost:3306`.
2. Check the database settings in `src/main/resources/application.properties`. The `raf_cloud_db` database is created automatically.
3. Start the app:

   ```
   ./gradlew bootRun
   ```

   The API is served on `http://localhost:8080`.

On first start a data seeder creates a few demo users with different permission sets.

## Configuration note

The database credentials and the JWT secret in `application.properties` are local development placeholders, not real secrets. Replace them before deploying anywhere.
