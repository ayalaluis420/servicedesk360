package sv.edu.itca.servicedesk360.service;

/**
 * Guía 7: error de una regla de negocio o de un proceso de la capa de
 * servicio. Su mensaje es funcional y puede mostrarse al usuario; la
 * causa técnica (si existe) queda como {@code cause} para el log.
 */
public class ServiceException extends Exception {

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
