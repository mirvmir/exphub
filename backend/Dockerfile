# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY . .
RUN mvn -pl configuration -am clean package -DskipTests

FROM tomcat:10.1-jdk17-temurin

ENV APP_MODE=docker \
    DB_DOCKER_URL=jdbc:postgresql://db:5432/bookstore_db \
    DB_USER=postgres \
    DB_PASSWORD=psql_password \
    LIQUIBASE_ENABLED=true \
    HIBERNATE_HBM2DDL_AUTO=validate \
    LOG_DIR=/usr/local/tomcat/logs/app

RUN rm -rf /usr/local/tomcat/webapps/* \
    && mkdir -p /usr/local/tomcat/storage/uploads /usr/local/tomcat/logs/app

COPY --from=build /workspace/configuration/target/backend.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]
