# Stage 1: Build the application
FROM maven:3.8.5-openjdk-17 AS build

# Set the working directory in the container
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml ./
COPY src ./src

# Copy the .env file
COPY .env .

# Package the application (build the JAR file)
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the jar file from the build stage
COPY --from=build /app/target/*.jar app.jar

# **Copy the keystore.jks file into the runtime image**
COPY . .

## Expose the application port
#EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
