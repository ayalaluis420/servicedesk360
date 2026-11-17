package sv.edu.itca.servicedesk360.model;

/**
 * Guía 7: equipo tecnológico de un cliente (tabla equipos).
 */
public class Equipo {

    private Long idEquipo;
    private long idCliente;
    private String codigoInventario;
    private String tipo;
    private String marca;
    private String modelo;
    private boolean activo = true;

    public Equipo() {
    }

    public Equipo(Long idEquipo, long idCliente, String codigoInventario, String tipo, String marca, String modelo, boolean activo) {
        this.idEquipo = idEquipo;
        this.idCliente = idCliente;
        this.codigoInventario = codigoInventario;
        this.tipo = tipo;
        this.marca = marca;
        this.modelo = modelo;
        this.activo = activo;
    }

    public Long getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(Long idEquipo) {
        this.idEquipo = idEquipo;
    }

    public long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(long idCliente) {
        this.idCliente = idCliente;
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

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
