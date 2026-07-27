# Mbhombi-FINANCIAL SERVICES

THIS IS A BANKING APPLICATION FOR PEOPLE WHO NEED TO CREATE A STOKVEL ACCOUNT
## ------------- SYSTEM -------------- ##
THE ACCOUNT MINIMUM SHOULD BE 3 PEOPLE, AND MAXIMUM SHOULD BE 50
- CUSTOMERS ARE ABLE TO CREATE ACCOUNT FOR OTHER BANKING PURPOSES THEN THEY CAN CREATE ACCOUNT FOR COMMUNITY PURPOSES
- COMMUNITY ACCOUNTS CAN BE PRIVATE OR PUBLIC DEPENDING ON THE USER PREFERENCES
- ACCOUNT NUMBERS SHOUL BE UNIQUE AND CONTAIN 12 DIGITS
- THERE SHOULD AT LEAST BE ONE ADMIN AUTOMATICALLY THE FIRST PERSON TO CREATE THE AACOUNT
- DEDUCTING THE BALANCE FROM THE SENDER WILL BE CALLED BY TRANSACTION SERVICES via kafka
- CREDIT BALANCE WILL ALSO BE CALLED BY TRANSACTION SERVICES via kafka
- THE EVENT CONSUMER SHOULD CONSUME TRANSACTIONS
- USER ACTIVITIES SHOULD ALSO BE LIMITTED BY ROLES
- IN COMMUNITY SERVICES USERS CAN CLAIM THE MONEY BUT(IN THE FUTURE I SHALL IMPLIMENT THE AUTOMATED CLAIMS FOR USERS)
- if fraud is detected then the flagged accounts should be blocked temporarily
- FOR EVERY ACTIVITIES USERS MUST BE NOTIFIED FOR CONFIRMATION OR WARN THEM ABOUT FRAUD
- OR IN OTHER WORDS THE BANK MUST CONFIRM WITH THE USER TO CHECK IF THE USER IS THE ONE BEHIND THE TRANSACTIONS
<!-- - CUSTOMERS SHOULD BE WARNED ABOUT GROUPS BEFORE JOINING THAT PEOPLE ARE ALLOWED TO CLAIM -->
- PayFast

# Pay

## ARCHITECTURE
- THE SERVICE SHOULD BE DIVIDED INTO MICROSERVICES
WHERE THERE WOULD BE SERCIVES SUCH AS

# THERE SHOULD ALSO BE LOGGING FOR MONITORING
- Use java built in methods for logging

# BACKEND
- ACCOUNT SERVICE
- CUSTOMER SERVICE (account-service) for now
- DATABASE SERVICE
- NOTIFICATION SERVICE
- PAYMENT SERVICE
- FRAUD DETECTION SERVICE

AND THERE WILL ALSO BE FRONTEND

# LANGUAGES AND FRAMEWORKS
NESTJS FOR ITS SCALABILITY AND SIMPLICITY OF MICROSERVICE ARCHITECTURE
- YML FOR FOR SOAP API TO ENFORCE STRICT TRANSACTIONS
- AI (CODEX AND COPILOT)
- JAVA and JAVASCRIPT but mainly java because of its heavily use in enterprises and banking
- Object oriented programming
- Event streaming/event oriented programming

# CONTAINER
DOCKER FOR CONTAINERIZING

# DATABASE
- postgres
- for its acidity and relationships
- Then noSQL database for loging and fraud preventions

# VERSION CONTROL
- Git 

# CLoud
??

## FRAUD DETECTION
- LISTEN TO INITIATED TRANSACTIONS
  - CHECK IF THERE IS ANY FRAUD INIT
  - IF NON THEN ALLOW THE TRANSACTION TO BE COMPLETE
  - BASICALLY THE ONE TO ALLOW THE TRANSACTIONS TO BE COMPLETE
- THE BANK WILL SEND THE OTP VIA SMS FOR THE USER TO CONFIRM THEIR INTENTIONS (They shoulf be warned about giving it to strangers)
- Checks with the velocity, unusual amounts, and balance comparision
- 

## API GATEWAY 
- ROUND ROBIN, AND IP ALGORITHIM FOR OUR SERVERS BECAUSE THE GEOGRAPHY ONE IS NOT REALLY NEEDED SINCE THE APPLICATION WORKS FOR PEOPLE BASED IN SOUTH AFRICA SO FAR
- REDIS FOR RATE LIMITING
- OAUTH2, JWT AUTHENTICATION AND AUTHORIZATION
- JWT WILL HANDLE MORE OF THOSE ONES OF ENTERING THE SERVICES

