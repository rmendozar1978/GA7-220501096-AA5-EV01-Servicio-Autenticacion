package co.sena.adso.autenticacion.controller;

import co.sena.adso.autenticacion.dto.ApiResponse;
import co.sena.adso.autenticacion.service.AuthService;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Controlador centralizado para el manejo homogéneo de excepciones en la API REST.
 * Captura y transforma errores de validación, duplicidad y persistencia en respuestas
 * HTTP con formato ApiResponse, evitando la exposición de trazas o datos sensibles.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Captura las excepciones de validación disparadas por @Valid en AuthRequest.
     * Devuelve HTTP 400 Bad Request y el mensaje de error de la restricción infringida.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> manejarValidacion(MethodArgumentNotValidException ex) {
        FieldError error = ex.getBindingResult().getFieldError();
        String mensaje = (error != null && error.getDefaultMessage() != null)
                ? error.getDefaultMessage()
                : "Datos de solicitud inválidos.";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(mensaje));
    }

    /**
     * Captura solicitudes cuyo cuerpo JSON sea nulo, esté vacío o presente sintaxis inválida.
     * Devuelve HTTP 400 Bad Request con un mensaje comprensible.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> manejarCuerpoInvalido(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse("El cuerpo de la solicitud es obligatorio y debe tener formato JSON válido."));
    }

    /**
     * Captura la excepción de negocio cuando se intenta registrar un usuario ya existente.
     * Devuelve HTTP 409 Conflict con el mensaje esperado: "El usuario ya está registrado."
     */
    @ExceptionHandler(AuthService.UsuarioDuplicadoException.class)
    public ResponseEntity<ApiResponse> manejarUsuarioDuplicado(AuthService.UsuarioDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse(ex.getMessage()));
    }

    /**
     * Captura violaciones de integridad de datos lanzadas por la capa de persistencia.
     * Se gestiona fuera de la transacción para no dejarla en estado fallido.
     * Si corresponde a la restricción única del usuario (colisión concurrente), devuelve HTTP 409 Conflict.
     * Si corresponde a otro fallo de persistencia, devuelve HTTP 500 sin exponer detalles internos.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse> manejarIntegridadDatos(DataIntegrityViolationException ex) {
        if (esErrorPorUsuarioDuplicado(ex)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse("El usuario ya está registrado."));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("Error interno al procesar los datos de la solicitud."));
    }

    /**
     * Evalúa si una DataIntegrityViolationException fue ocasionada por la restricción
     * única de la tabla de usuarios (uk_usuarios_usuario o violación de unicidad de usuario).
     */
    private boolean esErrorPorUsuarioDuplicado(DataIntegrityViolationException ex) {
        // Comprobación de la excepción de restricción de Hibernate
        if (ex.getCause() instanceof ConstraintViolationException) {
            ConstraintViolationException cve = (ConstraintViolationException) ex.getCause();
            if (cve.getConstraintName() != null &&
                    cve.getConstraintName().equalsIgnoreCase("uk_usuarios_usuario")) {
                return true;
            }
        }

        // Comprobación de mensajes detallados y código de estado SQL en el proveedor JDBC
        Throwable causaRaiz = ex.getMostSpecificCause();
        String texto = (causaRaiz != null && causaRaiz.getMessage() != null)
                ? causaRaiz.getMessage().toLowerCase()
                : (ex.getMessage() != null ? ex.getMessage().toLowerCase() : "");

        return texto.contains("uk_usuarios_usuario")
                || (texto.contains("usuarios") && texto.contains("usuario"))
                || (texto.contains("23505") && texto.contains("usuario"));
    }
}
