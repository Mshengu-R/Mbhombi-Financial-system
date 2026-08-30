# Account Service

**Version:** 0.0.1-SNAPSHOT  
**Framework:** Spring Boot 4.1.0  
**Java Version:** 25  
**Database:** PostgreSQL

## Overview

The Account Service is the core banking microservice responsible for account lifecycle management within the Mbhombi Financial Services platform. It manages personal banking accounts with support for multiple account types (SAVINGS, CHECKING, INVESTMENT) and handles critical operations like account creation, balance management, and account state transitions.

## Architecture & Design

### Service Responsibilities

- **Account Lifecycle Management**: Create, retrieve, and manage individual customer accounts
- **Balance Tracking**: Maintain accurate account balances with transaction-level precision
- **Account Status Management**: Handle account states (ACTIVE, FROZEN, BLOCKED, CLOSED)
- **Account Number Generation**: Generate unique 12-digit account numbers with validation
- **Transaction Limit Enforcement**: Enforce daily/monthly transaction limits by account type
- **Event Consumption**: Listen to Kafka events for balance updates and state transitions
- **Compensating Transactions**: Handle refunds for fraudulent transactions via saga pattern

### Account Types

| Type | Daily Limit | Use Case |
|------|------------|----------|
| SAVINGS | ZAR 100,000 | Personal savings accounts |
| CHECKING | ZAR 500,000 | Daily transaction accounts |
| INVESTMENT | ZAR 500,000 | Investment-focused accounts |

### Design Patterns

- **Service-Oriented Architecture**: Independent deployment, own database
- **Saga Pattern**: Distributed transactions with compensating actions
- **Event-Driven**: Kafka-based async communication with other services
- **Repository Pattern**: Clean data access abstraction with Spring Data JPA

## API Endpoints

### Create Account
```http
POST /api/v1/accounts
Content-Type: application/json

{
  "customerId": "uuid-string",
  "accountHolderName": "John Doe",
  "phone": "+27791234567",
  "accountType": "SAVINGS"
}
```

**Response (201 Created):**
```json
{
  "accountId": "uuid",
  "accountNumber": "123456789012",
  "accountHolderName": "John Doe",
  "phone": "+27791234567",
  "accountType": "SAVINGS",
  "currency": "ZAR",
  "balance": "0.00",
  "bankName": "Mbhombi Bank",
  "status": "ACTIVE",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

### Get Account by Account Number
```http
GET /api/v1/accounts/{accountNumber}
```

**Response (200 OK):**
```json
{
  "accountId": "uuid",
  "accountNumber": "123456789012",
  "accountHolderName": "John Doe",
  "balance": "5000.00",
  "accountType": "SAVINGS",
  "status": "ACTIVE",
  "currency": "ZAR"
}
```

### Debit Account (Saga Step 1)
```http
POST /api/v1/accounts/{accountNumber}/debit
Content-Type: application/json

{
  "amount": "500.00",
  "transactionId": "uuid",
  "transactionType": "TRANSFER"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "newBalance": "4500.00",
  "transactionId": "uuid"
}
```

### Credit Account
```http
POST /api/v1/accounts/{accountNumber}/credit
Content-Type: application/json

{
  "amount": "1000.00",
  "transactionId": "uuid",
  "transactionType": "TRANSFER"
}
```

### Compensating Transaction (Refund)
```http
POST /api/v1/accounts/{accountNumber}/compensate
Content-Type: application/json

{
  "amount": "500.00",
  "reason": "FRAUD_DETECTED",
  "originalTransactionId": "uuid"
}
```

### Health Check
```http
GET /actuator/health
```

**Response (200 OK):**
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "kafkaProducer": {"status": "UP"},
    "kafkaConsumer": {"status": "UP"}
  }
}
```

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/accountdb` | PostgreSQL connection URL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | - | Database password (required) |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update` | Hibernate DDL strategy |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker address |
| `SPRING_KAFKA_PRODUCER_VALUE_SERIALIZER` | `org.springframework.kafka.support.serializer.JsonSerializer` | Event serialization |
| `SERVER_PORT` | `8082` | Service port |
| `LOGGING_LEVEL_ROOT` | `INFO` | Logging level |

### application.yml
```yaml
spring:
  application:
    name: account-service
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      group-id: account-service-group

server:
  port: 8082
  
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics

logging:
  level:
    com.banking.accountservice: DEBUG
```

## Running the Service

### Prerequisites

- Java 25
- Maven 3.8+
- PostgreSQL 12+
- Apache Kafka 3.0+
- Redis (for caching/distributed sessions)

### Local Development

#### 1. Start PostgreSQL
```bash
# Using Docker
docker run --name accountdb -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=accountdb -p 5432:5432 -d postgres:latest

# Create database
psql -U postgres -c "CREATE DATABASE accountdb;"
```

#### 2. Start Kafka
```bash
# Using Docker Compose (from root directory)
docker-compose up -d kafka zookeeper
```

#### 3. Run the Service
```bash
cd account-service

# Using Maven wrapper
mvnw.cmd spring-boot:run

# Or
mvnw.cmd clean package
java -jar target/account-service-0.0.1-SNAPSHOT.jar
```

The service starts on `http://localhost:8082`

### Docker Deployment

#### Build Docker Image
```bash
cd account-service
docker build -t account-service:latest .
```

#### Run with Docker
```bash
docker run -d \
  --name account-service \
  -p 8082:8082 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/accountdb \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  --network mbhombi-network \
  account-service:latest
```

#### Docker Compose (from root)
```bash
docker-compose up account-service
```

## Event Topics (Kafka)

### Published Events

