package sv.edu.itca.servicedesk360.dto;

import java.io.Serializable;

/**
 * Guía 7: fila del listado de equipos con el nombre del cliente (JOIN).
 */
public class EquipoDetalleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private long idEquipo;
    private String codigoInventario;
    private String tipo;
    private String marca;
    private String modelo;
    private String cliente;
    private boolean activo;
    private int tickets;

    public EquipoDetalleDTO() {
    }

    public long getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(long idEquipo) {
        this.idEquipo = idEquipo;
    }

    public String getCodigoInventario() {
        return codigoInventario;
    }

    public void setCodigoInventario(String codigoInventario) {
        this.codigoInventario = codigoInventario;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public int getTickets() {
        return tickets;
    }

    public void setTickets(int tickets) {
        this.tickets = tickets;
    }
}
