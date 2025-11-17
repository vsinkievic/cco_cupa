# PostgreSQL Configuration for CUPA (dev-pg profile)

This document describes how to run CUPA application with PostgreSQL database containing production data backup for testing purposes.

## Overview

The `dev-pg` profile allows you to run CUPA in development mode using PostgreSQL instead of the default H2 database. This is useful for testing with production-like data while maintaining development conveniences like hot reload and debug logging.

## Prerequisites

1. PostgreSQL server running on `localhost:5432`
2. Database named `cupa_from_prod`
3. Credentials: `cupa` / `cupa-cupa`
4. Database should contain a backup from production

## Configuration Files

### 1. Application Configuration

**File:** `src/main/resources/config/application-dev-pg.yml`

This file extends the standard `dev` profile with PostgreSQL-specific settings:
- Database connection to `jdbc:postgresql://localhost:5432/cupa_from_prod`
- Uses `public` schema (default PostgreSQL schema)
- PostgreSQL dialect for Hibernate
- Connection pool configuration optimized for local development
- Liquibase contexts set to `dev` (no faker data)

### 2. Startup Script (Command Line)

**File:** `start-dev-pg.sh`

A convenience script to start the application from the command line.

**Usage:**
```bash
./start-dev-pg.sh
```

This is equivalent to:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev-pg
```

### 3. IDE Launch Configurations

**File:** `.vscode/launch.json`

Four launch configurations are available in Cursor/VS Code:

1. **CupaApplication** - Standard H2 development mode
2. **CupaApplication hotswap** - H2 with enhanced class reloading
3. **CupaApplication dev-pg** - PostgreSQL development mode ⭐ NEW
4. **CupaApplication dev-pg hotswap** - PostgreSQL with enhanced class reloading ⭐ NEW

## How to Use

### Method 1: Command Line

1. Ensure PostgreSQL is running and accessible
2. Navigate to the CUPA project directory
3. Run:
   ```bash
   ./start-dev-pg.sh
   ```

### Method 2: IDE (Cursor/VS Code)

1. Open the Run/Debug panel (Ctrl+Shift+D or Cmd+Shift+D)
2. Select one of the PostgreSQL configurations:
   - **CupaApplication dev-pg** - for standard debugging
   - **CupaApplication dev-pg hotswap** - for hot code replacement during debugging
3. Click the Start Debugging button (F5)

### Method 3: Manual Maven Command

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev-pg
```

## Database Connection Details

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/cupa_from_prod
    username: cupa
    password: cupa-cupa
  jpa:
    properties:
      hibernate.default_schema: public
```

## Key Features in dev-pg Profile

- **Development Tools:** Spring DevTools enabled with restart and live reload
- **Logging:** DEBUG level for most packages to aid in development
- **SQL Logging:** Hibernate SQL logging enabled
- **Connection Pool:** Configured with 50 max connections
- **Hot Deploy:** Vaadin hot deployment enabled
- **Port:** Application runs on port 8080 (default)

## Differences from Standard dev Profile

| Feature | dev (H2) | dev-pg (PostgreSQL) |
|---------|----------|---------------------|
| Database | H2 (embedded) | PostgreSQL (localhost) |
| Data | Generated/Faker | Production backup |
| Schema | cupa | public |
| H2 Console | Enabled | Not applicable |
| Liquibase contexts | dev, faker | dev |

## Troubleshooting

### Connection Issues

If you get connection errors:

1. **Check PostgreSQL is running:**
   ```bash
   pg_isready -h localhost -p 5432
   ```

2. **Verify database exists:**
   ```bash
   psql -h localhost -U cupa -l | grep cupa_from_prod
   ```

3. **Test connection:**
   ```bash
   psql -h localhost -U cupa -d cupa_from_prod -c "SELECT current_schema();"
   ```
   (Password: `cupa-cupa`)

### Schema Issues

If you get schema-related errors, verify the schema exists:
```sql
SELECT schema_name FROM information_schema.schemata WHERE schema_name = 'public';
```

### Liquibase Errors

If Liquibase fails to run:
- Check that the database and schema exist
- Verify that Liquibase changesets are compatible with PostgreSQL
- Review the Liquibase changelog at `src/main/resources/config/liquibase/master.xml`

### Authentication Issues

If you get authentication errors:
- Verify the username and password are correct
- Check PostgreSQL's `pg_hba.conf` to ensure local connections are allowed
- Make sure the user `cupa` has appropriate permissions on the database

## Switching Back to H2

To switch back to the default H2 database:

**Command line:**
```bash
./mvnw spring-boot:run
```
or simply:
```bash
./mvnw
```

**IDE:** Use the "CupaApplication" or "CupaApplication hotswap" launch configuration

## Notes

- The PostgreSQL driver is already included in `pom.xml` (version managed by Spring Boot)
- No code changes are needed to switch between H2 and PostgreSQL
- Both profiles use the same port (8080) - you cannot run both simultaneously
- The dev-pg profile inherits all development conveniences from the base dev profile
- HotSwap configurations provide enhanced class reloading capabilities using the `-XX:+AllowEnhancedClassRedefinition` JVM flag

## Related Files

- Configuration: `src/main/resources/config/application-dev-pg.yml`
- Startup Script: `start-dev-pg.sh`
- Launch Configs: `.vscode/launch.json`
- Base Config: `src/main/resources/config/application.yml`
- Dev Config: `src/main/resources/config/application-dev.yml`

