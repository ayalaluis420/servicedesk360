package sv.edu.itca.servicedesk360.jsf;

import java.util.Locale;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 * Convertidor (base de la Guía 9): estandariza códigos en mayúsculas sin
 * espacios externos (por ejemplo, el código de inventario de un equipo).
 */
@FacesConverter("mayusculas")
public class MayusculasConverter implements Converter<String> {

    @Override
    public String getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, String value) {
        return value == null ? "" : value;
    }
}
