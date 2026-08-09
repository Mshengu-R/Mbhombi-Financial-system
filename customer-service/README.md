# Customer Service

This microservice manages customer profile creation for the Mbhombi Financial Services platform. It supports profile onboarding, validation, persistence, and a notification hook for sending a confirmation email after a profile is created.

## What it provides
- Profile creation via POST `/api/v1/profiles/sign-up`
- Validation for required profile fields
- Data persistence with PostgreSQL
- Notification email dispatch after successful profile creation
- Basic actuator health endpoint

## Local development
1. Ensure Java 25 and Maven are installed.
2. Start PostgreSQL and create a database named `customerdb` (or set the `DB_URL` environment variable).
3. Optionally start a local SMTP server such as MailHog on port `1025` for email testing.
4. Run the application:

```cmd
mvnw.cmd spring-boot:run
```

## Example request
```json
{
  "email": "user@example.com",
  "phone": "27761234567",
  "idno": 9801011234567,
  "title": "MR",
  "name": "Lerato",
  "surname": "Mokoena",
  "dateOfBirth": "1998-01-01T00:00:00.000Z",
  "customerLanguage": "ENGLISH",
  "gender": "MALE",
  "ethnicity": "AFRICAN",
  "country": "SOUTH_AFRICA",
  "homeAdress": ["10 Main Road", "Johannesburg"]
}
```

## Notes
- The email notification is fire-and-forget and will not block profile creation.
- If SMTP is not configured, the service logs a warning and continues.
