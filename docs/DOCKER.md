# Guia Docker local: solo PostgreSQL

Esta guia explica como levantar la base de datos local del backend.

Por ahora, en desarrollo local se levantara solo:

- PostgreSQL

La API Spring Boot se ejecuta desde IntelliJ cuando sea necesario. No se levanta por defecto con Docker.

Configuracion local estandar:

```text
Database: OonaDB
Username: postgres
Password: postgres
PostgreSQL port: 5432
```

> Estos valores son solo para desarrollo local. En produccion se deben usar secretos reales.

## 1. Requisitos

Cada integrante debe tener instalado:

- Docker Desktop
- Git
- Java 21, para ejecutar Spring Boot desde IntelliJ
- DBeaver, opcional para revisar la base de datos

Verifica que Docker funcione:

```powershell
docker --version
docker compose version
```

## 2. Clonar el proyecto

```powershell
git clone <URL_DEL_REPOSITORIO>
cd Soft-Backend
```

Si ya tienes el proyecto clonado:

```powershell
git pull
```

## 3. Crear el archivo .env

El archivo `.env` no se sube al repositorio porque es configuracion local.

Copia el ejemplo:

```powershell
copy .env.example .env
```

El `.env` debe quedar asi:

```env
POSTGRES_DB=OonaDB
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

DB_URL=jdbc:postgresql://localhost:5432/OonaDB
DB_USERNAME=postgres
DB_PASSWORD=postgres

JWT_SECRET=dev-local-jwt-secret-change-only-for-production-1234567890
JWT_EXPIRATION_MS=900000

SPRING_JPA_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=false
```

Aunque solo levantemos PostgreSQL, mantenemos `JWT_SECRET` en el `.env` para que IntelliJ pueda ejecutar la API localmente sin errores.

## 4. Levantar solo PostgreSQL

Comando recomendado para desarrollo local:

```powershell
docker compose up -d postgres
```

Tambien funciona:

```powershell
docker compose up -d
```

Ese comando levanta solo PostgreSQL porque la API esta dentro del profile `app`.

Verifica el estado:

```powershell
docker compose ps
```

Debe aparecer:

```text
oona-postgres   Up ... healthy
```

No debe aparecer `oona-api` si solo estas levantando la base.

## 5. Configurar DBeaver

Crea una conexion PostgreSQL con estos datos:

```text
Host: localhost
Port: 5432
Database: OonaDB
Username: postgres
Password: postgres
```

Importante:

- En `Database` va `OonaDB`.
- En `Username` va `postgres`.
- En `Password` va `postgres`.
- No pongas `postgres` como database si quieres entrar a la base del proyecto.

Luego presiona `Test Connection`.

## 6. Ejecutar la API desde IntelliJ

Con PostgreSQL ya levantado, ejecuta la clase principal:

```text
OonaBeApplication
```

La app usara:

```properties
DB_URL=jdbc:postgresql://localhost:5432/OonaDB
DB_USERNAME=postgres
DB_PASSWORD=postgres
JWT_SECRET=dev-local-jwt-secret-change-only-for-production-1234567890
```

Cuando la API este corriendo desde IntelliJ, puedes abrir:

```text
http://localhost:8083/swagger-ui.html
```

Healthcheck:

```text
http://localhost:8083/actuator/health
```

Categorias:

```text
http://localhost:8083/api/v1/categories
```

## 7. Comandos utiles

Ver contenedores:

```powershell
docker compose ps
```

Ver logs de PostgreSQL:

```powershell
docker compose logs -f postgres
```

Parar PostgreSQL sin borrar datos:

```powershell
docker compose down
```

Parar PostgreSQL y borrar la base local:

```powershell
docker compose down -v
```

Usa `down -v` solo si quieres reiniciar la base desde cero.

## 8. Solucion de errores comunes

### Error en DBeaver: password authentication failed

Puede pasar si ya habias levantado PostgreSQL antes con otro usuario o password.

Si no tienes datos importantes, reinicia la base local:

```powershell
docker compose down -v
docker compose up -d postgres
```

Luego prueba DBeaver con:

```text
Host: localhost
Port: 5432
Database: OonaDB
Username: postgres
Password: postgres
```

### Error: port is already allocated

Significa que ya tienes algo usando el puerto `5432`.

Revisa contenedores activos:

```powershell
docker ps
```

Si es un contenedor viejo del mismo proyecto:

```powershell
docker compose down
docker compose up -d postgres
```

Si tienes PostgreSQL instalado directamente en tu PC usando el puerto `5432`, debes detenerlo o cambiar el puerto en `docker-compose.yml`.

### Error: JWT_SECRET is required

Este error solo aparece si intentas levantar la API con Docker usando el profile `app`.

Para solo base de datos, usa:

```powershell
docker compose up -d postgres
```

Si necesitas levantar la API con Docker, revisa que `.env` tenga:

```env
JWT_SECRET=dev-local-jwt-secret-change-only-for-production-1234567890
```

## 9. API en Docker, solo si se necesita

Por ahora no es el flujo recomendado para el equipo, pero se conserva por si alguien necesita probar todo dentro de Docker:

```powershell
docker compose --profile app up -d --build
```

Ver estado:

```powershell
docker compose --profile app ps
```

Parar API + PostgreSQL:

```powershell
docker compose --profile app down
```

## 10. Produccion

Para produccion no se deben usar:

```text
postgres/postgres
dev-local-jwt-secret-change-only-for-production-1234567890
SPRING_JPA_DDL_AUTO=update
```

En produccion se debe usar `docker-compose.prod.yml` con variables reales:

```powershell
docker compose --env-file .env.prod -f docker-compose.prod.yml up -d --build
```

Recomendado para produccion:

- Password fuerte para PostgreSQL.
- `JWT_SECRET` largo, privado y diferente al local.
- `SPRING_JPA_DDL_AUTO=validate`.
- Migraciones con Flyway o Liquibase cuando el esquema ya este definido.
