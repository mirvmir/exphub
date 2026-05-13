## Stack

- Java 17
- Maven
- Spring MVC / Spring Security
- Hibernate
- PostgreSQL
- Liquibase YAML changelog
- Tomcat 10.1
- Docker / Docker Compose

## Project structure

```text
backend/
├── configuration/              # main web module, builds backend.war
├── activity/                   # activity domain module
├── course/                     # course domain module
├── enrollment/                 # enrollments, bookings, orders
├── payment/                    # payment module
├── identity/                   # users/authentication
├── profile/                    # user profiles
├── catalog/                    # catalog module
├── taxonomy/                   # topics/categories
├── review/                     # reviews
├── media/                      # file/video storage
├── practice/                   # practice tasks
├── *-api/                      # public module contracts/events
├── Dockerfile
├── docker-compose.yml
└── scripts/generate-jwt-keys.sh
```

## Requirements

For Docker run:

- Docker Desktop or Docker Engine
- Docker Compose plugin

For local WAR build:

- JDK 17
- Maven 3.9+
- PostgreSQL 16+ if running without Docker
- Tomcat 10.1 if deploying WAR manually

## Environment variables

The application reads configuration from `configuration/src/main/resources/application.properties`.

| Variable | Default | Description |
|---|---:|---|
| `APP_MODE` | `docker` | Runtime mode. `docker` uses Docker paths and DB URL, `web` uses local paths and DB URL. |
| `DB_DOCKER_URL` | `jdbc:postgresql://db:5432/bookstore_db` | PostgreSQL URL inside Docker network. |
| `DB_WEB_URL` | `jdbc:postgresql://localhost:5433/bookstore_db` | PostgreSQL URL for local/manual run. |
| `DB_USER` | `postgres` | Database user. |
| `DB_PASSWORD` | `psql_password` | Database password. |
| `LIQUIBASE_ENABLED` | `true` | Enables/disables Liquibase migrations on startup. |
| `LIQUIBASE_CHANGE_LOG` | `classpath:db/changelog/db.changelog-master.yaml` | Main Liquibase changelog. |
| `HIBERNATE_HBM2DDL_AUTO` | `validate` | Hibernate schema mode. Keep `validate` when Liquibase is enabled. |
| `APP_ADMIN_EMAIL` | `admin@ya.ru` | Default admin email. |
| `APP_ADMIN_PASSWORD` | `12345` | Default admin password. |
| `JWT_EXPIRATION` | `PT10M` | Access token TTL. |
| `JWT_REFRESH_TOKEN_EXPIRATION` | `P30D` | Refresh token TTL. |
| `BOOKING_EXPIRES_MINUTES` | `15` | Booking expiration in minutes. |
| `LOG_DIR` | `/usr/local/tomcat/logs/app` | Application log directory in Docker. |


## Generate JWT keys

Before running the app, generate RSA keys:

```bash
./scripts/generate-jwt-keys.sh
```

The script creates:

```text
data/secrets/private.pem
data/secrets/public.pem
```

On Windows, run the command from Git Bash or WSL.


## Build WAR locally

From the project root:

```bash
mvn -pl configuration -am clean package -DskipTests
```

Output file:

```text
configuration/target/backend.war
```

To build with tests:

```bash
mvn -pl configuration -am clean package
```

To run only tests:

```bash
mvn test
```

## Run with Docker Compose

1. Generate JWT keys:

```bash
./scripts/generate-jwt-keys.sh
```

2. Build and start PostgreSQL + backend:

```bash
docker compose up --build
```

3. Open application:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

PostgreSQL from host machine:

```text
jdbc:postgresql://localhost:5433/bookstore_db
```

Default database credentials:

```text
database: bookstore_db
user: postgres
password: psql_password
```

## Run without Docker

1. Start PostgreSQL locally or with Docker:

```bash
docker compose up db
```

2. Generate JWT keys:

```bash
./scripts/generate-jwt-keys.sh
```

3. Build WAR:

```bash
mvn -pl configuration -am clean package -DskipTests
```

4. Deploy WAR to Tomcat 10.1:

```text
configuration/target/backend.war
```

Copy it to Tomcat:

```text
<TOMCAT_HOME>/webapps/ROOT.war
```

5. For local/manual Tomcat run, use these environment variables:

```bash
APP_MODE=web
DB_WEB_URL=jdbc:postgresql://localhost:5433/bookstore_db
DB_USER=postgres
DB_PASSWORD=psql_password
LIQUIBASE_ENABLED=true
HIBERNATE_HBM2DDL_AUTO=validate
SECRETS_WEB_PUBLIC_KEY=./data/secrets/public.pem
SECRETS_WEB_PRIVATE_KEY=./data/secrets/private.pem
```
