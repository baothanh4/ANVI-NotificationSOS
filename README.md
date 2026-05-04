# ANVI SOS Backend Starter

This project provides the Phase 1-2 backend foundation for ANVI SOS: auth, QR tokens, SOS alerts, emergency contacts, health records, audit logs, and access grants.

## Requirements
- Java 21
- PostgreSQL

## Configuration
Set environment variables or update `src/main/resources/application.properties`:
- `ANVI_DB_URL`
- `ANVI_DB_USER`
- `ANVI_DB_PASSWORD`
- `ANVI_JWT_SECRET`

## Run
```bash
./mvnw test
./mvnw spring-boot:run
```

## Key APIs
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/cards`
- `POST /api/qr/cards/{cardId}/tokens`
- `GET /api/qr/resolve/{shortCode}`
- `POST /api/sos/trigger`
- `POST /api/access-grants/request`
- `POST /api/access-grants/{id}/approve`
- `GET /api/access-grants/{id}/stream`

# ANVI-NotificationSOS
