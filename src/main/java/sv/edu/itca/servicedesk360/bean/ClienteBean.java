package sv.edu.itca.servicedesk360.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import sv.edu.itca.servicedesk360.dao.DAOException;
import sv.edu.itca.servicedesk360.jsf.CicloVida;
import sv.edu.itca.servicedesk360.jsf.Mensajes;
import sv.edu.itca.servicedesk360.model.Cliente;
import sv.edu.itca.servicedesk360.service.ClienteService;
import sv.edu.itca.servicedesk360.service.ServiceException;

/**
 * Guía 8: Managed Bean del mantenimiento de clientes en JSF.
 *
 * <ul>
 * <li>{@code @Named("clienteBean")}: CDI lo publica para Expression
 * Language; las vistas lo usan como {@code #{clienteBean...}}.</li>
 * <li>{@code @ViewScoped}: vive mientras el usuario permanece en la misma
 * vista y hace postbacks (editar, desactivar, guardar con errores). Al
 * navegar a otra vista se crea una instancia nueva.</li>
 * <li>{@code @PostConstruct}: se ejecuta una vez, cuando CDI crea el bean
 * y ya resolvió sus dependencias; carga el listado inicial.</li>
 * <li>{@code Serializable}: el estado de vista puede guardarse en la sesión
 * y serializarse; el servicio se marca {@code transient}.</li>
 * </ul>
 *
 * <p>No contiene SQL ni mantiene Connection/ResultSet: delega en
 * {@link ClienteService}, que reutiliza ClienteDAO de la Guía 6.
 */
@Named("clienteBean")
@ViewScoped
public class ClienteBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private transient ClienteService clienteService;

    private Cliente cliente;
    private List<Cliente> clientes;
    /** Parámetro de vista ?id= de formulario.xhtml (edición). */
    private Long idEditar;

    private ClienteService servicio() {
        if (clienteService == null) {
            clienteService = new ClienteService();
        }
        return clienteService;
    }

    @PostConstruct
    public void init() {
        CicloVida.log("ClienteBean @PostConstruct (instancia " + System.identityHashCode(this) + ")");
        nuevo();
        cargarClientes();
    }

    public void cargarClientes() {
        try {
            clientes = servicio().listar();
        } catch (DAOException | IllegalStateException ex) {
            clientes = new ArrayList<>();
            Mensajes.error("No fue posible cargar los clientes. Intente más tarde.");
        }
    }

    public void nuevo() {
        cliente = new Cliente();
        cliente.setActivo(true);
    }

    // --------------------------------------------------------- navegación

    /** Navegación implícita: outcome "formulario" = formulario.xhtml. */
    public String irNuevo() {
        nuevo();
        return "formulario?faces-redirect=true";
    }

    /**
     * Paso 19. Diferencia con la guía: lista.xhtml y formulario.xhtml son
     * vistas distintas, así que con @ViewScoped el formulario recibe una
     * instancia NUEVA del bean y el cliente copiado aquí se perdería con el
     * redirect. Por eso se envía el id como parámetro de vista y el
     * formulario carga el registro con f:viewAction.
     */
    public String editar(Cliente seleccionado) {
        return "formulario?faces-redirect=true&id=" + seleccionado.getIdCliente();
    }

    /** f:viewAction de formulario.xhtml: carga el cliente a editar. */
    public String cargarParaEdicion() {
        if (idEditar == null) {
            return null;                       // alta: formulario vacío
        }
        try {
            Optional<Cliente> encontrado = servicio().buscarPorId(idEditar);
            if (encontrado.isEmpty()) {
                Mensajes.error("Cliente no encontrado.");
                Mensajes.conservarTrasRedireccion();
                return "lista?faces-redirect=true";
            }
            Cliente c = encontrado.get();
            // Copia controlada: el formulario no modifica el objeto del listado.
            cliente = new Cliente(c.getIdCliente(), c.getNombre(), c.getCorreo(), c.isActivo());
            return null;
        } catch (DAOException | IllegalStateException ex) {
            Mensajes.error("No fue posible cargar el cliente. Intente más tarde.");
            return null;
        }
    }

    public String volverListado() {
        return "lista?faces-redirect=true";
    }

    // ------------------------------------------------------------ acciones

    /** Paso 16: se ejecuta en la fase Invoke Application. */
    public String guardar() {
        CicloVida.log("ClienteBean.guardar() (INVOKE_APPLICATION) nombre=" + cliente.getNombre());
        try {
            boolean insertado = servicio().guardar(cliente);
            Mensajes.info(insertado ? "Cliente registrado correctamente."
                    : "Cliente actualizado correctamente.");
            Mensajes.conservarTrasRedireccion();
            return "lista?faces-redirect=true";     // PRG
        } catch (ServiceException ex) {
            Mensajes.error(ex.getMessage());
            return null;                            // se queda en el formulario
        } catch (DAOException | IllegalStateException ex) {
            Mensajes.error("No fue posible guardar el cliente. Intente más tarde.");
            return null;
        }
    }

    /** Paso 20: desactivación lógica y recarga del listado. */
    public void desactivar(Cliente c) {
        cambiarEstado(c, false);
    }

    public void activar(Cliente c) {
        cambiarEstado(c, true);
    }

    private void cambiarEstado(Cliente c, boolean activo) {
        try {
            servicio().cambiarEstado(c.getIdCliente(), activo);
            Mensajes.info(activo ? "Cliente activado." : "Cliente desactivado.");
        } catch (ServiceException ex) {
            Mensajes.error(ex.getMessage());
        } catch (DAOException | IllegalStateException ex) {
            Mensajes.error("No fue posible cambiar el estado del cliente.");
        }
        cargarClientes();
    }

    // ------------------------------------------------- getters y setters

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public List<Cliente> getClientes() {
        return clientes;
    }

    public Long getIdEditar() {
        return idEditar;
    }

    public void setIdEditar(Long idEditar) {
        this.idEditar = idEditar;
    }

    /** true si el formulario está editando un cliente existente. */
    public boolean isEdicion() {
        return cliente != null && cliente.getIdCliente() != null;
    }
}
