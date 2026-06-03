## Stack

* Java 17
* Maven
* Spring MVC / Spring Security
* Hibernate
* PostgreSQL
* Liquibase YAML changelog
* Tomcat 10.1
* Docker / Docker Compose

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

* Docker Desktop or Docker Engine
* Docker Compose plugin

For local WAR build:

* JDK 17
* Maven 3.9+
* PostgreSQL 16+ if running without Docker
* Tomcat 10.1 if deploying WAR manually

## Environment variables

The application reads configuration from `configuration/src/main/resources/application.properties`.

| Variable                                       |                                                          Default | Description                                                                             |
| ---------------------------------------------- | ---------------------------------------------------------------: | --------------------------------------------------------------------------------------- |
| `APP_MODE`                                     |                                                            `web` | Runtime mode. `docker` uses Docker paths and DB URL, `web` uses local paths and DB URL. |
| `DB_WEB_URL`                                   |                        `jdbc:postgresql://localhost:5433/exphub` | PostgreSQL URL for local/manual run.                                                    |
| `DB_DOCKER_URL`                                |                               `jdbc:postgresql://db:5432/exphub` | PostgreSQL URL inside Docker network.                                                   |
| `DB_USER`                                      |                                                       `postgres` | Database user.                                                                          |
| `DB_PASSWORD`                                  |                                                  `psql_password` | Database password.                                                                      |
| `HIBERNATE_HBM2DDL_AUTO`                       |                                                       `validate` | Hibernate schema mode. Keep `validate` when Liquibase is enabled.                       |
| `BANK_BASE_URL`                                |                                          `http://localhost:8000` | Internal base URL of the banking/payment system.                                        |
| `BANK_PUBLIC_BASE_URL`                         |                                          `http://localhost:8080` | Public base URL used for payment redirects/callbacks.                                   |
| `LIQUIBASE_ENABLED`                            |                                                           `true` | Enables/disables Liquibase migrations on startup.                                       |
| `LIQUIBASE_CHANGE_LOG`                         |                `classpath:db/changelog/db.changelog-master.yaml` | Main Liquibase changelog.                                                               |
| `JWT_EXPIRATION`                               |                                                          `PT10M` | Access token TTL.                                                                       |
| `JWT_REFRESH_TOKEN_EXPIRATION`                 |                                                           `P30D` | Refresh token TTL.                                                                      |
| `JWT_REFRESH_TOKEN_CLEANUP_BATCH_SIZE`         |                                                           `1000` | Batch size for refresh token cleanup.                                                   |
| `JWT_REFRESH_TOKEN_CLEANUP_MAX_BATCHES`        |                                                             `20` | Maximum number of batches for refresh token cleanup.                                    |
| `ACTIVITY_CANCELLATION_MIN_HOURS_BEFORE_START` |                                                             `24` | Minimum number of hours before activity start when cancellation is allowed.             |
| `BOOKING_EXPIRES_MINUTES`                      |                                                             `15` | Booking expiration in minutes.                                                          |
| `MEDIA_DOCKER_STORAGE_ROOT_PATH`               |                                            `run/storage/uploads` | Media upload storage path in Docker mode.                                               |
| `MEDIA_WEB_STORAGE_ROOT_PATH`                  |          `C:/Users/user/Projects/exphub/backend/storage/uploads` | Media upload storage path in local/manual mode.                                         |
| `MEDIA_FILES_STORAGE_MAX_SIZE_BYTES`           |                                                       `10485760` | Maximum uploaded file size in bytes.                                                    |
| `MEDIA_VIDEOS_STORAGE_MAX_SIZE_BYTES`          |                                                      `524288000` | Maximum uploaded video size in bytes.                                                   |
| `MEDIA_VIDEOS_DEFAULT_CHUNK_SIZE_BYTES`        |                                                        `1048576` | Default video chunk size in bytes.                                                      |
| `MEDIA_PLAYBACK_TOKEN_SECRET`                  |                                                     `secret-key` | Secret key for media playback tokens.                                                   |
| `MEDIA_PLAYBACK_TOKEN_TTL_SECONDS`             |                                                            `360` | Media playback token TTL in seconds.                                                    |
| `SECRETS_DOCKER_PUBLIC_KEY`                    |                                    `/run/secrets/jwt_public_key` | JWT public key path in Docker mode.                                                     |
| `SECRETS_DOCKER_PRIVATE_KEY`                   |                                   `/run/secrets/jwt_private_key` | JWT private key path in Docker mode.                                                    |
| `SECRETS_WEB_PUBLIC_KEY`                       |  `C:/Users/user/Projects/exphub/backend/data/secrets/public.pem` | JWT public key path in local/manual mode.                                               |
| `SECRETS_WEB_PRIVATE_KEY`                      | `C:/Users/user/Projects/exphub/backend/data/secrets/private.pem` | JWT private key path in local/manual mode.                                              |

The following application properties are also defined directly in `application.properties` and normally do not need to be changed through environment variables:

```properties
hibernate.connection.driver_class=org.postgresql.Driver
hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

Some variables are also set directly in the `Dockerfile` for Docker runtime:

```dockerfile
ENV APP_MODE=docker \
    DB_DOCKER_URL=jdbc:postgresql://db:5432/exphub \
    DB_USER=postgres \
    DB_PASSWORD=password \
    LIQUIBASE_ENABLED=true \
    HIBERNATE_HBM2DDL_AUTO=validate \
    LOG_DIR=/usr/local/tomcat/logs/app
