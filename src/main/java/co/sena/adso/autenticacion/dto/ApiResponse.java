package co.sena.adso.autenticacion.dto;

/**
 * Respuesta JSON uniforme para informar el resultado de la operación.
 * No contiene contraseñas ni hashes.
 */
public class ApiResponse {

    private String mensaje;

    public ApiResponse() {
    }

    public ApiResponse(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
