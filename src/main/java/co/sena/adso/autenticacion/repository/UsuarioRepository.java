package co.sena.adso.autenticacion.repository;

import co.sena.adso.autenticacion.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Acceso a datos de usuarios.
 * Spring Data JPA implementa las operaciones básicas del repositorio.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsuario(String usuario);

    boolean existsByUsuario(String usuario);
}