| Topic | Event Type | Payload |
|-------|-----------|---------|
| `transaction.initiated` | Transaction started | `{accountNumber, amount, transactionId, timestamp}` |
| `account.debit.requested` | Debit requested | `{accountNumber, amount, transactionId}` |
| `account.credit.requested` | Credit requested | `{accountNumber, amount, transactionId}` |
| `account.status.changed` | Account status updated | `{accountNumber, oldStatus, newStatus}` |

### Consumed Events

| Topic | Consumed For |
|-------|--------------|
| `fraud.check.clean` | Credit balance after fraud check passes |
| `transaction.refund.required` | Compensating transaction (fraud detected) |
| `account.block.requested` | Block account for fraud prevention |

## Database Schema

### Accounts Table
```sql
CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    account_number VARCHAR(12) UNIQUE NOT NULL,
    account_holder_name VARCHAR(255) NOT NULL,
    account_type VARCHAR(50) NOT NULL,
    currency VARCHAR(3) DEFAULT 'ZAR',
    balance DECIMAL(19, 2) DEFAULT 0.00,
    bank_name VARCHAR(255) DEFAULT 'Mbhombi Bank',
    phone VARCHAR(20) NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    daily_transaction_limit DECIMAL(19, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE INDEX idx_account_number ON accounts(account_number);
CREATE INDEX idx_customer_id ON accounts(customer_id);
CREATE INDEX idx_status ON accounts(status);
```

### Account History/Audit Table
```sql
CREATE TABLE account_transactions (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES accounts(id),
    transaction_id UUID NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    previous_balance DECIMAL(19, 2),
    new_balance DECIMAL(19, 2),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(id)
);

CREATE INDEX idx_account_transactions ON account_transactions(account_id);
CREATE INDEX idx_transaction_id ON account_transactions(transaction_id);
```

## Key Features

### 1. Account Number Generation
- Generates unique 12-digit account numbers
- Format: Prefix (2 digits) + Random (10 digits)
- Validates uniqueness before assignment

### 2. Balance Management
- Atomic balance updates using database transactions
- Precision: 2 decimal places (cents)
- Real-time balance calculation

### 3. Transaction Limits
- **SAVINGS**: ZAR 100,000 daily limit
- **CHECKING**: ZAR 500,000 daily limit
- **INVESTMENT**: ZAR 500,000 daily limit
- Verified at transaction initiation

### 4. Account Status Lifecycle
```
ACTIVE → FROZEN → ACTIVE
ACTIVE → BLOCKED (Fraud detected)
ACTIVE → CLOSED (Customer request)
```

### 5. Saga Pattern Integration
- Participates as saga participant in distributed transactions
- Supports compensating transactions for rollback scenarios
- Publishes state changes for downstream services

## Testing

### Unit Tests
```bash
mvnw.cmd test
```

### Integration Tests
```bash
mvnw.cmd verify
```

### Load Testing (Example with JMeter)
```bash
# Run load test
jmeter -n -t load-test-plan.jmx -l results.jtl -j logs.log
```

## Troubleshooting

### Issue: Account Creation Fails with "Account already exists"
**Solution:** Check if account number is already in database. Regenerate unique number.

### Issue: Balance Debit Exceeds Daily Limit
**Solution:** Verify transaction amount against account type's daily limit. Implement request splitting.

### Issue: Kafka Events Not Consumed
**Solution:** 
1. Verify Kafka is running: `kafka-topics.sh --list --bootstrap-server localhost:9092`
2. Check consumer group: `kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list`
3. Review application logs for consumer errors

### Issue: PostgreSQL Connection Timeout
**Solution:**
1. Verify PostgreSQL is running
2. Check credentials in `application.yml`
3. Ensure database exists: `psql -l`
4. Check connection pool settings

## Performance Optimization

### Caching Strategy
- Cache account details for 5 minutes using Redis
- Cache exchange rates for 1 hour
- Invalidate on balance changes

### Database Optimization
- Index on `account_number` for fast lookups
- Index on `customer_id` for customer queries
- Batch credit/debit operations where possible

### Kafka Optimization
- Partition by `customerId` for ordering
- Consumer concurrency: 3 threads
- Batch size: 100 messages

## Security Considerations

1. **Authentication**: Validate JWT token in Authorization header
2. **Authorization**: Ensure customer can only access their own accounts
3. **Data Validation**: Sanitize and validate all inputs
4. **Encryption**: Store sensitive data encrypted at rest
5. **Audit Logging**: Log all account modifications

## Monitoring & Observability

### Metrics Exposed
- `account.creation.total` - Total accounts created
- `account.balance.total` - Total balance by account type
- `transaction.processing.time` - P95 transaction processing time
- `debit.failures.total` - Failed debit operations

### Health Checks
```bash
# Basic health
curl http://localhost:8082/actuator/health

# Detailed metrics
curl http://localhost:8082/actuator/metrics

# Database connectivity
curl http://localhost:8082/actuator/health/db

# Kafka connectivity
curl http://localhost:8082/actuator/health/kafkaProducer
```

## Deployment Checklist

- [ ] Database migration scripts tested
- [ ] Environment variables configured
- [ ] Kafka topics created
- [ ] Redis cache configured
- [ ] SSL/TLS certificates deployed
- [ ] Load balancer health checks configured
- [ ] Monitoring alerts set up
- [ ] Backup and recovery procedures documented

## Related Services

- **Authentication Service** - Validates JWT tokens
- **Transaction Service** - Initiates debit/credit operations
- **Fraud Detection Service** - Monitors for suspicious activity
- **Notification Service** - Sends alerts for account activities
- **API Gateway** - Routes and rate limits requests

## Support & Documentation

For more information:
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

## License

Proprietary - Mbhombi Financial Services
