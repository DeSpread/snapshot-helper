# Use the OpenJDK 21 slim image as the base for the build stage
FROM openjdk:21-jdk-slim AS build

# Set the working directory to /app
WORKDIR /app

# Create a volume for Gradle dependencies
VOLUME /root/.gradle

# Copy the Gradle wrapper and build configuration files to the container
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Run Gradle to download dependencies
RUN ./gradlew dependencies

# Copy the entire project into the container
COPY . .

# Build the project, skipping tests
RUN ./gradlew build -x test

# Use the OpenJDK 21 slim image as the base for the final stage
FROM openjdk:21-slim

# Update package list and install necessary packages, then clean up
RUN apt-get update && apt-get install -y --no-install-recommends \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* \
    && rm -rf $JAVA_HOME/lib/src.zip \
    && rm -rf $JAVA_HOME/lib/*.diz

# Set the working directory to /app
WORKDIR /app

# Copy the built jar file from the build stage to the final stage
COPY --from=build /app/build/libs/*.jar app.jar
COPY .env .

# Set the entry point to run the application
ENTRYPOINT ["java", "-Xms1g", "-Xmx4g", "-jar", "app.jar"]