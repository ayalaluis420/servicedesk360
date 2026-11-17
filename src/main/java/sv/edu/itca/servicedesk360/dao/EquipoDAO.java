package sv.edu.itca.servicedesk360.dao;

import java.util.List;
import java.util.Optional;
import sv.edu.itca.servicedesk360.dto.EquipoDetalleDTO;
import sv.edu.itca.servicedesk360.model.Equipo;

/**
 * Guía 7: contrato de persistencia de equipos (entidad relacionada con
 * clientes). Incluye las operaciones que necesita el proceso de tickets
 * (paso 7) y el CRUD de equipos.
 */
public interface EquipoDAO {

    /** Paso 7: equipos activos de un cliente (catálogo del formulario). */
    List<Equipo> listarPorCliente(long idCliente);

    /** Paso 7 y 10: el equipo existe, está activo y pertenece al cliente. */
    boolean perteneceACliente(long idEquipo, long idCliente);

    // ----- CRUD de equipos -----

    /** Listado con el nombre del cliente y la cantidad de tickets (JOIN). */
    List<EquipoDetalleDTO> listarDetalle();

    Optional<Equipo> buscarPorId(long id);

    /** El código de inventario ya pertenece a otro equipo. */
    boolean existeCodigo(String codigo, Long excluirId);

    long insertar(Equipo equipo);

    boolean actualizar(Equipo equipo);

    /** Tickets que referencian al equipo; con más de 0 no se elimina. */
    int contarTickets(long id);

    boolean eliminar(long id);

    boolean cambiarEstado(long id, boolean activo);
}
