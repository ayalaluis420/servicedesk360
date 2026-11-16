package sv.edu.itca.servicedesk360.persistence;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Traduce una {@link SQLException} en dos salidas distintas:
 * <ul>
 * <li>un registro técnico (SQLState, código del proveedor y mensaje del
 * motor) para la bitácora del servidor;</li>
 * <li>un mensaje funcional y controlado para el usuario final, sin
 * contraseñas, URL ni trazas.</li>
 * </ul>
 */
public final class DiagnosticoBD {

    private static final Logger LOG
            = Logger.getLogger(DiagnosticoBD.class.getName());

    private DiagnosticoBD() {
    }

    /**
     * Registra el detalle técnico y devuelve el mensaje para el usuario.
     *
     * @param operacion descripción breve de lo que se intentaba hacer
     * @param ex excepción recibida
     * @return mensaje seguro para mostrar en la interfaz
     */
    public static String registrar(String operacion, SQLException ex) {
        LOG.log(Level.WARNING, "[BD] {0} | SQLState={1} | codigo={2} | {3}",
                new Object[]{operacion, ex.getSQLState(),
                    String.valueOf(ex.getErrorCode()), ex.getMessage()});
        return mensajeUsuario(ex);
    }

    /** Mensaje funcional según la clase de SQLState. */
    public static String mensajeUsuario(SQLException ex) {
        String estado = ex.getSQLState() == null ? "" : ex.getSQLState();
        if (estado.startsWith("08")) {
            return "El servicio de datos no está disponible. Intente más tarde.";
        }
        if (estado.startsWith("28") || ex.getErrorCode() == 1045) {
            return "La aplicación no pudo autenticarse con la base de datos. "
                    + "Contacte al administrador.";
        }
        if (estado.startsWith("23") || ex.getErrorCode() == 3819) {
            return "Los datos no cumplen las reglas de integridad del sistema.";
        }
        return "Ocurrió un error al procesar la información. Intente nuevamente.";
    }
}
