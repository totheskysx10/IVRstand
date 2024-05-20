FROM openjdk:22-jdk
ARG JAR_FILE=target/IVRstand-1.0.0-BETA.jar
WORKDIR /app
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","app.jar"]