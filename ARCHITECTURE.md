# Arquitectura del backend de Oona

Este documento describe la arquitectura que existe actualmente en el repositorio. También establece convenciones para desarrollar nuevas funcionalidades sin presentar como implementado aquello que todavía solo está modelado.

## 1. Visión general

Oona Backend es un monolito modular desarrollado con Spring Boot. El código se organiza mediante **package-by-feature**: cada paquete representa un dominio y, cuando la funcionalidad está implementada, contiene sus propias capas técnicas.

El paquete raíz es:

```text
com.hean.consigueventas.oonabe
```

La aplicación no está dividida en microservicios ni aplica aislamiento estricto entre módulos. Todos los dominios se ejecutan dentro del mismo proceso y comparten la misma base de datos PostgreSQL y el mismo contexto de Spring.

## 2. Estado real de los módulos

No todos los módulos tienen el mismo nivel de implementación. Algunos exponen casos de uso completos y otros contienen únicamente el modelo JPA preparado para funcionalidades futuras.

| Módulo | Responsabilidad real | Estado actual |
| --- | --- | --- |
| `admin` | Registro de acciones administrativas mediante la entidad `AuditLog`. | Solo persistencia; no tiene controlador, servicio ni repositorio propio. |
| `auth` | Registro, inicio de sesión tradicional y con Google, emisión de JWT, refresh tokens, cierre de sesión, carga de usuarios para Spring Security y limitación básica de intentos de autenticación. | Implementado con API, servicios, repositorio y filtros de seguridad. |
| `booking` | Modelo de reservas para eventos y sesiones individuales, asistentes e historial de estados. | Solo entidades JPA; no expone API ni casos de uso. |
| `catalog` | Datos maestros reutilizables. Actualmente modela ciudades (`City`) y especialidades profesionales (`Specialty`). | La consulta de ciudades está implementada; `Specialty` solo tiene entidad. No representa un catálogo general de ofertas o servicios. |
| `category` | Administración y consulta de categorías que pueden asociarse a eventos. | Implementado con API, servicio, repositorio, DTOs y mapper. La lectura es pública y las escrituras requieren rol de administrador. |
| `common` | Elementos transversales: configuración de seguridad y OpenAPI, inicialización de datos, excepciones globales, enumeraciones, auditoría base y utilidades de seguridad. | Infraestructura compartida activa. |
| `content` | Modelo de secciones configurables para la página de inicio mediante `HomeSection`. | Solo entidad JPA; no tiene API ni servicio. |
| `event` | Modelo de eventos, imágenes, profesionales y categorías asociadas. Implementa la gestión y consulta de ocurrencias calendarizadas de eventos. | Las ocurrencias tienen API y servicio; el evento principal y sus relaciones están modelados, pero no tienen un CRUD propio completo. |
| `interaction` | Favoritos polimórficos mediante `Favorite` y `FavoriteId`. | Solo entidades JPA. No existen todavía mensajería, chat, comentarios ni valoraciones. |
| `location` | Ubicaciones físicas utilizadas por eventos y sesiones. | Implementa una consulta pública de ubicaciones activas. |
| `payment` | Modelo de órdenes de compra, ítems, pagos, métodos de pago, comprobantes y reembolsos. | Solo entidades JPA; no existe API, servicio ni integración con una pasarela de pagos. |
| `profile` | Modelo de perfiles de clientes y especialistas, imágenes, redes sociales y especialidades del profesional. | Solo entidades JPA; no expone API ni casos de uso. |
| `service` | Modelo de servicios individuales ofrecidos por especialistas, disponibilidad profesional y bloqueos de agenda. | Solo entidades JPA; no tiene API, servicio de aplicación ni repositorios propios. |
| `user` | Usuarios, roles, registro de usuarios y consulta del usuario autenticado. | Implementado con controlador, servicio, repositorios, DTO y mapper. También da soporte al módulo `auth`. |

