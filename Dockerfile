FROM maven:3.9.8-eclipse-temurin-21-alpine

# Default arguments values
ARG TEST_PROFILE=api
ARG APIBASEURL=http://localhost:4111
ARG UIBASEURL=http://localhost:3000

# Environment variables for container
ENV TEST_PROFILE=${TEST_PROFILE}
ENV APIBASEURL=${APIBASEURL}
ENV UIBASEURL=${UIBASEURL}

# Creating a working directory
WORKDIR /app

# Copying pom.xml file and download dependencies (through GitHub Actions)
COPY pom.xml .

# Install dependencies and cache them
# RUN mvn dependency:go-offline

# Copying the project to working directory
COPY . .

USER root

CMD /bin/bash -c " \
    mkdir -p /app/logs ; \
    { \
    echo '>>> Running tests with profile ${TEST_PROFILE}' ; \
    mvn test -q -P ${TEST_PROFILE} ; \
    \
    echo '>>> Running surefire-report:report' ; \
    mvn -DskipTests=true surefire-report:report ; \
    } > /app/logs/run.log 2>&1"
