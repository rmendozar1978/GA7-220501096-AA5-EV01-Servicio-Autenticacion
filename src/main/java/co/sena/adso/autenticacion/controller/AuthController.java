package co.sena.adso.autenticacion.controller;

import co.sena.adso.autenticacion.dto.ApiResponse;
import co.sena.adso.autenticacion.dto.AuthRequest;
import co.sena.adso.autenticacion.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Expone los endpoints REST de registro y autenticación.
 * Los datos de entrada y salida se intercambian en formato JSON.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** POST /api/auth/registro: crea una cuenta nueva. */
    @PostMapping("/registro")
    public ResponseEntity<ApiResponse> registrar(@Valid @RequestBody AuthRequest solicitud) {
        authService.registrar(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("Registro satisfactorio."));
    }

    /** POST /api/auth/login: valida las credenciales sin revelar cuál dato falló. */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody AuthRequest solicitud) {
        if (authService.autenticar(solicitud)) {
            return ResponseEntity.ok(new ApiResponse("Autenticación satisfactoria."));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse("Error en la autenticación."));
    }
}
