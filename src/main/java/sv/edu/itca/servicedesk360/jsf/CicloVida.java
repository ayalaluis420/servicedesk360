package sv.edu.itca.servicedesk360.jsf;

import java.util.logging.Logger;
import javax.faces.context.FacesContext;

/**
 * Guía 8, Parte I: registro opcional del ciclo de vida en el log del
 * servidor. Se activa con el context-param servicedesk360.LOG_CICLO_VIDA
 * = true en web.xml; en false (por defecto) no escribe nada.
 */
public final class CicloVida {

    private static final Logger LOG = Logger.getLogger("servicedesk360.ciclo-vida");

    private CicloVida() {
    }

    public static boolean activo() {
        FacesContext fc = FacesContext.getCurrentInstance();
        return fc != null && "true".equalsIgnoreCase(
                fc.getExternalContext().getInitParameter("servicedesk360.LOG_CICLO_VIDA"));
    }

    public static void log(String mensaje) {
        if (activo()) {
            LOG.info("[CICLO] " + mensaje);
        }
    }
}
