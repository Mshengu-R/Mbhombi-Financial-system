# FRAUD DETECTION SERVICE DOCKER

# Build stage
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src ./src

RUN chmod +x mvnw && ./mvnw -B clean package -DskipTests

# Run stage
FROM eclipse-temurin:25-jre-alpine AS runtime
WORKDIR /app

COPY --from=build /app/target/community-account-service-0.0.1-SNAPSHOT.jar ./app.jar

EXPOSE 8083
# ENTRYPOINT ["java", "-jar", "/app/app.jar"] if the one on the bottom does not work we go back to this one
ENTRYPOINT ["java", "-jar", "app.jar"]
