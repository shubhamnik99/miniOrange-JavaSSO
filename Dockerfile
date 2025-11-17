# Use Maven with Java 21 for building (matching your pom.xml java.version)
FROM maven:3.9-eclipse-temurin-21 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (creates JAR not WAR since you're using spring-boot-maven-plugin)
RUN mvn clean package -DskipTests

# Use OpenJDK 21 for runtime (matching your build stage)
FROM eclipse-temurin:21-jre

# Set working directory
WORKDIR /app

# Copy the JAR file from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Set environment variables placeholder
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
ENV SPRING_DATASOURCE_USERNAME=${DB_USER}
ENV SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
ENV SERVER_PORT=${PORT:-8080}

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]