```

These values are used when the application is built and started inside the Docker container. If the same variable is also passed through `docker-compose.yml` or the container environment, the final value is determined by the Docker Compose/container configuration.

## Docker Compose configuration

The Docker Compose file is located in the `./backend` directory:

```text
backend/docker-compose.yml
```

If necessary, update this file before running the application.

Current Docker Compose parameters:

| Parameter                          | Current value                       | Description                                                   |
| ---------------------------------- | ----------------------------------- | ------------------------------------------------------------- |
| `POSTGRES_DB`                      | `exphub`                            | Database name created in PostgreSQL.                          |
| `POSTGRES_USER`                    | `postgres`                          | PostgreSQL user.                                              |
| `POSTGRES_PASSWORD`                | `password`                          | PostgreSQL password inside Docker.                            |
| `ports: "5433:5432"` for `db`      | `5433` on host, `5432` in container | PostgreSQL port mapping.                                      |
| `APP_MODE`                         | `docker`                            | Runtime mode for backend in Docker.                           |
| `BANK_BASE_URL`                    | `http://exphub-mock-bank:8000`      | Internal URL of the banking/payment system in Docker network. |
| `BANK_PUBLIC_BASE_URL`             | `http://exphub-backend:8080`        | Public/backend URL used for payment redirects or callbacks.   |
| `DB_DOCKER_URL`                    | `jdbc:postgresql://db:5432/exphub`  | PostgreSQL URL used by backend inside Docker network.         |
| `DB_USER`                          | `postgres`                          | Database user used by backend.                                |
| `DB_PASSWORD`                      | `password`                          | Database password used by backend in Docker.                  |
| `LIQUIBASE_ENABLED`                | `true`                              | Enables Liquibase migrations on startup.                      |
| `HIBERNATE_HBM2DDL_AUTO`           | `validate`                          | Hibernate schema validation mode.                             |
| `LOG_DIR`                          | `/usr/local/tomcat/logs/app`        | Application log directory inside the container.               |
| `ports: "8080:8080"` for `backend` | `8080` on host, `8080` in container | Backend port mapping.                                         |
| `./storage/uploads`                | `/usr/local/tomcat/storage/uploads` | Local directory for uploaded files.                           |
| `./logs`                           | `/usr/local/tomcat/logs/app`        | Local directory for application logs.                         |
| `./data/secrets/public.pem`        | Docker secret `jwt_public_key`      | JWT public key.                                               |
| `./data/secrets/private.pem`       | Docker secret `jwt_private_key`     | JWT private key.                                              |

Before running Docker Compose, make sure that JWT keys are already generated in:

```text
backend/data/secrets/
```

## Important note about startup directory

All commands for building, running Docker Compose and generating JWT keys must be executed from the `./backend` directory.

Before running the commands, go to the backend folder:

```bash
cd ./backend
```

## Generate JWT keys

Before running the app, generate RSA keys.

The key generation script must be executed from the `./backend` directory, because it creates files by relative path:

```text
data/secrets/private.pem
data/secrets/public.pem
```

Go to the backend directory:

```bash
cd ./backend
```

Then run the script:

```bash
./scripts/generate-jwt-keys.sh
```

The script creates the `data/secrets` directory if it does not exist and generates two files:

```text
data/secrets/private.pem
data/secrets/public.pem
```

On Windows, run the command from Git Bash or WSL.

If the script is executed from another directory, the keys may be created in the wrong location. In that case, the application will not find them at the expected path:

```text
backend/data/secrets/
```

## Build WAR locally

From the `./backend` directory:

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

Before running the commands, make sure you are inside the backend directory:

```bash
cd ./backend
```

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
jdbc:postgresql://localhost:5433/exphub
```

Default database credentials for Docker Compose:

```text
database: exphub
user: postgres
password: password
```

## Payment system

The payment module depends on an external banking/payment system. To test payment scenarios, the banking system must also be started separately from another repository.

Example repository:

```text
https://github.com/example/banking-system
```

Clone and run the banking system according to its own README before testing payment flows in this application.

For Docker Compose run, the backend currently expects the banking system to be available inside the Docker network by this URL:

```text
http://exphub-mock-bank:8000
```

If the banking system has a different service/container name or runs on another port, update the `BANK_BASE_URL` value in `backend/docker-compose.yml` before starting the backend.

For example:

```bash
git clone https://github.com/example/banking-system.git
cd banking-system
docker compose up --build
```

After the banking system is running, start the backend application from the `./backend` directory.

## Run without Docker

Before running the commands, make sure you are inside the backend directory:

```bash
cd ./backend
```

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
DB_WEB_URL=jdbc:postgresql://localhost:5433/exphub
DB_USER=postgres
DB_PASSWORD=psql_password
LIQUIBASE_ENABLED=true
HIBERNATE_HBM2DDL_AUTO=validate
SECRETS_WEB_PUBLIC_KEY=./data/secrets/public.pem
SECRETS_WEB_PRIVATE_KEY=./data/secrets/private.pem
```

If payment functionality is required during local/manual run, the banking system from the separate repository must also be running.
