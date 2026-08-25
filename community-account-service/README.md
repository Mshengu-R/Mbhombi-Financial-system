# Community Account Service

This service provides community-based banking features for the Mbhombi Financial Services platform.

## Features
- Create community accounts
- Choose account type (STOKVEL or OTHER)
- Create communities with a creator-supplied 6-digit join code
- Join and leave communities
- Store community rules and payout description
- Transfer funds into the community account using the generated account number
- Allow members to claim funds from the shared pool when they are eligible
- Notify other active members through the existing Kafka notification configuration when funds are claimed
- Allow the creator to update the community description and rules

## Main API flow

1. Create an account with `POST /api/v1/community-accounts/community/{creatorCustomerId}`.
	The request must contain `customerId`, `displayName`, and a six-digit `joinCode`.
2. Search an account with `GET /api/v1/community-accounts/{accountNumber}`.
3. Join with `POST /api/v1/community-accounts/{accountNumber}/{customerId}/join`, passing the creator's `joinCode` as a request parameter and the matching community details in the body.
4. Claim funds with `PUT /api/v1/community-accounts/{accountNumber}/claim?customerId={customerId}&amount={amount}`.
	The response contains the claimed amount and remaining balance; other active members receive a Kafka notification.
5. Update the description with `PUT /api/v1/community-accounts/{accountNumber}/description?customerId={creatorCustomerId}` and a body containing `description` and optional `rulesDescription`.

## Run with Maven
Use Command Prompt:

```cmd
cd C:\Users\Dell\OneDrive\Documentos\Mbhombi-Financial-Services\Mbhombi-Financial-System\community-account-service
mvnw.cmd spring-boot:run
```

## Build
```cmd
mvnw.cmd clean package
```

## Docker
```cmd
docker build -t community-account-service .
docker run -p 8087:8087 community-account-service
```
