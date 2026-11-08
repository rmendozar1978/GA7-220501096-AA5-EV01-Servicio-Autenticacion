# Servicio web de registro y autenticación
**Evidencia:** GA7-220501096-AA5-EV01  
**Aprendiz:** Ronald Fabian Mendoza Rodriguez  
**Ficha:** ADSO 3235898  
**Programa:** Tecnólogo en Análisis y Desarrollo de Software — SENA

## 1. Descripción
API REST desarrollada con Java 8 y Spring Boot 2.7.18. Permite registrar usuarios e iniciar sesión mediante solicitudes HTTP que reciben y devuelven JSON. Los comentarios del código explican la responsabilidad de las clases y operaciones principales.

## 2. Requisitos
- JDK 8 o compatible.
- Apache Maven 3.6 o superior.
- Git.
- Cliente HTTP como Postman, Insomnia o `curl`.

## 3. Ejecutar el proyecto
Desde una terminal, ubíquese en la carpeta donde está `pom.xml` y ejecute:

```bash
mvn clean test
mvn spring-boot:run
```

La API quedará disponible en `http://localhost:8080`.

La base de datos H2 se configura en modo archivo local en la carpeta `./data`. No se incluyen credenciales de acceso a servicios externos.

## 4. Endpoints

### Registrar usuario
`POST /api/auth/registro`

Cuerpo JSON:
```json
{
  "usuario": "ronald",
  "contrasena": "ClaveSegura123!"
}
```

Resultado esperado: HTTP `201 Created`.
```json
{
  "mensaje": "Registro satisfactorio."
}
```

Si el usuario ya existe: HTTP `409 Conflict`.

### Iniciar sesión
`POST /api/auth/login`

Cuerpo JSON:
```json
{
  "usuario": "ronald",
  "contrasena": "ClaveSegura123!"
}
```

Credenciales correctas: HTTP `200 OK`.
```json
{
  "mensaje": "Autenticación satisfactoria."
}
```

Credenciales incorrectas: HTTP `401 Unauthorized`.
```json
{
  "mensaje": "Error en la autenticación."
}
```

Si faltan campos o no cumplen las validaciones, la API devuelve HTTP `400 Bad Request`.

## 5. Prueba rápida con curl
Con la aplicación ejecutándose, abra otra terminal:

```bash
curl -i -X POST http://localhost:8080/api/auth/registro \
  -H "Content-Type: application/json" \
  -d "{\"usuario\":\"ronald\",\"contrasena\":\"ClaveSegura123!\"}"
```

```bash
curl -i -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"usuario\":\"ronald\",\"contrasena\":\"ClaveSegura123!\"}"
```

Para comprobar el rechazo, repita el login con una contraseña incorrecta.

## 6. Seguridad y alcance
- Las contraseñas se guardan mediante hash BCrypt.
- La API no devuelve el hash ni la contraseña.
- Las credenciales deben transmitirse mediante HTTPS al desplegar en un servidor.
- Este ejemplo no incorpora tokens JWT, recuperación de contraseña, bloqueo por intentos ni administración de roles; no debe considerarse un sistema de identidad listo para producción.
- La base H2 local es para fines de demostración académica.

## 7. Control de versiones con Git
Desde la carpeta raíz del proyecto:

```bash
git init
git add .
git commit -m "Inicializa API REST de registro y autenticacion"
git branch -M main
```

Cree un repositorio vacío en GitHub con el nombre `GA7-220501096-AA5-EV01-Servicio-Autenticacion` y luego ejecute los comandos que GitHub indique, por ejemplo:

```bash
git remote add origin https://github.com/USUARIO/GA7-220501096-AA5-EV01-Servicio-Autenticacion.git
git push -u origin main
```

Reemplace `USUARIO` por su nombre de usuario real de GitHub. No se afirma que el proyecto ya esté publicado: debe crear el repositorio y ejecutar el `push` para que el enlace sea válido.

## 8. Estructura
```text
servicio-autenticacion/
├── pom.xml
├── README.md
├── .gitignore
├── Repositorio_GitHub.txt
└── src/
    ├── main/
    │   ├── java/co/sena/adso/autenticacion/
    │   │   ├── ServicioAutenticacionApplication.java
    │   │   ├── config/SecurityConfig.java
    │   │   ├── controller/AuthController.java
    │   │   ├── dto/AuthRequest.java
    │   │   ├── dto/ApiResponse.java
    │   │   ├── model/Usuario.java
    │   │   ├── repository/UsuarioRepository.java
    │   │   └── service/AuthService.java
    │   └── resources/application.properties
    └── test/java/co/sena/adso/autenticacion/
        └── ServicioAutenticacionApplicationTests.java
```
