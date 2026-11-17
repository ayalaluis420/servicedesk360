package sv.edu.itca.servicedesk360.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import sv.edu.itca.servicedesk360.dao.CategoriaDAO;
import sv.edu.itca.servicedesk360.dao.ClienteDAO;
import sv.edu.itca.servicedesk360.dao.DAOException;
import sv.edu.itca.servicedesk360.dao.PersonalTecnicoDAO;
import sv.edu.itca.servicedesk360.dao.SeguimientoDAO;
import sv.edu.itca.servicedesk360.dao.TicketDAO;
import sv.edu.itca.servicedesk360.dao.impl.CategoriaDAOImpl;
import sv.edu.itca.servicedesk360.dao.impl.ClienteDAOImpl;
import sv.edu.itca.servicedesk360.dao.impl.PersonalTecnicoDAOImpl;
import sv.edu.itca.servicedesk360.dao.impl.SeguimientoDAOImpl;
import sv.edu.itca.servicedesk360.dao.impl.TicketDAOImpl;
import sv.edu.itca.servicedesk360.dto.TicketDetalleDTO;
import sv.edu.itca.servicedesk360.model.Categoria;
import sv.edu.itca.servicedesk360.model.Cliente;
import sv.edu.itca.servicedesk360.model.Equipo;
import sv.edu.itca.servicedesk360.model.PersonalTecnico;
import sv.edu.itca.servicedesk360.model.Seguimiento;
import sv.edu.itca.servicedesk360.model.Ticket;
import sv.edu.itca.servicedesk360.service.EquipoService;
import sv.edu.itca.servicedesk360.service.ServiceException;
import sv.edu.itca.servicedesk360.service.TicketService;

/**
 * Guía 10: Managed Bean del módulo de Tickets con PrimeFaces y AJAX.
 *
 * <p>Coordina la vista: carga la tabla y los catálogos, prepara el
 * diálogo, atiende eventos AJAX (cliente → equipos, selección de fila,
 * filtros) y traduce los resultados del servicio a mensajes. Las reglas
 * y transacciones siguen en {@link TicketService}; el SQL, en los DAO.
 * No hay SQL en este bean ni en el XHTML.
 */
