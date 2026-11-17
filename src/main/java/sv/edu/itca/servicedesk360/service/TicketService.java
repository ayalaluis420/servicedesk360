package sv.edu.itca.servicedesk360.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import sv.edu.itca.servicedesk360.dao.CategoriaDAO;
import sv.edu.itca.servicedesk360.dao.ClienteDAO;
import sv.edu.itca.servicedesk360.dao.DAOException;
import sv.edu.itca.servicedesk360.dao.EquipoDAO;
import sv.edu.itca.servicedesk360.dao.PersonalTecnicoDAO;
import sv.edu.itca.servicedesk360.dao.SeguimientoDAO;
import sv.edu.itca.servicedesk360.dao.TicketDAO;
import sv.edu.itca.servicedesk360.dao.impl.CategoriaDAOImpl;
import sv.edu.itca.servicedesk360.dao.impl.ClienteDAOImpl;
import sv.edu.itca.servicedesk360.dao.impl.EquipoDAOImpl;
import sv.edu.itca.servicedesk360.dao.impl.PersonalTecnicoDAOImpl;
import sv.edu.itca.servicedesk360.dao.impl.SeguimientoDAOImpl;
import sv.edu.itca.servicedesk360.dao.impl.TicketDAOImpl;
import sv.edu.itca.servicedesk360.dto.TicketDetalleDTO;
import sv.edu.itca.servicedesk360.model.Cliente;
import sv.edu.itca.servicedesk360.model.PersonalTecnico;
import sv.edu.itca.servicedesk360.model.Seguimiento;
import sv.edu.itca.servicedesk360.model.Ticket;
import sv.edu.itca.servicedesk360.persistence.ConexionBD;

/**
 * Guía 7: servicio de negocio del proceso de tickets.
 *
 * <p>Valida las reglas (sección X.1) y coordina las transacciones que
 * involucran varios DAO: obtiene una sola Connection, desactiva
 * autoCommit, invoca los DAO con esa misma conexión y decide commit o
 * rollback. No contiene SQL ni conoce HTTP.
 */
public class TicketService {

    private static final Logger LOG = Logger.getLogger(TicketService.class.getName());

    /** Catálogo de prioridades (igual que el CHECK de MySQL y el enum PrioridadTicket). */
    public static final List<String> PRIORIDADES = List.of("BAJA", "MEDIA", "ALTA", "CRITICA");

    /** Catálogo de estados (igual que el CHECK de MySQL y el enum EstadoTicket). */
    public static final List<String> ESTADOS
            = List.of("ABIERTO", "ASIGNADO", "EN_PROCESO", "RESUELTO", "CERRADO");

    /**
     * Ejercicio 3: transiciones permitidas. CERRADO es final: no puede
     * volver a ABIERTO. RESUELTO puede reabrirse a EN_PROCESO si la
     * solución no funcionó.
     */
    private static final Map<String, Set<String>> TRANSICIONES = new LinkedHashMap<>();

    static {
        TRANSICIONES.put("ABIERTO", Set.of("ASIGNADO", "EN_PROCESO"));
        TRANSICIONES.put("ASIGNADO", Set.of("EN_PROCESO"));
        TRANSICIONES.put("EN_PROCESO", Set.of("RESUELTO", "CERRADO"));
        TRANSICIONES.put("RESUELTO", Set.of("CERRADO", "EN_PROCESO"));
        TRANSICIONES.put("CERRADO", Set.of());
    }

    private final TicketDAO ticketDAO;
    private final SeguimientoDAO seguimientoDAO;
    private final EquipoDAO equipoDAO;
    private final ClienteDAO clienteDAO;
    private final CategoriaDAO categoriaDAO;
    private final PersonalTecnicoDAO tecnicoDAO;

    public TicketService() {
        this(new TicketDAOImpl(), new SeguimientoDAOImpl(), new EquipoDAOImpl(),
                new ClienteDAOImpl(), new CategoriaDAOImpl(), new PersonalTecnicoDAOImpl());
    }