## HEART (TRANSACTION SERVICE)
- ONE TIME PASSOWRD (OTP)
- THE FINAL SERVICE AFTER TRANSFER SERVICES

# Authentications and authorization
- JSON WEB TOKEN

## ALL USERS WILL CREATE AN ACOUNT THEN REQUEST TO JOIN A GROUP OR CREATE A GROUP
- WHEN CREATED A GROUP THEN THEY WILL GET AM ACCOUNT NUMBER WHICH WILL BE USED FOR TRANSACTIONS
- WHEN JOINING A GROUP THEY WONT CREATE AN ACCOUNT NUMBER BUT REGISTER ONE WHICH THEY WOULD LIKE TO RECIVE THE PAYMENTS TO
- ADMINS CAN CHOOSE WHETHER THEY MAKE THEIR GROUPS PRIVATE OR PUBLIC 
- IF PUBLIC THEN ANYBODY CAN SEE IT ON THE DASHBOARD THEN IF THE GROUP IS PRIVATE, IT MUST BE SEARCHED

## SAGA 
# STEP 1 - Deduct Balance
- INITIATE TRANSFER VIA FEIGN
- SAVES TRANSACTION AS PROCCESSING
- PUSH IT TO KAFKA FOR FRAUD CHECKING
- RETURNS
- CALLED BY TRANSACTION SERVICE WHEN TRANSFER IS INITIATED
# STEP 2 - Compesating transaction endpoint
- CALLED BY TRANSACTION SERVICE IN TWO SCENARIOS
1 - FRAUD DETECTION => REFUND SENDER
2 - TRANSACTION COMPLETED => CREDIT RECIVER

# DATA BINDING
- Playload

## TRANSACTION LIFE CYCLE
- PENDING 
   - VERIFICATION (suspicious activities detected)
- PROCCESSING
- COMPLETED 
   -  VERIFICATION 
       - verified
       - IF UNVERIFIED 
         - FLAGGED AND BLOCKED( saga refund)
            - FAILED
(clean transaction) 

## API
# FOR AUTHENTICATING AND AUTHORIZING USERS
# SAGA

- POST api/v1/accounts/sign-up -> FOR CREATING AN ACCOUNT FOR BANKING 
- POST api/v1/accounts/sign-in -> FOR SIGNING IN AFTER REGISTERING
- POST api/v1/accounts/sign-out -> FOR SIGNING OUT

- GET api/v1/accounts/:id -> for getting all the users in a specific group
- GET api/v1/accounts/getBalance/:id
- PUT api/v1/accounts/block/:id
- GET api/v1/accounts/deductBal/:id




### Repository interface (e.g., `AccountRepository extends JpaRepository`)
**Purpose:** provide a clean, declarative data‑access layer so you don’t write boilerplate CRUD code. Spring Data generates the implementation at runtime (or at build/AOT time) and gives you ready‑made methods for common operations.

---

### What it gives you
- **CRUD methods out of the box** — `save`, `findById`, `findAll`, `deleteById`, etc.  
- **Paging and sorting** support via `Pageable` and `Sort`.  
- **Derived query methods** by method name (e.g., `findByEmail(String email)`) — Spring builds the SQL/JPA query for you.  
- **Custom queries** with `@Query` for JPQL or native SQL when you need control.  
- **Transaction support** integrated with Spring’s transaction manager.  
- **Optional AOT/native support**: Spring can generate concrete classes ahead of time for faster startup or native images.

---

### Typical file and location
- **File name:** `AccountRepository.java`  
- **Package:** usually under `src/main/java/com/yourorg/repository` or `.../repo` 

---

### How it works at runtime
- Spring creates a **proxy/implementation** for the interface when the application context starts.  
- Your service classes inject the repository (constructor injection recommended) and call methods — no manual implementation required.


---

### When to add custom behavior
- **Custom queries:** use `@Query` on repository methods.  
- **Complex logic:** create a separate custom implementation (e.g., `AccountRepositoryCustom` + `AccountRepositoryImpl`) and wire it to the interface.  
- **Batch or performance operations:** consider `EntityManager` or `JdbcTemplate` for fine control.

---

### Testing tips
- Use **@DataJpaTest** for repository tests with an in‑memory DB (H2).  
- Mock repositories in service tests with Mockito when you don’t need DB integration.

---

### Common pitfalls
- Method name typos in derived queries cause runtime errors — run tests.  
- Mismatched entity id type (e.g., `Long` vs `Integer`) causes compile or runtime issues.  
- Relying on runtime proxies can complicate AOT/native builds unless you enable Spring’s AOT support.

---

If you want, I can generate a small example `Account` entity, `AccountRepository`, and a `@DataJpaTest` unit test you can paste into your project.