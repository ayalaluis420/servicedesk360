package sv.edu.itca.servicedesk360.persistence;

import java.time.LocalDateTime;

/**
 * Fila de lectura de tickets con JOIN (Parte E de la Guía 5).
 * Se refactorizará en la Guía 6.
 */
public final class TicketVista {

    private final long idTicket;
    private final String titulo;
    private final String prioridad;
    private final String estado;
    private final LocalDateTime fechaCreacion;
    private final String cliente;
    private final String categoria;

    public TicketVista(long idTicket, String titulo, String prioridad,
            String estado, LocalDateTime fechaCreacion,
            String cliente, String categoria) {
        this.idTicket = idTicket;
        this.titulo = titulo;
        this.prioridad = prioridad;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.cliente = cliente;
        this.categoria = categoria;
    }

    public long getIdTicket() {
        return idTicket;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public String getEstado() {
        return estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public String getCliente() {
        return cliente;
    }

    public String getCategoria() {
        return categoria;
    }

    @Override
    public String toString() {
        return "#" + idTicket + " [" + prioridad + "/" + estado + "] "
                + titulo + " - " + cliente + " (" + categoria + ")";
    }
}
