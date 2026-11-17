package sv.edu.itca.servicedesk360.model;

import java.time.LocalDateTime;

/**
 * Guía 7: entrada del historial de un ticket (tabla seguimientos).
 */
public class Seguimiento {

    private Long idSeguimiento;
    private long idTicket;
    private String detalle;
    private LocalDateTime fechaRegistro;

    public Seguimiento() {
    }

    public Seguimiento(Long idSeguimiento, long idTicket, String detalle, LocalDateTime fechaRegistro) {
        this.idSeguimiento = idSeguimiento;
        this.idTicket = idTicket;
        this.detalle = detalle;
        this.fechaRegistro = fechaRegistro;
    }

    public Long getIdSeguimiento() {
        return idSeguimiento;
    }

    public void setIdSeguimiento(Long idSeguimiento) {
        this.idSeguimiento = idSeguimiento;
    }

    public long getIdTicket() {
        return idTicket;
    }

    public void setIdTicket(long idTicket) {
        this.idTicket = idTicket;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
