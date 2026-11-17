package sv.edu.itca.servicedesk360.jsf;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

/**
 * Guía 8, paso 17: mensajes globales de JSF (h:messages).
 *
 * <p>Una diferencia con el código de la guía: después de
 * {@code ?faces-redirect=true} el navegador hace una petición nueva y los
 * FacesMessage se pierden. {@link #conservarTrasRedireccion()} los guarda
 * en el Flash de JSF para que la vista de destino los muestre (PRG).
 */
public final class Mensajes {

    private Mensajes() {
    }

    public static void info(String texto) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, texto, null));
    }

    public static void error(String texto) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, texto, null));
    }

    /** Mantiene los mensajes de esta petición tras un redirect. */
    public static void conservarTrasRedireccion() {
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
    }
}
