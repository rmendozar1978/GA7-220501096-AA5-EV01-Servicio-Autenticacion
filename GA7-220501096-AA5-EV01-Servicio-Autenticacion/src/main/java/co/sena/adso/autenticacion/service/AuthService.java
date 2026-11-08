package co.sena.adso.autenticacion.service;

import co.sena.adso.autenticacion.dto.AuthRequest;
import co.sena.adso.autenticacion.model.Usuario;
import co.sena.adso.autenticacion.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contiene las reglas de negocio para registrar usuarios y autenticar.
 * El controlador delega en esta clase para separar HTTP de la lógica.
 */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registra el usuario si el nombre aún no está en uso.
     * La contraseña se transforma a BCrypt antes de persistirse.
     */
    @Transactional
    public void registrar(AuthRequest solicitud) {
        String nombreUsuario = solicitud.getUsuario().trim();

        if (usuarioRepository.existsByUsuario(nombreUsuario)) {
            throw new UsuarioDuplicadoException();
        }

        String hash = passwordEncoder.encode(solicitud.getContrasena());
        Usuario usuario = new Usuario(nombreUsuario, hash);
        usuarioRepository.save(usuario);
    }

    /**
     * Devuelve true solamente si el usuario existe y BCrypt confirma
     * que la contraseña recibida coincide con el hash almacenado.
     */
    @Transactional(readOnly = true)
    public boolean autenticar(AuthRequest solicitud) {
        return usuarioRepository.findByUsuario(solicitud.getUsuario().trim())
                .map(usuario -> passwordEncoder.matches(
                        solicitud.getContrasena(), usuario.getContrasenaHash()))
                .orElse(false);
    }

    /** Excepción de negocio para informar que el nombre ya está registrado. */
    public static class UsuarioDuplicadoException extends RuntimeException {
        public UsuarioDuplicadoException() {
            super("El usuario ya está registrado.");
        }
    }
}
