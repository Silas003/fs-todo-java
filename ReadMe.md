# Notes Application

## Project Overview
A plain Spring MVC application (non-Boot) for managing personal notes with a simple HTML UI. This version is completely anonymous and does not require authentication.

## Features
- CRUD operations for notes
- Persistence using H2 database (in-memory)

## Architecture
- **Framework:** Spring Framework 6 (MVC, Data JPA)
- **UI:** Thymeleaf templates
- **Database:** H2 (Embedded)
- **Packaging:** WAR

## Configuration
- `WebConfig`: Configures Spring MVC and Thymeleaf.
- `PersistenceConfig`: Configures JPA, EntityManager, and DataSource.
- `AppInitializer`: Bootstraps the application without `web.xml`.

## Running the Application
Since this is a plain Spring MVC application (non-Boot) packaged as a WAR, you have two main options to run it:

### Option 1: Using Jetty Maven Plugin (Easiest)
You can run the application directly from the command line using the Jetty Maven plugin:

```bash
mvn jetty:run
```
Then open your browser and go to: `http://localhost:8080/notes`

### Option 2: Traditional Deployment
1. Build the WAR file:
   ```bash
   mvn clean package
   ```
2. Deploy the generated WAR file (`target/notesApi-0.0.1-SNAPSHOT.war`) to a Servlet container like **Apache Tomcat 10+** (since the project uses Jakarta Servlet 6.0).
3. Start Tomcat and access the application at `http://localhost:8080/notesApi-0.0.1-SNAPSHOT/notes` (or the configured context path).

## UI Endpoints
- `/notes`: List all notes
- `/notes/new`: Create a new note
- `/notes/edit/{id}`: Edit an existing note

## Testing
To run the tests, use:
```bash
mvn test
```
# fs-notes
# fs-todo
