package sv.edu.itca.servicedesk360.service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import sv.edu.itca.servicedesk360.dao.ClienteDAO;
import sv.edu.itca.servicedesk360.dao.DAOException;
import sv.edu.itca.servicedesk360.dao.impl.ClienteDAOImpl;
import sv.edu.itca.servicedesk360.model.Cliente;

/**
 * Guía 8 (paso 9): reglas de negocio del mantenimiento de clientes.
 *
 * <p>Antes estas reglas vivían en ClienteServlet (Guía 6). Al moverlas al
 * servicio, el Servlet y el Managed Bean de JSF aplican exactamente las
 * mismas validaciones sin duplicarlas. No contiene SQL ni conoce HTTP/JSF.
 */
public class ClienteService {

    private static final Pattern CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final ClienteDAO clienteDAO;

    public ClienteService() {
        this(new ClienteDAOImpl());
    }

    public ClienteService(ClienteDAO clienteDAO) {
        this.clienteDAO = clienteDAO;
    }

    public List<Cliente> listar() {
        return clienteDAO.listar();
    }

    public Optional<Cliente> buscarPorId(long id) {
        return clienteDAO.buscarPorId(id);
    }

    /**
     * Inserta (id null) o actualiza (id existente) aplicando las reglas
     * de la Guía 6: nombre de 3 a 120 caracteres, correo válido y único.
     *
     * @return true si fue una inserción, false si fue una actualización
     */
    public boolean guardar(Cliente cliente) throws ServiceException {
        String nombre = cliente.getNombre() == null ? "" : cliente.getNombre().trim();
        String correo = cliente.getCorreo() == null ? "" : cliente.getCorreo().trim().toLowerCase();

        if (nombre.length() < 3 || nombre.length() > 120) {
            throw new ServiceException("El nombre debe tener entre 3 y 120 caracteres.");
        }
        if (correo.isEmpty() || correo.length() > 160 || !CORREO.matcher(correo).matches()) {
            throw new ServiceException("Ingrese un correo válido (máximo 160 caracteres).");
        }
        try {
            if (clienteDAO.existeCorreo(correo, cliente.getIdCliente())) {
                throw new ServiceException("El correo ya está registrado.");
            }
            cliente.setNombre(nombre);
            cliente.setCorreo(correo);

            if (cliente.getIdCliente() == null) {
                clienteDAO.insertar(cliente);
                return true;
            }
            if (clienteDAO.buscarPorId(cliente.getIdCliente()).isEmpty()) {
                throw new ServiceException("Cliente no encontrado.");
            }
            clienteDAO.actualizar(cliente);
            return false;
        } catch (DAOException ex) {
            throw new ServiceException(ex.getMessage() + ".", ex);
        }
    }

    /** Eliminación física solo si el cliente no tiene equipos ni tickets. */
    public void eliminar(long id) throws ServiceException {
        try {
            int relaciones = clienteDAO.contarRelaciones(id);
            if (relaciones > 0) {
                throw new ServiceException("No se puede eliminar: el cliente tiene "
                        + relaciones + " registro(s) relacionado(s) (equipos o tickets). "
                        + "Desactívelo para conservar su historial.");
            }
            if (!clienteDAO.eliminar(id)) {
                throw new ServiceException("Cliente no encontrado.");
            }
        } catch (DAOException ex) {
            throw new ServiceException(ex.getMessage() + ".", ex);
        }
    }

    /** Desactivación o reactivación lógica. */
    public void cambiarEstado(long id, boolean activo) throws ServiceException {
        try {
            if (!clienteDAO.cambiarEstado(id, activo)) {
                throw new ServiceException("Cliente no encontrado.");
            }
        } catch (DAOException ex) {
            throw new ServiceException(ex.getMessage() + ".", ex);
        }
    }
}
