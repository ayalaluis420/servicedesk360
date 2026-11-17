package sv.edu.itca.servicedesk360.dao;

import java.sql.Connection;
import java.util.List;
import sv.edu.itca.servicedesk360.model.Seguimiento;

/**
 * Guía 7: contrato de persistencia del historial de tickets.
 * {@link #registrar} usa la conexión de la transacción del servicio.
 */
public interface SeguimientoDAO {

    /** Inserta el seguimiento y devuelve las filas afectadas (1). */
    int registrar(Connection cn, Seguimiento seguimiento);

    /** Historial del ticket, del más antiguo al más reciente. */
    List<Seguimiento> listarPorTicket(long idTicket);
}
