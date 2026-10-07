# Oona — Backend (Soft-Backend)

API REST de la plataforma **Oona**, el marketplace de experiencias de bienestar del núcleo de desarrollo de software. Expone la autenticación (JWT + Google), el catálogo público de experiencias, la gestión del profesional, el checkout simulado, la comunidad y el panel de administración.

> Frontend: [NucleoSoftwareCV/Soft-Frontend](https://github.com/NucleoSoftwareCV/Soft-Frontend)

---

## Funcionalidad

**Pública**
- Catálogo de experiencias: eventos, ocurrencias (fechas con aforo), retiros y sesiones 1:1, con búsqueda y filtros (categoría, precio, fechas, ciudad, modalidad, horario, recurrencia).
- Perfiles públicos de profesionales con búsqueda por nombre, biografía, temas, técnicas, servicios y eventos.
- Catálogos maestros: ciudades, categorías, tipos de experiencia, temas y técnicas de sesión, ubicaciones e intereses por ciudad.
- Autenticación: registro, login (username o email), login con Google (validación de `id_token`), refresh y logout, recuperación de contraseña por correo.
- Login ERP específico para administradores (`/api/v1/auth/admin`).

**Cliente autenticado**
- Onboarding y preferencias (intereses y ciudad), "Mi espacio".
- Favoritos de eventos y seguimiento de profesionales.
- Checkout: creación de órdenes, pago simulado (tarjeta o Yape), comprobante digital, enlace de WhatsApp y "Mis reservas".
- Comunidad: solicitud única y actualizable de Match Bienestar.

**Profesional (rol `PROFESSIONAL`)**
- Solicitud de alta desde Círculo Oona (`/professional-applications/me`) sujeta a decisión del administrador.
- Gestión de eventos: CRUD, imágenes, ocurrencias con aforo, asistentes por fecha y estado borrador/publicado.
- Gestión de sesiones 1:1: CRUD, imagen, precio, reserva online y estado.

**Administrador (rol `ADMIN`)**
- Revisión y decisión (aprobar/rechazar con motivo) de solicitudes profesionales.
- Catálogos maestros: ciudades y catálogos de eventos.

## Stack

| Tecnología | Uso |
|------------|-----|
| Java 21 | Lenguaje |
| Spring Boot 3.5 (Web, Security, JPA, Validation, Actuator, Mail) | Framework |
| PostgreSQL | Base de datos |
| Spring Security + JWT (access + refresh) | Autenticación y roles |
| springdoc-openapi (Swagger UI) | Documentación interactiva de la API |
| Maven (wrapper incluido) | Build |

## Requisitos

- **JDK 21**
- **PostgreSQL** con la base de datos `OonaDB` creada
- (Opcional) Docker

## Puesta en marcha (desarrollo)

```bash
# 1. Clonar (rama develop)
git clone -b develop https://github.com/NucleoSoftwareCV/Soft-Backend.git
cd Soft-Backend

# 2. Configurar variables de entorno
cp .env.example .env
#    Editar .env: DB_USERNAME / DB_PASSWORD (y el resto si aplica)

# 3. Levantar la base de datos (ejemplo con Docker)
docker run -d --name oona-db -e POSTGRES_DB=OonaDB \
  -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 postgres:16

# 4. Ejecutar
./mvnw spring-boot:run
```

La API queda disponible en **http://localhost:8083** (puerto que espera el proxy del frontend).

> `.env.example` trae `SPRING_JPA_DDL_AUTO=create` para la primera ejecución: crea el esquema completo. Si vas a reiniciar la app y quieres **conservar los datos**, cámbialo a `update` (es el valor por defecto del perfil `dev`).

### Verificación rápida

| Servicio | URL |
|----------|-----|
| Swagger UI | `http://localhost:8083/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8083/v3/api-docs` |
| Health check | `http://localhost:8083/actuator/health` |

## Datos semilla

Con el perfil `dev` (activo por defecto), la app siembra al arrancar: roles (`USER`, `PROFESSIONAL`, `ADMIN`), usuarios de prueba, catálogos maestros, tipos de experiencia y perfiles/demo de profesionales con eventos y sesiones de ejemplo.

| Usuario | Email | Contraseña | Rol |
|---------|-------|-----------|-----|
| `user1` | `user1@oona.es` | `User1?` | Cliente |
| `user2` | `user2@oona.es` | `User2!` | Cliente |
| `admin_main1` | `admin1@oona.es` | `Admin1@` | Administrador |
| `admin_main2` | `admin2@oona.es` | `Admin2#` | Administrador |
| `specialist_ana` / `specialist_carlos` | `ana@oona.es` / `carlos@oona.es` | *(ver `DataSeederOrchestrator`)* | Profesionales demo |
| `professional_demo` | `professional_demo@oona.es` | *(ver `DataSeederOrchestrator`)* | Cliente + Profesional |

> Las contraseñas se almacenan hasheadas (BCrypt). Cambia las credenciales de administrador antes de cualquier entorno expuesto.

## Mapa de la API

| Módulo | Ruta base | Acceso |
|--------|-----------|--------|
| Autenticación | `/api/auth` | Pública (login, registro, Google, refresh, recuperación) |
| Login ERP admin | `/api/v1/auth/admin` | Pública (credenciales de admin) |
| Usuario actual | `/api/users/me` | Autenticado |
| Perfil de cliente | `/api/v1/client-profiles/me/**` | Autenticado (espacio, preferencias, onboarding) |
| Eventos (catálogo) | `/api/v1/events` | Público (listado y detalle con filtros) |
| Eventos (gestión) | `/api/v1/events` (POST, `/my-events`, imágenes, ocurrencias, estado) | Rol `PROFESSIONAL` |
| Ocurrencias | `/api/v1/event-occurrences` | Público/Mixto |
| Sesiones 1:1 (catálogo y gestión) | `/api/v1/one-to-one-services` | Público / Rol `PROFESSIONAL` |
| Perfiles de profesionales | `/api/v1/specialist-profiles` | Público (búsqueda y perfil por slug) |
| Solicitudes profesionales | `/api/v1/professional-applications` | `/me` autenticado · `/admin/**` rol `ADMIN` |
| Favoritos y seguimiento | `/api/v1/interactions/me/**` | Autenticado |
| Checkout y pagos | `/api/v1/payments/checkout` | Autenticado (órdenes, pago simulado, comprobantes, reservas) |
| Comunidad (Match Bienestar) | `/api/v1/community/match-request` | Autenticado |
| Catálogos maestros | `/api/v1/cities`, `/api/v1/categories`, `/api/v1/experience-types`, `/api/v1/work-topics`, `/api/v1/techniques`, `/api/v1/locations`, `/api/v1/city-interests` | Público (listados) / `ADMIN` (escritura) |
| Home | `/api/v1/home` | Público |

## Estructura del proyecto

```
src/main/java/com/hean/consigueventas/oonabe/
├── auth/                 # Autenticación (AuthController, AdminAuthController, JWT)
├── user/                 # Usuarios y roles
├── profileCliente/       # Mi espacio, preferencias y onboarding del cliente
├── profileProfesional/   # Perfiles de especialistas y su medio (imágenes)
├── professionalApplication/  # Solicitudes para convertirse en profesional
├── event/                # Eventos: catálogo, gestión y ocurrencias
├── oneToOneSession/      # Sesiones 1:1: catálogo y gestión
├── home/                 # Agregados de la página de inicio
├── payment/              # Checkout, órdenes, pagos simulados y comprobantes
├── interaction/          # Favoritos y seguimiento de profesionales
├── community/            # Match Bienestar (emparejamiento por intereses)
├── category/ · experienceType/ · masterdata/   # Catálogos maestros (ciudades, técnicas, temas…)
├── common/               # Configuración: seguridad, CORS, almacenamiento, seeders
└── ...
```

Cada módulo sigue la organización `controller/` · `service/` · `repository/` · `entity/` · `dto/`.

## ⚙Configuración (`.env`)

Variables principales (ver `.env.example` y `.env.prod.example`):

| Variable | Descripción |
|----------|-------------|
| `DB_URL` · `DB_USERNAME` · `DB_PASSWORD` | Conexión a PostgreSQL (`jdbc:postgresql://localhost:5432/OonaDB`) |
| `JWT_SECRET` · `JWT_EXPIRATION_MS` · `JWT_REFRESH_EXPIRATION_MS` | Firma y vigencia de tokens (dev: 15 min / 7 días) |
| `GOOGLE_CLIENT_ID` | Client ID de OAuth para el login con Google |
| `MAIL_HOST` · `MAIL_PORT` · `MAIL_USERNAME` · `MAIL_PASSWORD` · `MAIL_FROM` | SMTP para recuperación de contraseña |
| `APP_FRONTEND_URL` | URL del frontend (enlaces de correo) |
| `SPRING_PROFILES_ACTIVE` | `dev` (defecto) o `prod` |
| `SPRING_JPA_DDL_AUTO` | `create` (primera vez) / `update` (conservar datos) |

## Docker

```bash
# Imagen multi-stage (JDK 21 → JRE 21, usuario no-root, healthcheck)
docker build -t oona-be .

docker run -d --name oona-backend -p 8083:8083 \
  --env-file .env oona-be
```

El contenedor expone el healthcheck en `/actuator/health` y corre como usuario `spring` sin privilegios.

## Tests

```bash
./mvnw test
```

##  Seguridad

- JWT con access token (15 min) y refresh token (7 días), con renovación automática desde el frontend.
- Roles `USER`, `PROFESSIONAL` y `ADMIN` protegidos a nivel de endpoint.
- Cifrado de campos sensibles (clave `APP_FIELD_ENCRYPTION_KEY`).
- Validación de entrada con Bean Validation y control de propiedad de recursos por usuario autenticado.
