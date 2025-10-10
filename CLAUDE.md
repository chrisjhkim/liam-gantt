# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

**Liam Gantt Chart Application** - Project management web application with Gantt chart visualization

> **Status**: Backend complete (107 tests passing) | Frontend in progress
> **Last Updated**: 2025-10-10
> **Tech**: Java 21 + Spring Boot 3.5.5 + Gradle + H2/MariaDB

## Quick Commands
```bash
/build      # Clean + compile + test + jar
/test       # Run all tests (unit + integration)
/clean      # Clean build artifacts
/migrate    # Flyway database migration

# Development commands
./gradlew bootRun                    # Run app (http://localhost:8080)
./gradlew test --tests *TaskService* # Run specific test class
./gradlew bootRun --args='--debug'   # Debug mode with detailed logs
```

## Architecture Overview

**Layered Architecture**: Controller → Service → Repository → Entity

```
Request Flow:
┌─────────────┐
│ Controller  │ → Validate input, call service, return DTO
│ (API/Web)   │    Exception: @Valid fails → 400 Bad Request
└──────┬──────┘
       ↓
┌─────────────┐
│  Service    │ → Business logic, transaction boundary
│ (@Service)  │    Exception: Not found → 404, Invalid state → 400
└──────┬──────┘
       ↓
┌─────────────┐
│ Repository  │ → Data access with Spring Data JPA
│ (JPA)       │    Exception: DB errors propagate to service
└──────┬──────┘
       ↓
┌─────────────┐
│  Entity     │ → Database mapping with relationships
│ (JPA)       │    Validation: @NotNull, @Size at entity level
└─────────────┘
```

## Critical Patterns

### 1. DTO Mapping (Entity ↔ DTO)
**Never expose entities directly in controllers**. Always use DTOs.

```java
// Service layer pattern
public ProjectResponseDto createProject(ProjectRequestDto requestDto) {
    Project entity = ProjectMapper.toEntity(requestDto);
    Project saved = repository.save(entity);
    return ProjectMapper.toResponseDto(saved);
}
```

**Mapper locations**: `src/main/java/com/liam/gantt/mapper/`

### 2. Exception Handling Strategy
**GlobalExceptionHandler** catches all exceptions and returns consistent API responses.

```java
// Standard exception flow
Service throws → ProjectNotFoundException
              → GlobalExceptionHandler catches
              → Returns ApiResponse with 404 status

// Custom exceptions (all in exception/ package)
- ProjectNotFoundException → 404
- TaskNotFoundException → 404
- InvalidRequestException → 400
- General Exception → 500
```

### 3. Transaction Boundaries
**@Transactional** at service layer only, not controller or repository.

```java
@Service
public class ProjectServiceImpl {
    @Transactional  // Transaction starts here
    public ProjectResponseDto createProject(...) { }

    @Transactional(readOnly = true)  // Optimization for reads
    public ProjectResponseDto getProject(Long id) { }
}
```

## Database Management

**H2 Console**: http://localhost:8080/h2-console (JDBC: `jdbc:h2:mem:gantt_dev`, User: `sa`)

**Migration Pattern**: All schema changes via Flyway only
```
src/main/resources/db/migration/
└── V001__create_projects_table.sql
└── V002__create_tasks_table.sql
└── V003__create_task_dependencies_table.sql
└── V004__add_updated_at_to_dependencies.sql
└── V005__add_project_status_column.sql
```

**Never**: Modify schema directly or use `spring.jpa.hibernate.ddl-auto=update`

## Testing Strategy

**107 tests passing** across all layers:
- **Service**: Mockito for repository mocking, verify business logic
- **Controller**: MockMvc for API testing, validate responses
- **Repository**: @DataJpaTest with real H2 database

```bash
# Run all tests
./gradlew test

# Run single test class
./gradlew test --tests "ProjectServiceImplTest"

# Run single test method
./gradlew test --tests "ProjectServiceImplTest.createProject_ShouldReturnSavedProject"

# Continuous testing (re-run on file changes)
./gradlew test --continuous
```

