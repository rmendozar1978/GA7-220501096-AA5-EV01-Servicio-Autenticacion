package co.sena.adso.autenticacion;

import co.sena.adso.autenticacion.dto.AuthRequest;
import co.sena.adso.autenticacion.model.Usuario;
import co.sena.adso.autenticacion.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Batería de pruebas automatizadas de integración para la API de autenticación.
 * Evalúa los endpoints /api/auth/registro y /api/auth/login mediante MockMvc,
 * validando códigos de estado HTTP, cuerpos JSON homogéneos (ApiResponse),
 * reglas de validación de Bean Validation y la seguridad de contraseñas con BCrypt.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ServicioAutenticacionApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Limpia la tabla de usuarios antes de cada prueba para garantizar
     * independencia total y aislamiento respecto al orden de ejecución.
     */
    @BeforeEach
    void limpiarBaseDeDatos() {
        usuarioRepository.deleteAll();
    }

    /**
     * Prueba de verificación del contexto de Spring Boot (conservada del proyecto original).
     */
    @Test
    @DisplayName("0. Verifica que el contexto de Spring Boot se inicialice correctamente")
    void contextoCargaCorrectamente() {
        assertNotNull(mockMvc, "MockMvc debe estar inicializado en el contexto.");
        assertNotNull(usuarioRepository, "El repositorio de usuarios debe estar disponible.");
        assertNotNull(passwordEncoder, "El codificador de contraseñas debe estar disponible.");
    }

    /**
     * Escenario 1: Registro correcto de un usuario nuevo.
     * Espera HTTP 201 Created y el mensaje exacto "Registro satisfactorio.".
     * Verifica que el usuario quede persistido en la base de datos.
     */
    @Test
    @DisplayName("1. Registro exitoso devuelve HTTP 201 y persiste el usuario")
    void testRegistroCorrecto() throws Exception {
        AuthRequest solicitud = new AuthRequest();
        solicitud.setUsuario("usuario.valido");
        solicitud.setContrasena("ClaveValida123!");

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje").value("Registro satisfactorio."));

        Optional<Usuario> usuarioGuardado = usuarioRepository.findByUsuario("usuario.valido");
        assertTrue(usuarioGuardado.isPresent(), "El usuario registrado debe existir en la base de datos.");
    }

    /**
     * Escenario 2: Intento de registro con un nombre de usuario ya existente.
     * Espera HTTP 409 Conflict y el mensaje exacto "El usuario ya está registrado.".
     */
    @Test
    @DisplayName("2. Registro de usuario duplicado devuelve HTTP 409 Conflict")
    void testRegistroUsuarioDuplicado() throws Exception {
        AuthRequest inicial = new AuthRequest();
        inicial.setUsuario("usuario.duplicado");
        inicial.setContrasena("ClaveInicial123!");

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inicial)))
                .andExpect(status().isCreated());

        AuthRequest repetido = new AuthRequest();
        repetido.setUsuario("usuario.duplicado");
        repetido.setContrasena("OtraClaveTotalmenteDistinta123!");

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(repetido)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("El usuario ya está registrado."));
    }

    /**
     * Escenario 3: Registro con datos inválidos (usuario en blanco o contraseña corta).
     * Espera HTTP 400 Bad Request y una respuesta JSON uniforme con el campo mensaje.
     */
    @Test
    @DisplayName("3. Registro con datos inválidos devuelve HTTP 400 y mensaje explicativo")
    void testRegistroConDatosInvalidos() throws Exception {
        // Caso A: Nombre de usuario nulo / obligatorio
        AuthRequest usuarioNulo = new AuthRequest();
        usuarioNulo.setUsuario(null);
        usuarioNulo.setContrasena("ClaveValida123!");

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioNulo)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El usuario es obligatorio."));

        // Caso B: Nombre de usuario con menos de 3 caracteres
        AuthRequest usuarioCorto = new AuthRequest();
        usuarioCorto.setUsuario("ab");
        usuarioCorto.setContrasena("ClaveValida123!");

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioCorto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El usuario debe tener entre 3 y 50 caracteres."));

        // Caso C: Contraseña con menos de 8 caracteres
        AuthRequest claveCorta = new AuthRequest();
        claveCorta.setUsuario("usuario.valido");
        claveCorta.setContrasena("corta");

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(claveCorta)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La contraseña debe tener entre 8 y 100 caracteres."));

        // Caso D: Contraseña nula / obligatoria
        AuthRequest claveNula = new AuthRequest();
        claveNula.setUsuario("usuario.valido");
        claveNula.setContrasena(null);

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(claveNula)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La contraseña es obligatoria."));
    }

    /**
     * Escenario 4: Inicio de sesión correcto con credenciales válidas.
     * Espera HTTP 200 OK y el mensaje exacto "Autenticación satisfactoria.".
     */
    @Test
    @DisplayName("4. Inicio de sesión correcto devuelve HTTP 200 y mensaje satisfactorio")
    void testInicioSesionCorrecto() throws Exception {
        AuthRequest registro = new AuthRequest();
        registro.setUsuario("usuario.login");
        registro.setContrasena("PasswordSegura2026!");

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registro)))
                .andExpect(status().isCreated());

        AuthRequest credenciales = new AuthRequest();
        credenciales.setUsuario("usuario.login");
        credenciales.setContrasena("PasswordSegura2026!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credenciales)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Autenticación satisfactoria."));
    }

    /**
     * Escenario 5: Inicio de sesión con contraseña incorrecta.
     * Espera HTTP 401 Unauthorized y el mensaje exacto "Error en la autenticación.".
     */
    @Test
    @DisplayName("5. Inicio de sesión con clave incorrecta devuelve HTTP 401")
    void testInicioSesionContrasenaIncorrecta() throws Exception {
        AuthRequest registro = new AuthRequest();
        registro.setUsuario("usuario.clave.erronea");
        registro.setContrasena("PasswordCorrecta123!");

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registro)))
                .andExpect(status().isCreated());

        AuthRequest credencialesErroneas = new AuthRequest();
        credencialesErroneas.setUsuario("usuario.clave.erronea");
        credencialesErroneas.setContrasena("PasswordCompletamenteEquivocada123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credencialesErroneas)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Error en la autenticación."));
    }

    /**
     * Escenario 6: Inicio de sesión con usuario inexistente.
     * Espera HTTP 401 Unauthorized y el mensaje exacto "Error en la autenticación.".
     */
    @Test
    @DisplayName("6. Inicio de sesión con usuario inexistente devuelve HTTP 401")
    void testInicioSesionUsuarioInexistente() throws Exception {
        AuthRequest credenciales = new AuthRequest();
        credenciales.setUsuario("usuario.no.existe");
        credenciales.setContrasena("CualquierPassword123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credenciales)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Error en la autenticación."));
    }

    /**
     * Escenario 7: Verificación del almacenamiento seguro de contraseñas mediante BCrypt.
     * Recupera el registro persistido y comprueba que la contraseña almacenada no esté
     * en texto plano y sea verificable contra el algoritmo BCrypt de Spring Security Crypto.
     */
    @Test
    @DisplayName("7. Contraseña protegida con BCrypt: no coincide con texto plano y es verificable")
    void testContrasenaAlmacenadaConBCrypt() throws Exception {
        final String nombreUsuario = "usuario.bcrypt";
        final String clavePlana = "MiClaveSuperSegura999!";

        AuthRequest registro = new AuthRequest();
        registro.setUsuario(nombreUsuario);
        registro.setContrasena(clavePlana);

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registro)))
                .andExpect(status().isCreated());

        Usuario guardado = usuarioRepository.findByUsuario(nombreUsuario)
                .orElseThrow(() -> new AssertionError("El usuario debería estar guardado en BD."));

        String hashAlmacenado = guardado.getContrasenaHash();

        assertNotNull(hashAlmacenado, "El hash de la contraseña no debe ser nulo.");
        assertNotEquals(clavePlana, hashAlmacenado, "La contraseña jamás debe guardarse en texto plano.");
        assertTrue(passwordEncoder.matches(clavePlana, hashAlmacenado),
                "El hash almacenado debe ser verificable contra la clave original mediante PasswordEncoder BCrypt.");
        assertFalse(passwordEncoder.matches("ClaveIncorrectaParaComparar123!", hashAlmacenado),
                "El PasswordEncoder debe rechazar cualquier contraseña distinta.");
    }

    /**
     * Escenario 8: Peticiones con JSON mal formado o cuerpo ausente.
     * Espera HTTP 400 Bad Request y una respuesta uniforme ApiResponse.
     */
    @Test
    @DisplayName("8. JSON mal formado o cuerpo ausente devuelve HTTP 400 Bad Request")
    void testJsonMalFormadoYCuerpoAusente() throws Exception {
        // Caso A: JSON sintácticamente incorrecto
        String jsonMalFormado = "{ usuario: 'sin_comillas_validas', contrasena: ";

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMalFormado))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El cuerpo de la solicitud es obligatorio y debe tener formato JSON válido."));

        // Caso B: Cuerpo ausente (cadena vacía)
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El cuerpo de la solicitud es obligatorio y debe tener formato JSON válido."));
    }
}
