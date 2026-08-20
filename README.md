# Earthquake Management Application

A full-stack web application for fetching, filtering, storing, and visualizing recent earthquake events from the United States Geological Survey (USGS).

The backend retrieves GeoJSON data from the USGS public earthquake feed, validates and maps each event, and stores the resulting records in PostgreSQL. The React frontend allows users to refresh the data, search stored earthquakes, delete records, and view earthquake locations on an interactive map.

## Technologies

### Backend

- Java 26
- Spring Boot 4.1
- Spring Web MVC
- Spring RestClient
- Spring Data JPA
- Hibernate
- PostgreSQL 16
- Maven
- Lombok
- JUnit
- Mockito
- Testcontainers

### Frontend

- React 19
- JavaScript
- Vite
- ESLint
- Leaflet
- React Leaflet
- OpenStreetMap

## Features

- Fetches recent earthquake data from the USGS GeoJSON feed
- Converts USGS timestamps from epoch milliseconds to Java `Instant`
- Extracts:
  - USGS event ID
  - Magnitude
  - Magnitude type
  - Place
  - Title
  - Event time
  - Longitude
  - Latitude
  - Depth
- Filters events by minimum magnitude
- Filters events after a specified time
- Supports combined magnitude and time filtering
- Supports importing all valid earthquakes from the current feed
- Replaces previously stored records during each refresh
- Prevents duplicate USGS event IDs
- Allows individual earthquake records to be deleted
- Displays stored earthquakes in a responsive table
- Displays earthquake locations on an interactive map
- Provides loading, error, empty, and success states
- Includes service integration and controller tests

## External API

The application uses the USGS all-earthquakes feed for the last hour:

```text
https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_hour.geojson
```

USGS API documentation:

```text
https://earthquake.usgs.gov/fdsnws/event/1/
```

The external feed changes continuously, so results may differ between refresh requests.

## Project Structure

```text
Earthquake_Assignment/
├── src/
│   ├── main/
│   │   ├── java/com/example/earthquake_assignment/
│   │   │   ├── clients/
│   │   │   ├── dtos/
│   │   │   ├── exceptions/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   └── web/
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── frontend/
│   ├── public/
│   ├── src/
│   │   ├── api/
│   │   ├── components/
│   │   ├── App.jsx
│   │   └── main.jsx
│   └── package.json
├── compose.yaml
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

## Prerequisites

Install the following before running the application:

- JDK 26
- Docker Desktop
- Node.js LTS and npm
- Git

Docker Desktop must be running in Linux-container mode.

Verify the installed tools:

```powershell
java -version
docker --version
node -v
npm -v
```

## Project Setup

Clone the repository:

```powershell
git clone https://github.com/JordanceE/Earthquake_Assignment.git
cd Earthquake_Assignment
```

## Database Configuration

PostgreSQL is provided through Docker Compose.

The default database configuration is:

| Setting | Value |
|---|---|
| Host | `localhost` |
| Port | `5432` |
| Database | `earthquake_db` |
| Username | `earthquake_user` |
| Password | `earthquake_password` |

Start PostgreSQL:

```powershell
docker compose up -d
```

Check the container status:

```powershell
docker compose ps
```

The application uses the following datasource configuration:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/earthquake_db
spring.datasource.username=${DB_USERNAME:earthquake_user}
spring.datasource.password=${DB_PASSWORD:earthquake_password}
```

The username and password can be overridden with environment variables:

```powershell
$env:DB_USERNAME = "earthquake_user"
$env:DB_PASSWORD = "earthquake_password"
```

If the Docker Compose credentials are changed, the Spring datasource credentials must be changed to match.

Hibernate creates and updates the database schema automatically:

```properties
spring.jpa.hibernate.ddl-auto=update
```

Stop the database without deleting its data:

```powershell
docker compose down
```

To delete the PostgreSQL volume and all stored earthquake data:

```powershell
docker compose down -v
```

## Running the Backend

From the repository root:

### Windows

```powershell
.\mvnw.cmd spring-boot:run
```

### Linux or macOS

```bash
./mvnw spring-boot:run
```

The backend runs at:

```text
http://localhost:8080
```

## Running the Frontend

Open another terminal:

```powershell
cd frontend
npm install
npm run dev
```

The frontend runs at:

