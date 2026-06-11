# Docker local y produccion

## Desarrollo local: solo PostgreSQL

1. Copia `.env.example` como `.env`.
2. Cambia `POSTGRES_PASSWORD` y `JWT_SECRET`.
3. Levanta la base de datos:

```powershell
docker compose up -d postgres
```

La app local de Spring Boot puede conectarse con:

```properties
DB_URL=jdbc:postgresql://localhost:5432/OonaDB
DB_USERNAME=oona_user
DB_PASSWORD=<POSTGRES_PASSWORD>
JWT_SECRET=<JWT_SECRET>
```

## Desarrollo local: API + PostgreSQL en Docker

```powershell
docker compose --profile app up -d --build
```

API:

```text
http://localhost:8083
```

Swagger:

```text
http://localhost:8083/swagger-ui.html
```

Health:

```text
http://localhost:8083/actuator/health
```

## Produccion

Usa `docker-compose.prod.yml` con variables de entorno reales, idealmente desde el secret manager del proveedor. Puedes tomar `.env.prod.example` como plantilla.

```powershell
docker compose --env-file .env.prod -f docker-compose.prod.yml up -d --build
```

Para produccion, `SPRING_JPA_DDL_AUTO` debe quedar en `validate` y las migraciones deberian manejarse con Flyway o Liquibase.
