package sv.edu.itca.servicedesk360.persistence;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Paso 10 de la Guía 5: prueba de conexión y metadatos.
 *
 * <p>También sirve para los pasos 20 y 21 (credenciales inválidas y
 * servidor detenido): en esos casos muestra un mensaje controlado y
 * deja el detalle técnico en el log, sin imprimir la contraseña.
 */
public class PruebaConexion {

    public static void main(String[] args) {
        System.out.println("Configuración: " + ConexionBD.rutaConfiguracion());
        try (Connection cn = ConexionBD.abrir()) {
            DatabaseMetaData md = cn.getMetaData();
            System.out.println("Conexión válida: " + cn.isValid(3));
            System.out.println("URL: " + ConexionBD.urlSinParametros());
            System.out.println("Usuario: " + md.getUserName());
            System.out.println("DBMS: " + md.getDatabaseProductName());
            System.out.println("Versión: " + md.getDatabaseProductVersion());
            System.out.println("Driver: " + md.getDriverName()
                    + " " + md.getDriverVersion());
        } catch (SQLException ex) {
            String mensaje = DiagnosticoBD.registrar("Prueba de conexión", ex);
            System.out.println("Conexión fallida (controlado): " + mensaje);
            System.out.println("SQLState: " + ex.getSQLState()
                    + " | código: " + ex.getErrorCode());
            System.exit(1);
        } catch (IllegalStateException ex) {
            System.out.println("Configuración inválida: " + ex.getMessage());
            System.exit(2);
        }
    }
}