## 3. Organización interna

Los módulos con casos de uso implementados siguen, en general, esta estructura:

```text
mi_modulo/
├── controller/   # Endpoints HTTP; recibe y devuelve DTOs.
├── service/      # Interfaces de los casos de uso.
│   └── impl/     # Implementaciones, reglas de negocio y transacciones.
├── repository/   # Acceso a datos mediante Spring Data JPA.
├── entity/       # Entidades y relaciones de persistencia.
├── dto/
│   ├── request/  # Contratos de entrada con sufijo Request.
│   └── response/ # Contratos de salida con sufijo Response.
├── mapper/       # Conversión entre entidades y DTOs con MapStruct.
└── security/     # Solo cuando el dominio necesita componentes de seguridad.
```

Esta estructura es una convención, no una descripción de todos los paquetes existentes. Los módulos que hoy solo contienen entidades deberán incorporar las demás capas cuando se implementen sus casos de uso.

## 4. Flujo de una petición

El flujo habitual de una funcionalidad completa es:

```text
Cliente HTTP
    ↓
SecurityFilterChain / filtros JWT
    ↓
Controller
    ↓
Service (@Transactional)
    ↓
Repository (Spring Data JPA)
    ↓
PostgreSQL
```

Los controladores trabajan con DTOs e inyectan interfaces de servicio. Cada interfaz tiene una única implementación Spring en `service.impl`; las implementaciones delimitan las transacciones y utilizan repositorios para acceder a persistencia. MapStruct realiza la mayor parte de las conversiones entre entidades y DTOs.

Las excepciones de negocio y de recursos inexistentes se transforman en respuestas HTTP mediante `GlobalExceptionHandler`.

## 5. Dependencias entre módulos

### Situación actual

El código todavía permite dependencias directas entre dominios:

- `auth` usa componentes de `user`, incluidos `UserService`, `UserMapper` y `UserRepository`.
- `RefreshTokenService`, dentro de `auth`, usa `UserRepository`.
- `EventOccurrenceService`, dentro de `event`, usa `LocationRepository` y la entidad `Location`.
- Las entidades JPA mantienen relaciones con entidades pertenecientes a otros módulos.

Por lo tanto, no es correcto afirmar que cada servicio solo utiliza repositorios de su propio módulo. Esa regla no se cumple en el proyecto actual.

### Convención para código nuevo

- Un controlador debe delegar en un servicio de su propio módulo y no acceder directamente a repositorios.
- La lógica de negocio debe permanecer en servicios, no en controladores ni mappers.
- Debe evitarse introducir nuevas dependencias circulares entre servicios.
- Para nuevas integraciones entre módulos, se prefiere depender de un servicio o interfaz pública del módulo propietario. El acceso directo a un repositorio de otro módulo debe justificarse y mantenerse dentro de una transacción bien definida.
- Las entidades JPA no deben exponerse directamente en respuestas HTTP; la API debe usar DTOs.

Estas convenciones orientan la evolución del proyecto. No deben interpretarse como garantías de aislamiento que el código ya cumpla por completo.

## 6. Persistencia

- La base de datos principal es **PostgreSQL**.
- Las tablas, columnas, restricciones, índices y secuencias usan nombres ingleses en `snake_case` desde la migración V2.
- El acceso a datos utiliza **Spring Data JPA / Hibernate**.
- `spring.jpa.open-in-view=false`, por lo que las relaciones necesarias deben resolverse dentro de la capa de servicio y de su transacción.
- Hibernate valida o administra el esquema según `SPRING_JPA_DDL_AUTO`; en los despliegues Docker el valor predeterminado es `validate`.
- **Flyway** es el mecanismo oficial para cambiar el esquema. Las migraciones viven en `src/main/resources/db/migration` y deben ser versionadas e inmutables una vez aplicadas en un entorno compartido.
- `AuditableEntity` centraliza campos de auditoría comunes para las entidades que heredan de ella.
- `SensitiveStringCryptoConverter` cifra campos sensibles que utilizan ese convertidor mediante la clave externa `APP_FIELD_ENCRYPTION_KEY`.

