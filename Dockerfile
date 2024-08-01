# Stage 1: Build the Java application using Maven
FROM maven:3.9.5 AS build-java

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM openjdk:21-jdk

WORKDIR /app

COPY --from=build-java /app/target/IVRstand-1.0.0-BETA.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
