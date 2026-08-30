# Authentication service

This service owns customer credentials, password authentication, JWT access tokens, and customer consent records.

## Local configuration

Copy `.env` values into the process environment or run from this directory so Spring can load the optional `.env` file. Replace `AUTH_SECRET_KEY` with a randomly generated Base64 key of at least 256 bits before using the service outside local development. Never commit real database passwords or signing keys.

Required settings:

- `AUTH_DB_URL`, `AUTH_DB_USERNAME`, `AUTH_DB_PASSWORD`
- `AUTH_SECRET_KEY`
- `AUTH_EXPIRATION_TIME`, for example `15m`
- `AUTH_SERVICE_SERVER`, for example `8089`

## Endpoints

- `POST /api/v1/auth/register`: creates a customer and BCrypt-hashes the supplied password.
- `POST /api/v1/auth/login`: authenticates username/password and returns a signed JWT string.
- `POST /api/v1/auth/consents`: records the authenticated customer's consent to a document version. Supported types include `POPIA`, `TERMS_AND_CONDITIONS`, `PRIVACY_POLICY`, and `MARKETING`.
- `POST /api/v1/auth/logout`: returns a compatibility response. JWTs are stateless; token revocation requires a future deny-list or refresh-token design.

JWTs use the username as `sub` and include `customerId` and `role` claims. Other services should use `customerId` as a string UUID and still enforce ownership at their own authorization boundary.

OAuth2/OIDC can be added later by introducing an authorization server and refresh-token policy without changing the customer ID contract.