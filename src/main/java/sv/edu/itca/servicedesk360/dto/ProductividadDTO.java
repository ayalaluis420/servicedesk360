package sv.edu.itca.servicedesk360.dto;

/**
 * Guía 7, Ejercicio 1: cantidad de tickets por técnico y estado.
 */
public class ProductividadDTO {

    private String tecnico;
    private String estado;
    private int total;

    public ProductividadDTO() {
    }

    public ProductividadDTO(String tecnico, String estado, int total) {
        this.tecnico = tecnico;
        this.estado = estado;
        this.total = total;
    }

    public String getTecnico() {
        return tecnico;
    }

    public void setTecnico(String tecnico) {
        this.tecnico = tecnico;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
