# UD03-RVL — Usuarios y roles en Spring Boot

Proyecto de **Rafael Vega Librero**. Adapta el CRUD de `User` de UD02 a Spring Boot y añade un CRUD de `Role` con relación **N:M**. Mantiene Java 21, Spring Boot 3.5.6, Thymeleaf, `JdbcTemplate`, validación y el mismo CSS del proyecto anterior de Comunidades y Provincias.

## Abrir en IntelliJ IDEA

Abre esta carpeta o su `pom.xml` como proyecto Maven. Selecciona **JDK 21**, espera a que Maven importe las dependencias y conserva UTF-8 para los recursos. Hay una configuración compartida de ejecución llamada **Usuarios y Roles**.

## Arranque

La aplicación utiliza una base de datos independiente: **users-roles-rvl**, MariaDB en **localhost:3311** y la web en **http://localhost:8081**.

Antes del primer arranque, copia `.env.example` a `.env` y completa `DB_PASSWORD` y `DB_ROOT_PASSWORD`. El archivo `.env` queda excluido de Git; el repositorio solo contiene la plantilla de configuración.

1. Arranca Docker Desktop o el servicio Docker de tu equipo.
2. Desde la raíz del proyecto ejecuta:

   ```bash
   docker compose up -d --wait
   ./mvnw spring-boot:run
   ```

   Si usas el servicio Docker de Linux y el contexto activo apunta a Docker Desktop cerrado, arranca la base de datos con `docker --context default compose up -d --wait`.

3. Abre **http://localhost:8081**. También puedes ejecutar `TicketLoggerApplication` desde IntelliJ, con la raíz del proyecto como directorio de trabajo.

`schema.sql` crea las tablas; `data.sql` inserta usuarios de ejemplo (`admin`, `jdoe`, `maria` y `blockeduser`) y los roles `ADMIN`, `USER` y `EDITOR`. Las asignaciones que ya tengan esos usuarios se conservan al reiniciar.

## Uso

- **Usuarios:** listado, detalle, alta, edición y eliminación. Conserva los campos del CRUD original: estados de la cuenta, cambio y caducidad de contraseña e intentos fallidos.
- En alta y edición puedes marcar **uno o varios roles**. Es obligatorio seleccionar al menos uno. Las casillas aparecen seleccionadas al editar y se conservan si hay un error de validación.
- Las contraseñas se guardan con BCrypt. Al crear un usuario son obligatorias y deben tener al menos 8 caracteres y un máximo de 72 bytes. Al editar, dejar el campo vacío conserva la contraseña actual. El hash nunca aparece en las páginas.
- La caducidad se calcula automáticamente **tres meses después** de la fecha del último cambio de contraseña, como en el proyecto de UD02.
- **Roles:** listado, detalle, alta, edición y eliminación. Los formularios y el detalle solo manejan `id` y `name`; no contienen selección de usuarios.
- Las asignaciones se cambian **solo desde Usuarios**. Si un rol está asignado, su eliminación se bloquea con un mensaje. Primero hay que quitarlo o sustituirlo en los usuarios afectados.
- Los errores de campos obligatorios, duplicados, fechas, números y roles inexistentes se muestran en el formulario. Hay selector de idioma español/inglés.

## Estructura

- `entities`: `User` y `Role`.
- `forms`: `UserForm`, separado de la entidad para no exponer hashes ni permitir modificar campos calculados.
- `daos`: interfaces e implementaciones JDBC, siguiendo la estructura del CRUD de `Region`.
- `services`: reglas y transacciones. El usuario y sus roles se guardan juntos; un fallo revierte toda la operación.
- `controllers`: controladores Spring MVC y tratamiento de errores de base de datos.
- `templates/fragments`: cabecera, pie, head y scripts compartidos.
- `templates/views/user` y `templates/views/role`: listados, formularios y detalles.
- `users`, `roles` y `user_roles`: la tabla intermedia usa clave primaria compuesta para evitar asignaciones duplicadas. Al borrar un usuario se eliminan sus vínculos; la clave foránea impide borrar un rol asignado.

## Comprobaciones

```bash
./mvnw test
./mvnw clean verify
```

Las pruebas de integración utilizan H2 en memoria en modo MariaDB; no necesitan Docker ni modifican la base de datos local. Cubren las pantallas, los CRUD, roles compartidos, cambios y limpieza de asignaciones, validaciones, contraseñas, idiomas y la imposibilidad de asignar usuarios desde Roles.
