package sv.edu.itca.servicedesk360.dao;

/**
 * Excepción de la capa DAO.
 *
 * <p>Envuelve la {@link java.sql.SQLException} original (que queda como
 * causa para el log del servidor) y expone a las capas superiores un
 * mensaje funcional, sin SQL, nombres de tablas ni datos de conexión.
 */
public class DAOException extends RuntimeException {

    public DAOException(String message, Throwable cause) {
        super(message, cause);
    }

    public DAOException(String message) {
        super(message);
    }
}
