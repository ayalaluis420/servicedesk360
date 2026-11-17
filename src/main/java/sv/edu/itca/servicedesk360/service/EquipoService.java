package sv.edu.itca.servicedesk360.service;

import java.util.List;
import java.util.Optional;
import sv.edu.itca.servicedesk360.dao.ClienteDAO;
import sv.edu.itca.servicedesk360.dao.DAOException;
import sv.edu.itca.servicedesk360.dao.EquipoDAO;
import sv.edu.itca.servicedesk360.dao.impl.ClienteDAOImpl;
import sv.edu.itca.servicedesk360.dao.impl.EquipoDAOImpl;
import sv.edu.itca.servicedesk360.dto.EquipoDetalleDTO;
import sv.edu.itca.servicedesk360.model.Cliente;
import sv.edu.itca.servicedesk360.model.Equipo;

/**
 * Guía 10: reglas de negocio de equipos. Antes estaban en EquipoServlet
 * (Guía 7); ahora las comparten el Servlet y EquipoBean (PrimeFaces).
 */
public class EquipoService {

    private final EquipoDAO equipoDAO;
    private final ClienteDAO clienteDAO;

    public EquipoService() {
        this(new EquipoDAOImpl(), new ClienteDAOImpl());
    }

    public EquipoService(EquipoDAO equipoDAO, ClienteDAO clienteDAO) {
        this.equipoDAO = equipoDAO;
        this.clienteDAO = clienteDAO;
    }

    public List<EquipoDetalleDTO> listarDetalle() {
        return equipoDAO.listarDetalle();
    }

    public List<Equipo> listarPorCliente(long idCliente) {
        return equipoDAO.listarPorCliente(idCliente);
    }

    public Optional<Equipo> buscarPorId(long id) {
        return equipoDAO.buscarPorId(id);
    }

    /**
     * Inserta (id null) o actualiza. Reglas: cliente existente (y activo
     * para equipos nuevos), código de 3 a 40 caracteres y único, tipo de 2 a
     * 80 caracteres, marca y modelo de hasta 80. Al actualizar se conserva
     * el estado activo actual.
     *
     * @return true si fue una inserción
     */
    public boolean guardar(Equipo e) throws ServiceException {
        String codigo = e.getCodigoInventario() == null ? "" : e.getCodigoInventario().trim().toUpperCase();
        String tipo = e.getTipo() == null ? "" : e.getTipo().trim();
        String marca = e.getMarca() == null ? "" : e.getMarca().trim();
        String modelo = e.getModelo() == null ? "" : e.getModelo().trim();
        try {
            Optional<Cliente> cliente = e.getIdCliente() <= 0
                    ? Optional.empty() : clienteDAO.buscarPorId(e.getIdCliente());
            if (cliente.isEmpty()) {
                throw new ServiceException("Seleccione un cliente válido.");
            }
            if (e.getIdEquipo() == null && !cliente.get().isActivo()) {
                throw new ServiceException("No se pueden registrar equipos para un cliente inactivo.");
            }
            if (codigo.length() < 3 || codigo.length() > 40) {
                throw new ServiceException("El código de inventario debe tener entre 3 y 40 caracteres.");
            }
            if (tipo.length() < 2 || tipo.length() > 80) {
                throw new ServiceException("El tipo debe tener entre 2 y 80 caracteres.");
            }
            if (marca.length() > 80 || modelo.length() > 80) {
                throw new ServiceException("Marca y modelo admiten como máximo 80 caracteres.");
            }
            if (equipoDAO.existeCodigo(codigo, e.getIdEquipo())) {
                throw new ServiceException("El código de inventario ya está registrado.");
            }
            e.setCodigoInventario(codigo);
            e.setTipo(tipo);
            e.setMarca(marca);
            e.setModelo(modelo);

            if (e.getIdEquipo() == null) {
                e.setActivo(true);
                equipoDAO.insertar(e);
                return true;
            }
            Optional<Equipo> actual = equipoDAO.buscarPorId(e.getIdEquipo());
            if (actual.isEmpty()) {
                throw new ServiceException("Equipo no encontrado.");
            }
            e.setActivo(actual.get().isActivo());
            equipoDAO.actualizar(e);
            return false;
        } catch (DAOException ex) {
            throw new ServiceException(ex.getMessage() + ".", ex);
        }
    }

    /** DELETE físico solo si el equipo no tiene tickets. */
    public void eliminar(long id) throws ServiceException {
        try {
            int tickets = equipoDAO.contarTickets(id);
            if (tickets > 0) {
                throw new ServiceException("No se puede eliminar: el equipo tiene " + tickets
                        + " ticket(s) relacionado(s). Desactívelo para conservar el historial.");
            }
            if (!equipoDAO.eliminar(id)) {
                throw new ServiceException("Equipo no encontrado.");
            }
        } catch (DAOException ex) {
            throw new ServiceException(ex.getMessage() + ".", ex);
        }
    }

    public void cambiarEstado(long id, boolean activo) throws ServiceException {
        try {
            if (!equipoDAO.cambiarEstado(id, activo)) {
                throw new ServiceException("Equipo no encontrado.");
            }
        } catch (DAOException ex) {
            throw new ServiceException(ex.getMessage() + ".", ex);
        }
    }
}
