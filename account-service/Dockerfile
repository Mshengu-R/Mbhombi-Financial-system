# Build stage
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src ./src

RUN chmod +x mvnw && ./mvnw -B clean package -DskipTests

# Run stage
FROM eclipse-temurin:25-jre-alpine AS runtime
WORKDIR /app

COPY --from=build /workspace/target/account-service-0.0.1-SNAPSHOT.jar ./app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
