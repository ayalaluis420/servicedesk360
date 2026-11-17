package sv.edu.itca.servicedesk360.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Guía 7: entidad Ticket. Solo contiene las columnas propias de la tabla
 * tickets (ids de las entidades relacionadas, no sus nombres). Los datos
 * descriptivos para la pantalla viajan en {@link sv.edu.itca.servicedesk360.dto.TicketDetalleDTO}.
 */
public class Ticket implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idTicket;
    private long idCliente;
    private Long idEquipo;
    private Long idTecnico;
    private long idCategoria;
    private String titulo;
    private String descripcion;
    private String prioridad;
    private String estado = "ABIERTO";
    private LocalDateTime fechaCreacion;

    public Ticket() {
    }

    public Ticket(Long idTicket, long idCliente, Long idEquipo, Long idTecnico, long idCategoria, String titulo, String descripcion, String prioridad, String estado, LocalDateTime fechaCreacion) {
        this.idTicket = idTicket;
        this.idCliente = idCliente;
        this.idEquipo = idEquipo;
        this.idTecnico = idTecnico;
        this.idCategoria = idCategoria;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.prioridad = prioridad;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
    }

    public Long getIdTicket() {
        return idTicket;
    }

    public void setIdTicket(Long idTicket) {
        this.idTicket = idTicket;
    }

    public long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(long idCliente) {
        this.idCliente = idCliente;
    }

    public Long getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(Long idEquipo) {
        this.idEquipo = idEquipo;
    }

    public Long getIdTecnico() {
        return idTecnico;
    }

    public void setIdTecnico(Long idTecnico) {
        this.idTecnico = idTecnico;
    }

    public long getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(long idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
