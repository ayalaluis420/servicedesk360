package sv.edu.itca.servicedesk360.bean;

import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import sv.edu.itca.servicedesk360.model.Usuario;

/**
 * Guía 8, Ejercicio 3: bean de sesión mínimo con los datos del usuario
 * autenticado (los coloca AccesoServlet en la HttpSession).
 *
 * <p>Solo guarda información transversal a todas las pantallas. El
 * formulario de clientes NO va aquí: quedaría vivo durante toda la sesión,
 * se mezclaría entre pestañas y consumiría memoria por usuario.
 */
@Named("sesionBean")
@SessionScoped
public class SesionBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Usuario usuario() {
        FacesContext fc = FacesContext.getCurrentInstance();
        HttpSession sesion = (HttpSession) fc.getExternalContext().getSession(false);
        Object u = sesion == null ? null : sesion.getAttribute("usuarioAutenticado");
        return u instanceof Usuario ? (Usuario) u : null;
    }

    public boolean isAutenticado() {
        return usuario() != null;
    }

    public String getNombre() {
        Usuario u = usuario();
        return u == null ? "" : u.getNombreCompleto();
    }

    public String getRol() {
        Usuario u = usuario();
        return u == null ? "" : u.getRol().name();
    }
}