@Named("ticketBean")
@ViewScoped
public class TicketBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Máximo de filas que se traen a memoria. La tabla pagina y filtra por
     * columna en memoria; los filtros externos (estado y prioridad) se
     * resuelven en MySQL. Para miles de tickets se usaría LazyDataModel
     * con LIMIT/OFFSET (paso 23).
     */
    private static final int MAX_FILAS = 500;

    // Dependencias (transient: se recrean si el bean se deserializa)
    private transient TicketService ticketService;
    private transient EquipoService equipoService;
    private transient TicketDAO ticketDAO;
    private transient SeguimientoDAO seguimientoDAO;
    private transient ClienteDAO clienteDAO;
    private transient CategoriaDAO categoriaDAO;
    private transient PersonalTecnicoDAO tecnicoDAO;

    // Tabla
    private List<TicketDetalleDTO> tickets = new ArrayList<>();
    private List<TicketDetalleDTO> ticketsFiltrados;
    private String filtroEstado;
    private String filtroPrioridad;
    private int abiertos;

    // Selección y detalle
    private TicketDetalleDTO seleccionado;
    private List<Seguimiento> seguimientos = new ArrayList<>();

    // Diálogo de registro/edición
    private Ticket ticket = new Ticket();
    private String detalleInicial;
    private boolean edicion;
    private List<Equipo> equiposCliente = new ArrayList<>();

    // Catálogos
    private List<Cliente> clientes = new ArrayList<>();
    private List<Categoria> categorias = new ArrayList<>();
    private List<PersonalTecnico> tecnicos = new ArrayList<>();

    // ------------------------------------------------------- dependencias

    private TicketService servicio() {
        if (ticketService == null) {
            ticketService = new TicketService();
        }
        return ticketService;
    }

    private EquipoService equipos() {
        if (equipoService == null) {
            equipoService = new EquipoService();
        }
        return equipoService;
    }

    private TicketDAO ticketDAO() {
        if (ticketDAO == null) {
            ticketDAO = new TicketDAOImpl();
        }
        return ticketDAO;
    }

    private SeguimientoDAO seguimientoDAO() {
        if (seguimientoDAO == null) {
            seguimientoDAO = new SeguimientoDAOImpl();
        }
        return seguimientoDAO;
    }

    // ---------------------------------------------------- carga de datos

    @PostConstruct
    public void init() {
        try {
            if (clienteDAO == null) {
                clienteDAO = new ClienteDAOImpl();
                categoriaDAO = new CategoriaDAOImpl();
                tecnicoDAO = new PersonalTecnicoDAOImpl();
            }
            clientes = clienteDAO.listarActivos();
            categorias = categoriaDAO.listarActivas();
            tecnicos = tecnicoDAO.listarActivos();
        } catch (DAOException | IllegalStateException ex) {
            error("No fue posible cargar los catálogos", "Intente más tarde.");
        }
        cargar();
        prepararNuevo();
    }

    /** Recarga la tabla con los filtros externos (consulta en MySQL). */
    public void cargar() {
        try {
            tickets = ticketDAO().listarDetalle(vacioANull(filtroEstado), vacioANull(filtroPrioridad), MAX_FILAS, 0);
            abiertos = ticketDAO().contar("ABIERTO", null);
            ticketsFiltrados = null;   // la tabla vuelve a aplicar sus filtros de columna
            if (seleccionado != null) {
                refrescarSeleccionado(seleccionado.getIdTicket());
            }
        } catch (DAOException | IllegalStateException ex) {
            tickets = new ArrayList<>();
            error("No fue posible cargar los tickets", "Intente más tarde.");
        }
    }

    /** Parte I: listener de los filtros externos (p:ajax en estado y prioridad). */
    public void aplicarFiltros() {
        cargar();
    }

    /** Ejercicio 17: limpia filtros externos (la vista limpia los de columna). */
    public void limpiarFiltros() {
        filtroEstado = null;
        filtroPrioridad = null;
        cargar();
    }

    // ------------------------------------------------ selección y detalle

    /** Parte J: evento rowSelect de la tabla. Solo coordina: la consulta está en el DAO. */
    public void alSeleccionar(SelectEvent<TicketDetalleDTO> evento) {
        refrescarSeleccionado(evento.getObject().getIdTicket());
    }

    /** Ejercicio 18: botón "Ver" del diálogo de detalle de solo lectura. */
    public void verDetalle(TicketDetalleDTO t) {
        refrescarSeleccionado(t.getIdTicket());
    }

    private void refrescarSeleccionado(long idTicket) {
        try {
            Optional<TicketDetalleDTO> actual = ticketDAO().buscarDetalle(idTicket);
            seleccionado = actual.orElse(null);
            seguimientos = actual.isPresent() ? seguimientoDAO().listarPorTicket(idTicket) : new ArrayList<>();
        } catch (DAOException | IllegalStateException ex) {
            error("No fue posible cargar el detalle", "Intente más tarde.");
        }
    }

    // ---------------------------------------------- diálogo nuevo/editar

    public void prepararNuevo() {
        ticket = new Ticket();
        detalleInicial = null;
        edicion = false;
        equiposCliente = new ArrayList<>();
    }

    /**
     * Paso 8: prepara el diálogo con el ticket de la fila. Si el ticket
     * está CERRADO no se abre el diálogo (callback "abrir" = false).
     */
    public void prepararEdicion(TicketDetalleDTO fila) {
        boolean abrir = false;
        try {
            Optional<Ticket> t = servicio().buscarTicket(fila.getIdTicket());
            if (t.isEmpty()) {
                error("Ticket no encontrado", "Recargue la pantalla.");
            } else if ("CERRADO".equals(t.get().getEstado())) {
                error("No se puede editar", "El ticket #" + fila.getIdTicket() + " está CERRADO.");
            } else {
                ticket = t.get();
                detalleInicial = null;
                edicion = true;
                equiposCliente = new ArrayList<>(equipos().listarPorCliente(ticket.getIdCliente()));
                abrir = true;
            }
        } catch (DAOException | IllegalStateException ex) {
            error("No fue posible cargar el ticket", "Intente más tarde.");
        }
        PrimeFaces.current().ajax().addCallbackParam("abrir", abrir);
    }

    /** Paso 14: evento AJAX cliente → equipos. */
    public void cargarEquipos() {
        try {
            equiposCliente = ticket.getIdCliente() > 0
                    ? new ArrayList<>(equipos().listarPorCliente(ticket.getIdCliente()))
                    : new ArrayList<>();
        } catch (DAOException | IllegalStateException ex) {
            equiposCliente = new ArrayList<>();
            error("No fue posible cargar los equipos", "Intente más tarde.");
        }
        ticket.setIdEquipo(null);
    }

    /**
     * Guardar del diálogo (process="@form"). Si el servicio rechaza la
     * operación se marca validationFailed para que el diálogo siga
     * abierto con los datos escritos (paso 35: no se pierde el contexto).
     */
    public void guardar() {
        try {
            if (edicion) {
                servicio().actualizarTicket(ticket);
                info("Ticket actualizado", "Ticket #" + ticket.getIdTicket() + " actualizado.");
            } else {
                long id = servicio().registrarTicketConSeguimiento(ticket, detalleInicial);
                info("Ticket registrado", "Ticket #" + id + " registrado con su seguimiento inicial.");
            }
            cargar();
            prepararNuevo();
        } catch (ServiceException ex) {
            error("No fue posible guardar", ex.getMessage());
            FacesContext.getCurrentInstance().validationFailed();
        }
    }

    // -------------------------------------------- cambio de estado (Parte G)

    public void iniciarAtencion(TicketDetalleDTO t) {
        cambiarEstado(t, "EN_PROCESO");
    }

    public void resolver(TicketDetalleDTO t) {
        cambiarEstado(t, "RESUELTO");
    }

    public void cerrar(TicketDetalleDTO t) {
        cambiarEstado(t, "CERRADO");
    }

    /**
     * Paso 19: usa el estado que se ve en la fila como estado esperado
     * (control optimista de la Guía 7).
     */
    private void cambiarEstado(TicketDetalleDTO t, String nuevo) {
        try {
            servicio().cambiarEstado(t.getIdTicket(), t.getEstado(), nuevo, "Cambio desde la vista PrimeFaces.");
            info("Estado actualizado", "Ticket #" + t.getIdTicket() + ": " + t.getEstado() + " → " + nuevo + ".");
        } catch (ServiceException ex) {
            error("No se cambió el estado", ex.getMessage());
        }
        cargar();
    }

    /** true si la transición desde el estado del ticket está permitida. */
    public boolean puede(TicketDetalleDTO t, String nuevo) {
        return t != null && servicio().transicionesDesde(t.getEstado()).contains(nuevo);
    }

    // ---------------------------------------------------- presentación

    /** Ejercicio 16: severidad de p:tag según la prioridad. */
    public String severidad(String prioridad) {
        if (prioridad == null) {
            return "secondary";
        }
        switch (prioridad) {
            case "CRITICA": return "danger";
            case "ALTA": return "warning";
            case "MEDIA": return "info";
            default: return "success";
        }
    }

    public String severidadEstado(String estado) {
        if (estado == null) {
            return "secondary";
        }
        switch (estado) {
            case "ABIERTO": return "info";
            case "ASIGNADO": return "secondary";
            case "EN_PROCESO": return "warning";
            case "RESUELTO": return "success";
            default: return "contrast";
        }
    }

    private static void info(String resumen, String detalle) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, resumen, detalle));
    }

    private static void error(String resumen, String detalle) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, resumen, detalle));
    }

    private static String vacioANull(String v) {
        return v == null || v.isBlank() ? null : v;
    }

    // ------------------------------------------------ getters y setters

    public List<TicketDetalleDTO> getTickets() {
        return tickets;
    }

    public List<TicketDetalleDTO> getTicketsFiltrados() {
        return ticketsFiltrados;
    }

    public void setTicketsFiltrados(List<TicketDetalleDTO> ticketsFiltrados) {
        this.ticketsFiltrados = ticketsFiltrados;
    }

    public String getFiltroEstado() {
        return filtroEstado;
    }

    public void setFiltroEstado(String filtroEstado) {
        this.filtroEstado = filtroEstado;
    }

    public String getFiltroPrioridad() {
        return filtroPrioridad;
    }

    public void setFiltroPrioridad(String filtroPrioridad) {
        this.filtroPrioridad = filtroPrioridad;
    }

    public int getAbiertos() {
        return abiertos;
    }

    public TicketDetalleDTO getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(TicketDetalleDTO seleccionado) {
        this.seleccionado = seleccionado;
    }

    public List<Seguimiento> getSeguimientos() {
        return seguimientos;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public String getDetalleInicial() {
        return detalleInicial;
    }

    public void setDetalleInicial(String detalleInicial) {
        this.detalleInicial = detalleInicial;
    }

    public boolean isEdicion() {
        return edicion;
    }

    public List<Equipo> getEquiposCliente() {
        return equiposCliente;
    }

    public List<Cliente> getClientes() {
        return clientes;
    }

    public List<Categoria> getCategorias() {
        return categorias;
    }

    public List<PersonalTecnico> getTecnicos() {
        return tecnicos;
    }

    public List<String> getEstados() {
        return TicketService.ESTADOS;
    }

    public List<String> getPrioridades() {
        return TicketService.PRIORIDADES;
    }
}