## 7. Seguridad y autenticación

- Spring Security funciona sin sesión (`SessionCreationPolicy.STATELESS`).
- Los access tokens son JWT firmados mediante `jjwt`.
- Las contraseñas se almacenan con BCrypt y factor de coste 12.
- Los refresh tokens se guardan como hash SHA-256, no en texto plano.
- Se admite autenticación con credenciales locales y validación de ID tokens de Google.
- `AuthTokenFilter` procesa el JWT antes de `UsernamePasswordAuthenticationFilter`.
- `AuthRateLimitFilter` aplica una limitación en memoria a las rutas de autenticación. Al ser local al proceso, no constituye un límite distribuido entre varias instancias.
- La autorización combina reglas HTTP en `SecurityConfig` y reglas por método con `@PreAuthorize`.
- Son públicos los endpoints de autenticación, salud, OpenAPI y las consultas públicas configuradas para categorías, ocurrencias, ubicaciones y ciudades. El resto requiere autenticación salvo que se agregue una regla explícita.
- Los secretos y credenciales se obtienen de variables de entorno; no deben almacenarse en el repositorio.

## 8. API y contratos

- La API utiliza el prefijo `/api/v1`.
- Los payloads de entrada se validan con Jakarta Bean Validation y `@Valid` cuando corresponde.
- Los contratos HTTP deben definirse con DTOs. Los DTOs de entrada y salida pueden ser distintos cuando sus responsabilidades difieran.
- Los mappers actuales usan `@Mapper(componentModel = "spring")` para integrarse con la inyección de dependencias.
- Springdoc genera la especificación OpenAPI en `/v3/api-docs` y Swagger UI está disponible en `/swagger-ui.html` cuando la documentación está habilitada.
- Los nuevos endpoints deben documentar su propósito, parámetros, respuestas exitosas y errores relevantes.

## 9. Tecnologías activas

- Java 21.
- Spring Boot 3.5.14.
- Maven Wrapper.
- Spring Web MVC.
- Spring Data JPA / Hibernate.
- Spring Security.
- PostgreSQL 17 en la configuración Docker actual.
- Flyway.
- MapStruct 1.5.5.Final y Lombok.
- JJWT 0.12.6.
- Springdoc OpenAPI 2.8.16.
- Spring Boot Actuator para `health` e `info`.
- JUnit 5, Spring Security Test y H2 para pruebas.
- Docker con compilación multi-stage y Docker Compose para aplicación y base de datos.

## 10. Convenciones para una nueva funcionalidad

1. Ubicar la funcionalidad en el módulo propietario del concepto de negocio.
2. Crear una migración Flyway si cambia el esquema de base de datos.
3. Agregar o ajustar las entidades y repositorios necesarios.
4. Definir DTOs de entrada y salida con sus validaciones.
5. Crear el mapper cuando exista conversión entre DTOs y entidades.
6. Implementar el caso de uso en un servicio y definir allí sus límites transaccionales.
7. Exponer el caso de uso desde un controlador del mismo módulo.
8. Configurar autorización y documentar el endpoint en OpenAPI.
9. Agregar pruebas unitarias o de integración de acuerdo con el riesgo y las capas modificadas.
10. Actualizar este documento si cambia la responsabilidad o el nivel de implementación de un módulo.

## 11. Límites actuales

La presencia de entidades o tablas no implica que una funcionalidad esté disponible para el cliente. En particular, reservas, pagos, perfiles, contenido, favoritos y servicios individuales están modelados en persistencia, pero todavía no cuentan con una API completa en este repositorio.

Este documento debe actualizarse conforme se implementen esos casos de uso para conservar una descripción verificable del sistema.
