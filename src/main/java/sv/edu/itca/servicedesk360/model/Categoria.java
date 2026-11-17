package sv.edu.itca.servicedesk360.model;

import java.io.Serializable;

/**
 * Guía 7: categoría de ticket. Solo las habilitadas (activo) se ofrecen
 * en el formulario.
 */
public class Categoria implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idCategoria;
    private String nombre;
    private boolean activo = true;

    public Categoria() {
    }

    public Categoria(Long idCategoria, String nombre, boolean activo) {
        this.idCategoria = idCategoria;
        this.nombre = nombre;
        this.activo = activo;
    }

    public Long getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Long idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
