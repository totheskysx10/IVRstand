# Stage 1: Build the application using Maven
FROM maven:3.9.5 AS build

# Устанавливаем рабочую директорию для сборки
WORKDIR /app

# Копируем pom.xml и скачиваем зависимости
COPY pom.xml .
COPY src ./src

# Скачиваем зависимости и собираем JAR файл
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM openjdk:21-jdk

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем собранный JAR файл из стадии сборки
COPY --from=build /app/target/IVRstand-1.0.0-BETA.jar app.jar

# Задаем команду для запуска JAR файла
ENTRYPOINT ["java", "-jar", "app.jar"]