# Use Maven with OpenJDK 17 for building
FROM maven:3.9-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Use Tomcat with JRE 17 for runtime
FROM tomcat:10.1-jre17-temurin

# Remove default Tomcat applications
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the WAR file from build stage
COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

# Expose port (Render uses PORT environment variable)
EXPOSE 8080

# Set environment variables for database connection
ENV CATALINA_OPTS="-Dspring.datasource.url=${DATABASE_URL} \
    -Dserver.port=${PORT:-8080}"

# Start Tomcat
CMD ["catalina.sh", "run"]