    /**
     * Constructor con inyección de dependencias: permite probar el
     * rollback sustituyendo un DAO, sin código de prueba en producción.
     */
    public TicketService(TicketDAO ticketDAO, SeguimientoDAO seguimientoDAO,
            EquipoDAO equipoDAO, ClienteDAO clienteDAO,
            CategoriaDAO categoriaDAO, PersonalTecnicoDAO tecnicoDAO) {
        this.ticketDAO = ticketDAO;
        this.seguimientoDAO = seguimientoDAO;
        this.equipoDAO = equipoDAO;
        this.clienteDAO = clienteDAO;
        this.categoriaDAO = categoriaDAO;
        this.tecnicoDAO = tecnicoDAO;
    }

    // ------------------------------------------------ registro (Parte F)

    /**
     * Registra el ticket y su seguimiento inicial como una sola unidad de
     * trabajo: se confirman juntos o no se guarda ninguno.
     *
     * @return id generado del ticket
     */
    public long registrarTicketConSeguimiento(Ticket ticket, String detalleInicial)
            throws ServiceException {

        validar(ticket, detalleInicial);
        validarRelaciones(ticket);

        try (Connection cn = ConexionBD.obtenerConexion()) {
            cn.setAutoCommit(false);
            try {
                long idTicket = ticketDAO.registrar(cn, ticket);

                Seguimiento s = new Seguimiento();
                s.setIdTicket(idTicket);
                s.setDetalle(detalleInicial.trim());
                if (seguimientoDAO.registrar(cn, s) != 1) {
                    throw new ServiceException("No se registró el seguimiento inicial.");
                }

                cn.commit();          // único punto de confirmación
                return idTicket;
            } catch (ServiceException | RuntimeException ex) {
                cn.rollback();
                ticket.setIdTicket(null);
                LOG.log(Level.WARNING, "Registro de ticket revertido", ex);
                throw new ServiceException("No se registró el ticket: ocurrió un error y la "
                        + "operación fue revertida completa (no se guardaron datos parciales).", ex);
            } finally {
                cn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            LOG.log(Level.WARNING, "Fallo de conexión al registrar ticket", ex);
            throw new ServiceException("No fue posible completar el proceso. Intente más tarde.", ex);
        }
    }

    /** Paso 16: validaciones de entrada. */
    private void validar(Ticket t, String detalle) throws ServiceException {
        if (t.getIdCliente() <= 0) {
            throw new ServiceException("Seleccione un cliente.");
        }
        if (t.getIdEquipo() == null || t.getIdEquipo() <= 0) {
            throw new ServiceException("Seleccione un equipo.");
        }
        if (t.getIdTecnico() == null || t.getIdTecnico() <= 0) {
            throw new ServiceException("Seleccione un técnico.");
        }
        if (t.getIdCategoria() <= 0) {
            throw new ServiceException("Seleccione una categoría.");
        }
        String titulo = t.getTitulo() == null ? "" : t.getTitulo().trim();
        if (titulo.length() < 5 || titulo.length() > 120) {
            throw new ServiceException("El título debe contener entre 5 y 120 caracteres.");
        }
        t.setTitulo(titulo);
        String descripcion = t.getDescripcion() == null ? "" : t.getDescripcion().trim();
        if (descripcion.length() < 10 || descripcion.length() > 1000) {
            throw new ServiceException("La descripción debe contener entre 10 y 1000 caracteres.");
        }
        t.setDescripcion(descripcion);
        if (!PRIORIDADES.contains(t.getPrioridad())) {
            throw new ServiceException("Prioridad no válida.");
        }
        if (detalle == null || detalle.isBlank()) {
            throw new ServiceException("El detalle del seguimiento inicial es obligatorio.");
        }
    }

    /** Reglas X.1 que dependen de la base de datos. */
    private void validarRelaciones(Ticket t) throws ServiceException {
        try {
            Optional<Cliente> cliente = clienteDAO.buscarPorId(t.getIdCliente());
            if (cliente.isEmpty() || !cliente.get().isActivo()) {
                throw new ServiceException("El cliente no existe o está inactivo.");
            }
            // La pertenencia se valida aquí aunque el formulario solo muestre
            // los equipos del cliente: no se confía en lo que envía el navegador.
            if (!equipoDAO.perteneceACliente(t.getIdEquipo(), t.getIdCliente())) {
                throw new ServiceException("El equipo seleccionado no pertenece al cliente.");
            }
            if (!categoriaDAO.estaActiva(t.getIdCategoria())) {
                throw new ServiceException("La categoría no existe o está deshabilitada.");
            }
            Optional<PersonalTecnico> tecnico = tecnicoDAO.buscarPorId(t.getIdTecnico());
            if (tecnico.isEmpty() || !tecnico.get().isActivo()) {
                throw new ServiceException("El técnico no existe o está inactivo.");
            }
            // Regla del modelo de la Guía 3 (Tecnico.puedeAtender).
            if ("CRITICA".equals(t.getPrioridad()) && tecnico.get().getNivelSoporte() != 3) {
                throw new ServiceException(
                        "Un ticket de prioridad CRITICA requiere un técnico de nivel 3.");
            }
        } catch (DAOException ex) {
            throw new ServiceException("No fue posible validar los datos. Intente más tarde.", ex);
        }
    }

    // ------------------------------- cambio de estado (Partes J y Ej. 3)

    /** Estados a los que puede pasar un ticket desde {@code estadoActual}. */
    public Set<String> transicionesDesde(String estadoActual) {
        return TRANSICIONES.getOrDefault(estadoActual, Collections.emptySet());
    }

    /**
     * Cambia el estado con control optimista y deja constancia en el
     * historial, ambas cosas en una sola transacción.
     *
     * @param estadoEsperado estado que el usuario tenía en pantalla
     */
    public void cambiarEstado(long idTicket, String estadoEsperado, String nuevoEstado,
            String comentario) throws ServiceException {

        if (!ESTADOS.contains(nuevoEstado) || !ESTADOS.contains(estadoEsperado)) {
            throw new ServiceException("Estado no válido.");
        }
        if (!transicionesDesde(estadoEsperado).contains(nuevoEstado)) {
            throw new ServiceException("Transición no permitida: " + estadoEsperado
                    + " → " + nuevoEstado + ".");
        }

        try (Connection cn = ConexionBD.obtenerConexion()) {
            cn.setAutoCommit(false);
            try {
                if (!ticketDAO.actualizarEstado(cn, idTicket, estadoEsperado, nuevoEstado)) {
                    cn.rollback();
                    Optional<TicketDetalleDTO> actual = ticketDAO.buscarDetalle(idTicket);
                    if (actual.isEmpty()) {
                        throw new ServiceException("Ticket no encontrado.");
                    }
                    throw new ServiceException("El ticket fue modificado por otra operación "
                            + "(estado actual: " + actual.get().getEstado()
                            + "). Recargue la pantalla e intente de nuevo.");
                }
                Seguimiento s = new Seguimiento();
                s.setIdTicket(idTicket);
                String texto = "Cambio de estado: " + estadoEsperado + " → " + nuevoEstado;
                if (comentario != null && !comentario.isBlank()) {
                    texto += ". " + comentario.trim();
                }
                s.setDetalle(texto.length() > 800 ? texto.substring(0, 800) : texto);
                seguimientoDAO.registrar(cn, s);
                cn.commit();
            } catch (RuntimeException ex) {
                cn.rollback();
                throw new ServiceException("No se cambió el estado: la operación fue revertida.", ex);
            } finally {
                cn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            LOG.log(Level.WARNING, "Fallo de conexión al cambiar estado", ex);
            throw new ServiceException("No fue posible completar el proceso. Intente más tarde.", ex);
        }
    }
}
