# Community Account Service

This service provides community-based banking features for the Mbhombi Financial Services platform.

## Features
- Create community accounts
- Choose account type (STOKVEL or OTHER)
- Create private communities with a 7-digit join code
- Join and leave communities
- Store community rules and payout description
- Transfer funds into the community account using the generated account number
- Allow members to claim funds from the shared pool when they are eligible

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
docker run -p 8080:8080 community-account-service
```
