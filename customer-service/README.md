# Customer Service

This microservice manages customer profiles for the Mbhombi Financial Services platform. Account creation is handled by the authentication service; this service manages the authenticated customer's existing profile.

## What it provides
- Read profile via GET `/api/v1/profiles/me`
- Update profile via PUT `/api/v1/profiles/me`
- Delete profile via DELETE `/api/v1/profiles/me`
- Data persistence with PostgreSQL
- Basic actuator health endpoint

## Local development
1. Ensure Java 25 and Maven are installed.
2. Start PostgreSQL and create a database named `customerdb` (or set the `DB_URL` environment variable).
3. Run the application:

```cmd
mvnw.cmd spring-boot:run
```

## Notes
- Every profile operation requires a valid JWT issued by the authentication service.
- The profile owner is resolved from the JWT `customerId` claim; client-supplied customer IDs are not accepted.
