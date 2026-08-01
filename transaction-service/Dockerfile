# Multi-stage Dockerfile for building and running the Spring Boot application
# Build stage
FROM maven:3.9.4-jdk-25 AS build
WORKDIR /workspace
COPY pom.xml mvnw mvnw.cmd .mvn/ ./
COPY src ./src
# Use Maven to build the application and produce an executable jar
RUN mvn -B -DskipTests package

# Run stage - use a small JRE image compatible with the target Java version
FROM eclipse-temurin:25-jre-jammy
ARG JAR_FILE=target/*.jar
COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-Xms256m","-Xmx512m","-jar","/app.jar"]
