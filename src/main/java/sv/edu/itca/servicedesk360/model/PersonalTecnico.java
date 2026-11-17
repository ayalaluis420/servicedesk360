package sv.edu.itca.servicedesk360.model;

import java.io.Serializable;

/**
 * Guía 6, adaptación a una entidad propia del proyecto: ficha del
 * personal técnico que se administra en el catálogo (tabla tecnicos).
 *
 * <p>Se llama PersonalTecnico para no confundirla con {@link Tecnico},
 * la clase de dominio de la Guía 3 que hereda de Usuario y exige un id
 * positivo desde su construcción. Esta entidad, en cambio, tiene id
 * {@code null} hasta que MySQL lo genera.
 */
public class PersonalTecnico implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idTecnico;
    private String nombre;
    private String correo;
    private String especialidad;
    private int nivelSoporte = 1;
    private boolean activo = true;

    public PersonalTecnico() {
    }

    public PersonalTecnico(Long idTecnico, String nombre, String correo,
            String especialidad, int nivelSoporte, boolean activo) {
        this.idTecnico = idTecnico;
        this.nombre = nombre;
        this.correo = correo;
        this.especialidad = especialidad;
        this.nivelSoporte = nivelSoporte;
        this.activo = activo;
    }

    public Long getIdTecnico() {
        return idTecnico;
    }

    public void setIdTecnico(Long idTecnico) {
        this.idTecnico = idTecnico;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public int getNivelSoporte() {
        return nivelSoporte;
    }

    public void setNivelSoporte(int nivelSoporte) {
        this.nivelSoporte = nivelSoporte;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "PersonalTecnico{id=" + idTecnico + ", nombre=" + nombre
                + ", correo=" + correo + ", nivel=" + nivelSoporte
                + ", activo=" + activo + "}";
    }
}
