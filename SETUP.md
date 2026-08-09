# Environment Setup Guide

## Backend Configuration

The backend requires database credentials to be set via environment variables for security.

### Local Development Setup

1. Copy the example configuration:
```bash
cp fundspark-backend/src/main/resources/application.properties.example fundspark-backend/src/main/resources/application.properties
```

2. Create a local properties file (not tracked by git):
```bash
cat > fundspark-backend/src/main/resources/application-local.properties << EOF
spring.datasource.username=root
spring.datasource.password=your_password_here
EOF
```

3. Set environment variables (or use application-local.properties):
```bash
export DB_USERNAME=root
export DB_PASSWORD=your_password_here
```

### Environment Variables

- `DB_USERNAME`: Database username (default: root)
- `DB_PASSWORD`: Database password (required for production)
- `DB_URL`: Database connection URL (default: jdbc:mysql://localhost:3306/fundspark)

### Important Security Notes

⚠️ **DO NOT commit the following files:**
- `application-local.properties` - Contains your local database password
- `.env` files - Any environment variable files
- Files containing API keys, tokens, or secrets

These files are protected by `.gitignore` to prevent accidental commits.

## Running the Application

### With Environment Variables
```bash
export DB_USERNAME=root
export DB_PASSWORD=your_password
mvn spring-boot:run
```

### With application-local.properties
The application will automatically load local credentials from `application-local.properties` if it exists.

---

For more information on Spring Boot property configuration, see:
https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config
