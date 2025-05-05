# Task Tracker API

A RESTful Spring Boot application with role-based access control (RBAC) for managing projects and tasks in a collaborative setting.

## Features

- Role-based access control (RBAC) with three roles: ADMIN, MANAGER, USER
- JWT-based authentication
- Projects and tasks management
- Task prioritization and status tracking
- RESTful API with comprehensive documentation

## Default Admin User

The application comes with a default admin user created during startup:

- **Email**: admin@tasktracker.com
- **Password**: admin

## Running the Application

1. Make sure you have Java 17 or higher installed
2. Navigate to the project directory
3. Run `./mvnw spring-boot:run`
4. Access the Swagger UI at http://localhost:8080/swagger-ui.html

## Roles and Permissions

- **ADMIN**: Has full access to all resources in the system
- **MANAGER**: Can create and manage their own projects and related tasks
- **USER**: Can view and update only their own assigned tasks

## API Endpoints

### Authentication

- `POST /api/v1/auth/register` - Register a new user (default role is USER)
- `POST /api/v1/auth/login` - Authenticate a user and get JWT token

### User Management

- `GET /api/v1/users` - Get all users (ADMIN only)
- `GET /api/v1/users/{id}` - Get user by ID (ADMIN only)
- `GET /api/v1/users/me` - Get current user profile
- `PATCH /api/v1/users/{id}/role` - Assign role to a user (ADMIN only)

### Projects

- `GET /api/v1/projects` - Get all projects (ADMIN only)
- `GET /api/v1/projects/me` - Get projects owned by the current user
- `GET /api/v1/projects/{id}` - Get project by ID
- `POST /api/v1/projects` - Create a new project (ADMIN and MANAGER only)
- `PUT /api/v1/projects/{id}` - Update a project
- `DELETE /api/v1/projects/{id}` - Delete a project

### Tasks

- `GET /api/v1/tasks` - Get all tasks (ADMIN only)
- `GET /api/v1/tasks/me` - Get tasks assigned to the current user
- `GET /api/v1/tasks/{id}` - Get task by ID
- `GET /api/v1/projects/{projectId}/tasks` - Get tasks by project
- `POST /api/v1/tasks` - Create a new task
- `PUT /api/v1/tasks/{id}` - Update a task
- `PATCH /api/v1/tasks/{id}/status` - Update task status
- `PATCH /api/v1/tasks/{id}/assign/{userId}` - Assign task to a user
- `DELETE /api/v1/tasks/{id}` - Delete a task

## How Authentication Works

1. Register a user or use the default admin account
2. Authenticate using the login endpoint to receive a JWT token
3. Include the token in the Authorization header of subsequent requests: `Authorization: Bearer {token}`
4. The token contains information about the user and their role, which is used to enforce access controls

## Technical Stack

- Java 17
- Spring Boot 3+
- Spring Security with JWT
- Spring Data JPA with Hibernate
- H2 Database (for development)
- Swagger/OpenAPI for documentation

## Technology Stack

- Java 17
- Spring Boot 3.4.5
- Spring Security with JWT
- Spring Data JPA with Hibernate
- H2 Database (development) / PostgreSQL (production)
- MapStruct for DTO mapping
- Lombok for reducing boilerplate code
- Swagger/OpenAPI for API documentation
- JUnit and Mockito for testing

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

## Getting Started

### Running the Application (Development)

1. Clone the repository:
   ```sh
   git clone https://github.com/yourusername/task-tracker-api.git
   cd task-tracker-api
   ```

2. Build the application:
   ```sh
   mvn clean install
   ```

3. Run the application:
   ```sh
   mvn spring-boot:run
   ```

The application will start on `http://localhost:8080`.

### H2 Console

When running in development mode with H2 database, you can access the H2 console at:
```
http://localhost:8080/h2-console
```

- JDBC URL: `jdbc:h2:mem:taskdb`
- Username: `sa`
- Password: `` (empty)

### API Documentation

Swagger UI is available at:
```
http://localhost:8080/swagger-ui.html
```

## Postman Collection

A Postman collection is included in the project for testing the API endpoints. Import the `Task-Tracker-API.postman_collection.json` file into Postman to get started.

## License

This project is licensed under the MIT License - see the LICENSE file for details. 