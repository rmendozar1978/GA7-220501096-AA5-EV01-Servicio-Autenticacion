package co.sena.adso.autenticacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación Spring Boot.
 * Desde esta clase se inicia el servidor HTTP y los componentes de la API.
 */
@SpringBootApplication
public class ServicioAutenticacionApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioAutenticacionApplication.class, args);
    }
}
