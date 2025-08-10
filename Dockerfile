FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
COPY testng.xml .
ENV BASE_URL="https://fakerestapi.azurewebsites.net/api/v1"
CMD ["mvn", "test", "-Dsurefire.suiteXmlFiles=testng.xml"]
