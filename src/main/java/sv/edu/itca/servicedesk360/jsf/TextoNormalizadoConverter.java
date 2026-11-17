package sv.edu.itca.servicedesk360.jsf;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 * Convertidor (base de la Guía 9): normaliza texto libre antes de que
 * llegue al bean. Quita espacios al inicio y al final y reduce los
 * espacios repetidos a uno ("  Impresora   no  imprime " → "Impresora no imprime").
 * Una cadena vacía se convierte en null, para que required la detecte.
 */
@FacesConverter("textoNormalizado")
public class TextoNormalizadoConverter implements Converter<String> {

    @Override
    public String getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null) {
            return null;
        }
        String limpio = value.trim().replaceAll("\\s+", " ");
        return limpio.isEmpty() ? null : limpio;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, String value) {
        return value == null ? "" : value;
    }
}
