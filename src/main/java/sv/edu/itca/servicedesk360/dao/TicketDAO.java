package sv.edu.itca.servicedesk360.dao;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import sv.edu.itca.servicedesk360.dto.ProductividadDTO;
import sv.edu.itca.servicedesk360.dto.TicketDetalleDTO;
import sv.edu.itca.servicedesk360.model.Ticket;

/**
 * Guía 7: contrato de persistencia de tickets.
 *
 * <p>Las operaciones de escritura reciben la {@link Connection} del
 * servicio para participar en su transacción: el DAO no hace commit ni
 * rollback. Las consultas abren su propia conexión.
 *
 * <p>Se usa {@code long} (y no {@code int}) porque las claves son BIGINT.
 */
public interface TicketDAO {

    /** Inserta el ticket con estado ABIERTO y devuelve el id generado. */
    long registrar(Connection cn, Ticket ticket);

    /**
     * Listado de negocio con JOIN. {@code estado} o {@code prioridad} en
     * null significan "sin filtro".
     */
    List<TicketDetalleDTO> listarDetalle(String estado, String prioridad,
            int limite, int offset);

    /** Total de tickets con los mismos filtros (para la paginación). */
    int contar(String estado, String prioridad);

    /** Detalle de un ticket con sus datos descriptivos (Ejercicio 2). */
    Optional<TicketDetalleDTO> buscarDetalle(long idTicket);

    /**
     * Control optimista (Parte J): cambia el estado solo si el ticket
     * sigue en {@code estadoEsperado}. Devuelve false si otra operación
     * lo modificó antes o si el ticket no existe.
     */
    boolean actualizarEstado(Connection cn, long idTicket,
            String estadoEsperado, String nuevoEstado);

    /** Guía 10: ticket como entidad (ids) para cargar el diálogo de edición. */
    Optional<Ticket> buscarPorId(long idTicket);

    /**
     * Guía 10: actualiza los datos editables del ticket dentro de la
     * transacción del servicio. No modifica tickets CERRADO; devuelve false
     * si el ticket no existe o ya está cerrado.
     */
    boolean actualizar(Connection cn, Ticket ticket);

    /** Ejercicio 1: tickets por técnico y estado, de mayor a menor. */
    List<ProductividadDTO> productividadPorTecnico();
}
