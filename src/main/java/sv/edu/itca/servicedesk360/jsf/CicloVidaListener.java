package sv.edu.itca.servicedesk360.jsf;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

/**
 * Guía 8, Parte I: registra el inicio y fin de cada fase del ciclo de vida
 * (Restore View, Apply Request Values, Process Validations, Update Model
 * Values, Invoke Application y Render Response). Registrado en
 * faces-config.xml; solo escribe si LOG_CICLO_VIDA = true.
 */
public class CicloVidaListener implements PhaseListener {

    private static final long serialVersionUID = 1L;

    @Override
    public void beforePhase(PhaseEvent event) {
        CicloVida.log("inicia " + event.getPhaseId() + " | vista: "
                + (event.getFacesContext().getViewRoot() == null ? "(sin construir)"
                : event.getFacesContext().getViewRoot().getViewId()));
    }

    @Override
    public void afterPhase(PhaseEvent event) {
        CicloVida.log("termina " + event.getPhaseId());
    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }
}
