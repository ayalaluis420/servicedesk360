package sv.edu.itca.servicedesk360.jsf;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.annotation.FacesConfig;

/**
 * Guía 8: activa el modo JSF 2.3 con CDI (Weld). Sin esta anotación,
 * Mojarra 2.3 funciona en modo de compatibilidad y no integra por completo
 * los beans @Named con Expression Language.
 */
@FacesConfig(version = FacesConfig.Version.JSF_2_3)
@ApplicationScoped
public class JsfConfig {
}