```text
http://localhost:5173
```

During development, Vite proxies requests beginning with `/api` to the Spring Boot application on port `8080`.

Both the backend and frontend must be running to use the complete application.

## Building the Frontend

Run the frontend checks:

```powershell
cd frontend
npm run lint
npm run build
```

The production frontend files are generated in:

```text
frontend/dist
```

## Running the Tests

Docker Desktop must be running because the integration tests use a PostgreSQL Testcontainer.

### Windows

```powershell
.\mvnw.cmd test
```

### Linux or macOS

```bash
./mvnw test
```

The tests cover:

- Strict magnitude filtering
- Strict time filtering
- Combined filters
- Missing earthquake IDs
- USGS refresh behavior
- Replacement of existing records
- Invalid USGS features
- USGS API failure behavior
- Importing all valid events
- Controller request parameters
- Record deletion

The tests use a temporary PostgreSQL container and do not modify the development database.

## REST API

The base API path is:

```text
/api/earthquake
```

### Get stored earthquakes

```http
GET /api/earthquake/
```

Optional query parameters:

| Parameter | Description |
|---|---|
| `minMagnitude` | Returns events with magnitude strictly greater than this value |
| `after` | Returns events strictly after this ISO-8601 timestamp |

Example:

```http
GET /api/earthquake/?minMagnitude=2.0&after=2026-08-20T09:00:00Z
```

When both parameters are provided, an earthquake must satisfy both conditions.

### Get an earthquake by database ID

```http
GET /api/earthquake/{id}
```

Example:

```http
GET /api/earthquake/1
```

### Refresh with filtering

```http
POST /api/earthquake/refresh?after=2026-08-20T09:00:00Z&includeAll=false
```

In filtered mode:

- Magnitude must be greater than `2.0`
- Event time must be after the supplied cutoff
- Event type must be `earthquake`

Existing records are deleted before the filtered events are inserted.

### Import all valid earthquakes

```http
POST /api/earthquake/refresh?includeAll=true
```

This imports every valid earthquake event in the current USGS one-hour feed without applying the magnitude and time filters.

Invalid entries and non-earthquake event types are still skipped.

### Delete an earthquake

```http
DELETE /api/earthquake/{id}
```

A successful deletion returns HTTP status `204 No Content`.

## Data Refresh Behavior

A refresh uses snapshot replacement:

1. Fetch the current USGS feed.
2. Validate and map the response.
3. Apply filters when filtered mode is selected.
4. Deduplicate events using the USGS event ID.
5. Delete the currently stored records.
6. Insert the newly fetched records.

The database replacement runs in a transaction. The USGS request is completed before existing records are deleted, so an external API failure does not erase the current database contents.

## Assumptions

- The `all_hour.geojson` endpoint represents the latest one-hour snapshot.
- “Import all” means all valid earthquakes in this feed, not all historical USGS events.
- The USGS feature ID is treated as the external unique event identifier.
- The database uses a separate generated numeric primary key.
- Only features whose type is `earthquake` are imported.
- Entries missing an ID, magnitude, or event time are considered invalid and skipped.
- Missing place or title values are replaced with fallback text.
- Invalid or missing geometry does not prevent an otherwise valid event from being stored, but that event cannot be displayed on the map.
- USGS timestamps are supplied as epoch milliseconds and stored as UTC `Instant` values.
- The frontend displays event times in the user’s local timezone.
- Refreshing replaces the stored snapshot instead of incrementally merging records.
- The provided database credentials are intended only for local development.

## Optional Improvements Implemented

The following features go beyond the minimum required table view:

- Interactive Leaflet map
- OpenStreetMap tiles
- Magnitude-based map marker sizes and colors
- Automatic map bounds based on visible events
- Popup event details
- Import-all option
- Individual record deletion
- Responsive user interface
- Summary cards
- Loading and empty states
- User-visible success and error messages
- Testcontainers-based PostgreSQL integration tests

## Possible Future Improvements

- Pagination for larger datasets
- Scheduled automatic refresh
- Configurable USGS feed URL
- Historical earthquake searches through the USGS query API
- Marker clustering
- Authentication and authorization
- Centralized structured API error responses
- Database migrations using Flyway or Liquibase
- Docker images for the backend and frontend
- Production deployment configuration

## License

This project was created as an interview assignment.
