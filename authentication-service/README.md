# Authentication Service

This service is the identity and access boundary for the banking platform. It owns customer credential lifecycle, authentication, stateless JWT issuance, and customer consent records. It is intentionally small, explicit, and security-focused: no sessions are stored in the application, and authorization is enforced with JWT validation on each protected request.

## 1. Purpose

The service is responsible for:

- registering new customers
- validating credentials during sign-in
- issuing signed JWT access tokens
- enforcing customer status rules (`ACTIVE`, `DEACTIVATED`, `DELETED`)
- recording customer consent events
- protecting downstream endpoints with Spring Security

This service does not act as a general-purpose user profile service. It is the trust anchor for authentication and access decisions.

## 2. Architectural responsibilities

### Core responsibilities

- Credential persistence for customer accounts
- Password hashing with BCrypt
- JWT generation and validation
- Account status enforcement before authentication
- Consents tracking for regulatory requirements
- Security filtering for protected routes

### Security model

The service uses Spring Security in a stateless configuration:

- CSRF is disabled because JWT is used instead of cookie-based sessions
- requests are stateless using `SessionCreationPolicy.STATELESS`
- public endpoints are:
  - `POST /api/v1/auth/register`
  - `POST /api/v1/auth/login`
- protected routes require a valid `Authorization: Bearer <token>` header
- unauthorized requests return HTTP `401`
- malformed JSON or validation failures return HTTP `400`
- duplicate data collisions return HTTP `409`
- a disabled or deleted account is blocked with a domain-specific forbidden / gone response

## 3. Runtime components

### Main packages

- `config` — Spring Security and JWT filter configuration
- `controller` — REST endpoints for register/login/consents
- `service` — domain logic and business rules
- `entity` — persistence model and enumerations
- `repository` — Spring Data repository access
- `security` — principal implementation used by Spring Security
- `states` — state pattern for customer lifecycle status validation and transitions
- `dto` — request/response contracts

## 4. Domain model

### Customer

The main entity is `Customer`, mapped to the `customer_profile` table.

Important fields:

- `email` — unique identity used as the login username
- `username` — stored as the email for the current service contract
- `password` — BCrypt hashed before persistence
- `status` — state value from `CustomerStatus`
- `role` — current default is `CUSTOMER`
- `phone`, `idNumber`, `dateOfBirth`, `country`, etc.

### Status model

The customer lifecycle is represented as an enum:

```java
public enum CustomerStatus {
    ACTIVE,
    DEACTIVATED,
    DELETED
}
```

The service uses the state pattern to validate the current status before registration and authentication. This keeps lifecycle rules centralized and prevents direct logic duplication across the auth flow.

### State logic summary

```text
ACTIVE
  - allowed to register as a valid customer
  - allowed to sign in

DEACTIVATED
  - blocked from authentication
  - returns a forbidden-style business error

DELETED
  - cannot be reused or authenticated
  - returns a gone-style business error
```

## 5. Request flow

### Registration flow

1. Client sends `POST /api/v1/auth/register` with a valid customer JSON payload.
2. Spring Security permits this endpoint without authentication.
3. `AuthController.addNewUser(...)` invokes `AuthService.saveUser(...)`.
4. The service validates required fields and uniqueness.
5. The password is encoded with `BCryptPasswordEncoder`.
6. The customer is assigned the default status: `ACTIVE`.
7. The entity is saved.
8. A success message is returned with HTTP `201 Created`.

### Login flow

1. Client sends `POST /api/v1/auth/login` with username and password.
2. Security permits the endpoint.
3. `AuthController.login(...)` delegates to `AuthService.authenticate(...)`.
4. Spring `AuthenticationManager` verifies credentials.
5. The service loads the persisted customer and validates the current state.
6. If the account is active, a JWT is created.
7. The JWT contains the username as the subject and the custom claims:
   - `customerId`
   - `role`
8. The token is returned to the caller as a plain string.

### Protected request flow

