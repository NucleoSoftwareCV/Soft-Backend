# Guía de Arquitectura y Buenas Prácticas - Oona Backend

Este documento sirve como guía técnica para todos los colaboradores del proyecto backend de **Oona**. Su objetivo es mantener la consistencia en el código, facilitar la integración de nuevos desarrolladores y asegurar que el sistema escale de manera ordenada.

---

## 1. Patrón de Arquitectura: *Package-by-Feature* (Paquetes por Funcionalidad)

El proyecto está organizado bajo el enfoque de **Package-by-Feature** (Paquetes por Funcionalidad / Módulos). En lugar de agrupar las clases por su rol técnico (todos los controladores juntos, todos los servicios juntos), el código se divide según el dominio del negocio al que pertenece.

### Estructura de Módulos Actuales
Todos los módulos se encuentran bajo el paquete principal `com.hean.consigueventas.oonabe`:

*   **`admin`**: Funcionalidades y reportería de administración interna.
*   **`auth`**: Autenticación tradicional (Registro, Login, Refresh Token, Logout) y filtros de seguridad.
*   **`booking`**: Gestión de agendas y reservas de citas.
*   **`catalog`**: Catálogo general de ofertas y servicios de bienestar.
*   **`category`**: Categorías de especialidades y experiencias (ej. Yoga, Psicoterapia).
*   **`common`**: Configuraciones globales del sistema (Seguridad, OpenAPI, Inicialización de datos).
*   **`content`**: Contenido estático del marketplace (banners, textos informativos).
*   **`event`**: Gestión de experiencias colectivas y eventos calendarizados.
*   **`interaction`**: Mensajería, chats, comentarios y valoraciones entre usuarios y especialistas.
*   **`location`**: Direcciones, zonas y ubicaciones físicas.
*   **`payment`**: Pasarela de pagos, transacciones y facturación.
*   **`profile`**: Información del perfil público y privado de usuarios y especialistas.
*   **`service`**: Definición de servicios/especialidades que ofrece cada especialista individualmente.
*   **`user`**: Gestión de usuarios base, roles y permisos de acceso.

---

## 2. Estructura Interna de un Módulo (Capas)

Cada módulo de negocio debe estructurarse internamente siguiendo el patrón de capas tradicional:

```text
com.hean.consigueventas.oonabe.mi_modulo/
│
├── controller/   # Expone los endpoints HTTP. Recibe DTOs y devuelve DTOs.
├── service/      # Contiene la lógica de negocio.
├── repository/   # Interfaz JPA para comunicación con la base de datos.
├── entity/       # Mapeo de la base de datos (clases anotadas con @Entity).
├── dto/          # Objetos de transferencia de datos de entrada/salida (Request/Response).
└── mapper/       # Interfaces MapStruct para traducción automática (Entity <-> DTO).
```

---

## 3. Reglas de Comunicación entre Módulos (Obligatorio)

Para evitar que el código se convierta en un laberinto difícil de mantener y depurar, se establecen las siguientes reglas de comunicación:

### Regla 1: Aislamiento de Controladores
*   **Un Controlador (`Controller`) solo puede comunicarse con el Servicio (`Service`) de su propio módulo.**
*   *Ejemplo incorrecto:* `EventController` inyecta y llama a `UserService` o `UserRepository`.
*   *Ejemplo correcto:* `EventController` llama a `EventService`. Si este último requiere información de usuarios, se comunica a nivel de capa de servicio.

### Regla 2: Inyección de Repositorios
*   **Un Servicio (`Service`) solo puede inyectar el Repositorio (`Repository`) de su propio módulo.**
*   *Ejemplo incorrecto:* `EventServiceImpl` inyecta a `UserRepository` para buscar un usuario.
*   *Ejemplo correcto:* `EventServiceImpl` inyecta a `UserService` (el servicio) y le solicita el usuario necesario.

### Regla 3: Evitar Dependencias Circulares
*   Queda estrictamente prohibido que el Servicio A dependa del Servicio B, y que a su vez el Servicio B dependa del Servicio A.
*   **Solución:** Si dos servicios se necesitan mutuamente, esa lógica compartida debe extraerse a un tercer servicio intermedio, o se debe evaluar si los módulos están mal delimitados.

### Regla 4: Comunicación externa mediante DTOs
*   Los controladores nunca deben exponer ni retornar Entidades de JPA (`@Entity`) directamente hacia el cliente (Frontend/Mobile).
*   Toda información de entrada y salida debe ser mapeada a objetos **DTO** usando **MapStruct** para evitar exponer datos sensibles de la base de datos y prevenir errores de carga diferida (Lazy Initialization Exceptions).

---

## 4. Stack de Tecnologías y Funcionalidades Activas

Al programar o agregar nuevas características, asegúrate de utilizar las herramientas ya configuradas en el proyecto:

*   **Seguridad y Autenticación:** Se utiliza Spring Security con autenticación basada en tokens **JWT** stateless (mediante la librería `jjwt`). Las contraseñas en base de datos deben almacenarse cifradas utilizando **BCrypt** (fuerza 12).
*   **Mapeo de Objetos (Mappers):** Se utiliza **MapStruct** en combinación con **Lombok**. Recuerda definir tus interfaces de mapeo con la anotación `@Mapper(componentModel = "spring")`.
*   **Migraciones de Base de Datos:** Toda modificación en la estructura de la base de datos (creación de tablas, nuevas columnas) debe realizarse a través de scripts de migración de **Flyway** ubicados en `src/main/resources/db/migration`.
*   **Documentación de API:** La API se autodocumenta utilizando **Swagger / OpenAPI**. Utiliza anotaciones como `@Operation` y `@ApiResponse` en tus controladores para mantener la documentación actualizada en `/swagger-ui.html`.

---

## 5. Flujo de Trabajo para Crear una Nueva Funcionalidad

1.  **Migración de DB:** Si requiere cambios en la base de datos, crea el script de Flyway correspondiente.
2.  **Entidad y Repositorio:** Crea la entidad JPA y su repositorio asociado en el módulo correspondiente.
3.  **DTOs y Mapper:** Define los DTOs de petición y respuesta, y crea la interfaz de MapStruct para su conversión.
4.  **Servicio:** Implementa la lógica de negocio respetando las reglas de comunicación del punto 3.
5.  **Controlador:** Expone el endpoint HTTP usando los DTOs y añade la documentación de OpenAPI.
