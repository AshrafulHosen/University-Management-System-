# Use official lightweight Eclipse Temurin JRE on Alpine Linux
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the pre-built executable JAR into the container
COPY target/*.jar app.jar

# Expose application port
EXPOSE 8081

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
