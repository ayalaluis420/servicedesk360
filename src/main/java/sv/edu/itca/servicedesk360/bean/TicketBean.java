package sv.edu.itca.servicedesk360.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import sv.edu.itca.servicedesk360.dao.DAOException;
import sv.edu.itca.servicedesk360.dao.TicketDAO;
import sv.edu.itca.servicedesk360.dao.impl.TicketDAOImpl;
import sv.edu.itca.servicedesk360.dto.TicketDetalleDTO;
import sv.edu.itca.servicedesk360.jsf.Mensajes;
import sv.edu.itca.servicedesk360.service.TicketService;

/**
 * Guía 8, Parte K: vista JSF de solo lectura de tickets. Reutiliza la
 * consulta JOIN y TicketDetalleDTO de la Guía 7 sin reescribir SQL.
 * Ejercicio 2: filtros de estado y prioridad con una acción normal del
 * formulario (sin AJAX).
 */
@Named("ticketBean")
@ViewScoped
public class TicketBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int LIMITE = 20;

    private transient TicketDAO ticketDAO;

    private List<TicketDetalleDTO> tickets;
    private String estado;
    private String prioridad;
    private int total;

    private TicketDAO dao() {
        if (ticketDAO == null) {
            ticketDAO = new TicketDAOImpl();
        }
        return ticketDAO;
    }

    @PostConstruct
    public void init() {
        cargar();
    }

    public void cargar() {
        String e = vacioANull(estado);
        String p = vacioANull(prioridad);
        try {
            tickets = dao().listarDetalle(e, p, LIMITE, 0);
            total = dao().contar(e, p);
        } catch (DAOException | IllegalStateException ex) {
            tickets = new ArrayList<>();
            total = 0;
            Mensajes.error("No fue posible cargar los tickets. Intente más tarde.");
        }
    }

    /** Ejercicio 2: acción del botón Filtrar (postback a la misma vista). */
    public String filtrar() {
        cargar();
        return null;
    }

    public String limpiar() {
        estado = null;
        prioridad = null;
        cargar();
        return null;
    }

    private static String vacioANull(String v) {
        return v == null || v.isBlank() ? null : v;
    }

    public List<String> getEstados() {
        return TicketService.ESTADOS;
    }

    public List<String> getPrioridades() {
        return TicketService.PRIORIDADES;
    }

    public List<TicketDetalleDTO> getTickets() {
        return tickets;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public int getTotal() {
        return total;
    }

    public int getLimite() {
        return LIMITE;
    }
}