## API Response Format

**Standard Success Response** (via ApiResponse.java):
```json
{
  "status": "success",
  "data": { "id": 1, "name": "Project Alpha" },
  "message": "Project created successfully"
}
```

**Standard Error Response** (via GlobalExceptionHandler):
```json
{
  "status": "error",
  "error": {
    "code": "PROJECT_NOT_FOUND",
    "message": "Project with id 1 not found"
  },
  "timestamp": "2025-10-10T14:30:00"
}
```

## Key REST Endpoints

```http
# Projects
GET    /api/v1/projects           # List all projects
POST   /api/v1/projects           # Create project
GET    /api/v1/projects/{id}      # Get project details
PUT    /api/v1/projects/{id}      # Update project
DELETE /api/v1/projects/{id}      # Delete project

# Tasks
GET    /api/v1/projects/{id}/tasks  # List project tasks
POST   /api/v1/projects/{id}/tasks  # Create task
GET    /api/v1/tasks/{id}           # Get task details
PUT    /api/v1/tasks/{id}           # Update task
DELETE /api/v1/tasks/{id}           # Delete task

# Gantt Chart
GET    /api/v1/gantt/{projectId}         # Get Gantt data
POST   /api/v1/gantt/dependencies        # Add dependency
DELETE /api/v1/gantt/dependencies/{id}   # Remove dependency
```

## Project Structure (Key Locations)

```
src/main/java/com/liam/gantt/
├── controller/
│   ├── api/v1/              # REST API controllers (COMPLETE)
│   ├── HomeController       # Web homepage
│   └── ProjectWebController # Web UI controller
├── service/
│   ├── impl/                # Service implementations (COMPLETE)
│   └── [Interfaces]         # Service contracts
├── mapper/                  # DTO ↔ Entity mappers (COMPLETE)
├── repository/              # Spring Data JPA repos (COMPLETE)
├── entity/                  # JPA entities + enums (COMPLETE)
├── dto/
│   ├── request/             # API request DTOs
│   └── response/            # API response DTOs
└── exception/               # Custom exceptions + GlobalExceptionHandler

src/main/resources/
├── db/migration/            # Flyway SQL scripts
├── templates/               # Thymeleaf templates (IN PROGRESS)
└── application.yml          # Spring configuration
```

## Development Guidelines

**Layer-Specific Guides** (detailed patterns in each layer):
- **Controllers**: `src/main/java/com/liam/gantt/controller/CLAUDE.md`
- **Services**: `src/main/java/com/liam/gantt/service/CLAUDE.md`
- **Repositories**: `src/main/java/com/liam/gantt/repository/CLAUDE.md`

**Comprehensive Architecture**: `docs/guides/ARCHITECTURE.md` (full patterns, workflows, standards)

## Common Development Tasks

**Adding a new entity**:
1. Create migration: `V00X__create_entity_table.sql`
2. Create entity: `entity/NewEntity.java`
3. Create repository: `repository/NewEntityRepository.java`
4. Create DTOs: `dto/request/` + `dto/response/`
5. Create mapper: `mapper/NewEntityMapper.java`
6. Create service: `service/NewEntityService.java` + `impl/`
7. Create controller: `controller/api/v1/NewEntityController.java`
8. Write tests for each layer

**Hot reload in development**:
- Add `spring-boot-devtools` dependency (already configured)
- Changes to Java files auto-reload on build (Ctrl+F9 in IntelliJ)

## Troubleshooting

- **Build fails**: `/clean` then `/build`
- **Tests fail**: Check test logs, verify database state
- **DB schema mismatch**: `/migrate` to sync schema
- **Port 8080 in use**: `./gradlew bootRun --args='--server.port=9090'`
- **Gradle daemon issues**: `./gradlew --stop` then retry
