# Stage 1: build
FROM maven:3.9.0-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: runtime
FROM tomcat:10.1-jdk17-temurin
LABEL maintainer="kivislime.org"
COPY --from=build /app/target/weatherApp-1.0-SNAPSHOT.war \
     /usr/local/tomcat/webapps/ROOT.war