1. Client sends a protected request with `Authorization: Bearer <token>`.
2. `JwtFilter` intercepts the request.
3. The filter extracts the token, validates the signature and subject, and resolves the user.
4. If valid, the request is authenticated via `SecurityContextHolder`.
5. The request proceeds to the controller.
6. If invalid, the request is rejected with HTTP `401`.

## 6. Endpoint contract

### Public endpoints

| Method | Endpoint | Purpose | Success | Notes |
|---|---|---|---|---|
| POST | `/api/v1/auth/register` | Register a customer | 201 | Validates required fields and uniqueness |
| POST | `/api/v1/auth/login` | Authenticate and receive JWT | 200 | Returns a signed JWT string |

### Protected endpoints

| Method | Endpoint | Purpose | Success | Notes |
|---|---|---|---|---|
| POST | `/api/v1/auth/consents` | Save consent | 200 | Requires valid JWT |
| POST | `/api/v1/auth/logout` | Compatibility endpoint | 200 | Stateless token model; logout is logical |

## 7. JWT behavior

The JWT service signs access tokens using a secret key loaded from configuration.

Token contract:

- subject = username
- claim `customerId` = UUID string
- claim `role` = role name
- expiration controlled by `security.jwt.access-token-expiration`

This design ensures downstream services can identify the authenticated customer without needing to share the raw password or session state.

## 8. Security configuration

`SecurityConfig` configures the filter chain with:

- stateless session management
- public permit-all for register/login
- authentication entry point returning `401` for unauthorized access
- JWT filter inserted before the username/password filter
- BCrypt password encoder

## 9. Configuration

Required application properties are defined in `src/main/resources/application.yaml`.

Important settings:

```yaml
security:
  jwt:
    secret: ${JWT_SECRET:...}
    access-token-expiration: ${AUTH_EXPIRATION_TIME:15m}

server:
  port: ${AUTH_SERVICE_SERVER:8089}
```

Important operational notes:

- keep `JWT_SECRET` consistent across all services that validate tokens
- never store production secrets directly in source control
- use `.env` or environment variables for local and deployment configuration
- use a strong PostgreSQL connection string and credentials in the runtime environment

## 10. Data and persistence

The service uses PostgreSQL through Spring Data JPA.

The main table is `customer_profile` and the entity includes:

- personal identity and contact fields
- status and role enums
- home and other address collections
- timestamps for create/update tracking

## 11. Validation and error-handling policy

The service follows a clear HTTP contract:

- `400 Bad Request` — malformed JSON, empty required values
- `401 Unauthorized` — missing or invalid JWT / credentials
- `403 Forbidden` — account deactivated
- `404`/`409` depending on repository semantics and domain rule enforcement
- `410 Gone` — deleted account lifecycle state

This preserves the distinction between an authentication failure and an authorization or domain-state problem.

## 12. State pattern design

Customer lifecycle logic is centralized in `AuthStates`.

This makes transitions and validation explicit, for example:

```java
AuthStates.Context.from(customer).validateForAuthentication();
CustomerStatus nextStatus = AuthStates.Context.from(customer).deactivate();
```

This design avoids scattering status checks across multiple services and keeps the domain rules consistent.

## 13. Local development and testing

### Run the application

From the module root:

```bash
./mvnw spring-boot:run
```

or on Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

### Run tests

```bash
./mvnw test
```

The project includes tests for:

- controller request behavior
- JWT generation/validation
- customer state rules

## 14. Security and operational recommendations

- rotate JWT signing secrets regularly
- keep the token expiry short and tune for business needs
- do not accept unsigned or unvalidated JWTs from external callers
- ensure database uniqueness rules are enforced for email/phone/username
- treat status transitions as business events, not just enum changes
- keep downstream services enforcing ownership and authorization boundaries independently

## 15. Summary

This service is the authentication and authorization backbone of the platform. It is designed to be simple, secure, and predictable:

- public auth endpoints
- authenticated protected endpoints
- stateless JWT-based authorization
- strong password hashing
- single source of truth for account state
- explicit status-aware behavior

The result is a service that is easy to reason about, easy to extend, and safe to integrate with upstream and downstream microservices.
