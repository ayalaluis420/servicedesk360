package sv.edu.itca.servicedesk360.jsf;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 * Validador personalizado (base de la Guía 9): el código de inventario
 * debe tener el formato EQ-0000 a EQ-99999 o similar: 2 a 5 letras, un
 * guion y de 3 a 6 caracteres alfanuméricos (por ejemplo, EQ-0010).
 * Se ejecuta después del convertidor de mayúsculas.
 */
@FacesValidator("codigoInventario")
public class CodigoInventarioValidator implements Validator<String> {

    @Override
    public void validate(FacesContext context, UIComponent component, String value) {
        if (value == null) {
            return; // de los vacíos se encarga required
        }
        if (!value.matches("^[A-Z]{2,5}-[A-Z0-9]{3,6}$")) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Código inválido",
                    "Use el formato LETRAS-CÓDIGO, por ejemplo EQ-0010."));
        }
    }
}
