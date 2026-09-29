# API Gateway

The gateway is a Spring Cloud Gateway edge service. It validates bearer JWTs
using the same Base64 HMAC secret as the authentication service, then forwards
the original `Authorization` header to protected services. Each downstream
service should continue to validate the token independently; gateway security
is not a replacement for service-level authorization.

## Local run

Start Redis, then run:

```text
./mvnw spring-boot:run
```

The gateway listens on `http://localhost:8080` by default. Set `JWT_SECRET` to
the same value as `authentication-service.security.jwt.secret`. Service URI
variables are `AUTH_SERVICE_SERVER`, `ACCOUNT_SERVER`,
`TRANSACTION_SERVER`, `COMMUNITY_ACCOUNT_SERVER`, and `PAYMENT_SERVER`.

Only `/api/v1/auth/login`, `/api/v1/auth/register`, and health/info actuator
endpoints are public. All other gateway exchanges require a valid, unexpired
JWT.

## Docker

Build from this directory with `docker build -t mbhombi-api-gateway .`.
The container expects `REDIS_HOST`, `REDIS_PORT`, `JWT_SECRET`, and the service
URI variables to be supplied through the runtime environment.