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

# Copy JMX exporter to local app directory
ADD https://repo.maven.apache.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/1.0.1/jmx_prometheus_javaagent-1.0.1.jar /app/jmx_prometheus_javaagent.jar
COPY jmx-config.yml /app/jmx-config.yml

ENV JAVA_OPTS="\
    -Dcom.sun.management.jmxremote \
    -Dcom.sun.management.jmxremote.port=9010 \
    -Dcom.sun.management.jmxremote.rmi.port=9010 \
    -Dcom.sun.management.jmxremote.authenticate=false \
    -Dcom.sun.management.jmxremote.ssl=false \
    -Djava.rmi.server.hostname=${HOST_IP:-0.0.0.0} \
    -Dcom.sun.management.jmxremote.local.only=false \
    -Djava.net.preferIPv4Stack=true \
    -Dcom.sun.management.jmxremote.autodiscovery=true \
    -javaagent:/app/jmx_prometheus_javaagent.jar=9404:/app/jmx-config.yml \
    -Xms1g \
    -Xmx4g"

EXPOSE 18000
EXPOSE 9010
EXPOSE 9404
EXPOSE 1099

# Set the entry point to run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]