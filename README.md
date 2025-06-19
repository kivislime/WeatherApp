# Weather App

**Weather App** is a sample Spring MVC web application for viewing current weather in user-selected locations.

## 🚀 Features

* Manual user **registration** and **authentication** without Spring Security (custom session and cookie handling).
* **Add** or **remove** cities from a personal favorites list.
* Display a list of favorite locations with current temperature, weather description, and icon.
* Location **search** powered by the OpenWeatherMap Geocoding API.
* **Caching** of weather API calls using Caffeine.
* Scheduled cleanup of expired sessions.

## 📦 Tech Stack

* **Java 17**
* **Spring MVC** (no Spring Boot)
* **Thymeleaf**
* **Hibernate/JPA** + **Liquibase**
* **Caffeine Cache**
* **PostgreSQL** (or MySQL/MariaDB)
* **Docker** / **Docker Compose**
* **JUnit 5**, Mockito, Spring Test

## 🔧 Setup and Run

1. **Clone the repository**

   ```bash
   git clone https://github.com/yourname/weather-app.git
   cd weather-app
   ```

2. **Configure environment variables**
   In development, use the `dev` Spring profile. Set the profile in `docker-compose.yaml`:

   ```yaml
   services:
     app:
       environment:
         - SPRING_PROFILES_ACTIVE=dev
   ```

   Two property files coexist:

    * `application.properties` (common settings)
    * `application-dev.properties` (development overrides)


3. **Create your secrets.properties**

  ```bash
   openweather.api-key=YOUR_API_KEY
   ```

4. **Launch with Docker Compose**
* DEV: in‑memory H2 + app
   ```bash
   docker compose up --profile dev --build -d
   ```
* PROD: PostgreSQL + app + Liquibase
  ```bash
  docker compose --profile prod up --build -d
  ```

5. **Access the application**
   Open your browser to:

   ```
   http://localhost:8080
   ```

## 🌐 Spring Profiles

* **dev** — H2 (in-memory), local development.
* **prod** — real database (PostgreSQL), Liquibase migrations enabled.
* **test** — in-memory database for integration tests.