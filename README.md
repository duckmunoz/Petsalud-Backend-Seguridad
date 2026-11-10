    
# Petsalud - Backend con Seguridad JWT

## Descripción

Petsalud es una API REST desarrollada con Spring Boot para la gestión de servicios veterinarios.

El proyecto incorpora autenticación JWT, autorización por roles y mecanismos de protección de endpoints mediante Spring Security.

**Asignatura:** Desarrollo Backend II (PBY2202)  
**Profesor:** Jorge Carmona  
**Estudiante:** Patricio Muñoz

## Tecnologías utilizadas

- Java 21
- Spring Boot 3.3.6
- Spring Security
- JSON Web Token (JWT)
- Spring Data JPA / Hibernate
- MySQL 8
- Maven
- Postman

## Estructura del proyecto

- `controller/`: endpoints REST.
- `service/`: lógica de negocio.
- `repository/`: acceso a datos.
- `entity/`: entidades JPA.
- `dto/`: objetos para solicitudes y respuestas.
- `security/`: configuración de Spring Security y JWT.
- `src/test/`: pruebas automatizadas.

## Seguridad implementada

### Autenticación

El endpoint `POST /api/login` permite autenticar usuarios y obtener un token JWT.

Las contraseñas se verifican mediante BCrypt.

### Roles

El sistema contempla los siguientes roles:

- CLIENTE
- EMPLEADO
- GERENTE
- ADMIN
- VETERINARIO

La autorización se configura mediante Spring Security y verificaciones adicionales de propiedad de recursos.

### Protección de endpoints

- Los endpoints públicos permiten registro y autenticación.
- El listado y eliminación de usuarios requieren GERENTE o ADMIN.
- La eliminación de recompensas requiere GERENTE o ADMIN.
- Los usuarios CLIENTE tienen restricciones para acceder a recursos ajenos.
- Las solicitudes protegidas requieren autenticación JWT.

## Configuración local

### Requisitos

- Java 21
- MySQL 8
- Base de datos `petsalud_db`
- Variables de entorno configuradas

Crear la base de datos en MySQL:

```sql
CREATE DATABASE IF NOT EXISTS petsalud_db;
```

En PowerShell, configurar las variables necesarias:

```powershell
$env:DB_PASSWORD = Read-Host "Contraseña de MySQL"
$env:JWT_SECRET = Read-Host "Clave JWT"
```

La clave JWT debe ser suficientemente larga y generarse de forma segura. No utilizar claves de ejemplo en producción.

El archivo `application.properties` obtiene las credenciales desde variables de entorno.

### Ejecutar pruebas

Desde la raíz del proyecto:

```powershell
.\mvnw.cmd clean test
```

### Iniciar el backend

```powershell
.\mvnw.cmd spring-boot:run
```

La API estará disponible en:

`http://localhost:8080`

## Pruebas realizadas

Se utilizaron Postman y Maven para verificar el comportamiento del sistema.

| Prueba | Resultado observado |
|---|---|
| Login GERENTE válido | 200 OK |
| Login con credenciales incorrectas | 401 Unauthorized |
| Entrada SQL Injection en login | 401 Unauthorized |
| Entrada XSS en login | 401 Unauthorized |
| DELETE recompensa sin autenticación | 403 Forbidden |
| DELETE recompensa con GERENTE | 204 No Content |
| DELETE recompensa con CLIENTE | 403 Forbidden |
| Pruebas Maven | BUILD SUCCESS |

Las pruebas de SQL Injection y XSS fueron comprobaciones puntuales, no una auditoría exhaustiva.

La protección CSRF se encuentra deshabilitada en la configuración actual de la API JWT stateless. Esta decisión requiere mantener el token fuera de mecanismos de autenticación automática basados en cookies.

## Consideraciones de seguridad

- No publicar contraseñas ni claves JWT.
- No incluir tokens activos en capturas de pantalla.
- Utilizar HTTPS en entornos productivos.
- Emplear un usuario MySQL con permisos limitados en producción.
- Mantener actualizadas las dependencias.
- Ampliar las pruebas de autorización y seguridad.

## Repositorio

https://github.com/duckmunoz/Petsalud-Backend-Seguridad
