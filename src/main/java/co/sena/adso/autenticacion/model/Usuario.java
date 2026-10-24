package co.sena.adso.autenticacion.model;

import javax.persistence.*;

 /**
  * Entidad persistente de usuario.
  * La columna contrasenaHash guarda únicamente el hash BCrypt.
  */
@Entity
@Table(name = "usuarios", uniqueConstraints = {
        @UniqueConstraint(name = "uk_usuarios_usuario", columnNames = "usuario")
})
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String usuario;

    @Column(name = "contrasena_hash", nullable = false, length = 100)
    private String contrasenaHash;

    public Usuario() {
    }

    public Usuario(String usuario, String contrasenaHash) {
        this.usuario = usuario;
        this.contrasenaHash = contrasenaHash;
    }

    public Long getId() {
        return id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public void setContrasenaHash(String contrasenaHash) {
        this.contrasenaHash = contrasenaHash;
    }
}
