package sv.edu.itca.servicedesk360.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import sv.edu.itca.servicedesk360.dao.ClienteDAO;
import sv.edu.itca.servicedesk360.dao.DAOException;
import sv.edu.itca.servicedesk360.dao.impl.ClienteDAOImpl;
import sv.edu.itca.servicedesk360.dto.EquipoDetalleDTO;
import sv.edu.itca.servicedesk360.model.Cliente;
import sv.edu.itca.servicedesk360.model.Equipo;
import sv.edu.itca.servicedesk360.service.EquipoService;
import sv.edu.itca.servicedesk360.service.ServiceException;

/**
 * Guía 10, aplicación al proyecto (sección XIII): inventario de equipos
 * con PrimeFaces. Tabla con paginación, ordenamiento y filtros, filtro
 * externo por AJAX, diálogo de alta/edición con convertidor y validador,
 * y cambio de estado con confirmación. Delega en {@link EquipoService}.
 */
@Named("equipoBean")
@ViewScoped
public class EquipoBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private transient EquipoService equipoService;
    private transient ClienteDAO clienteDAO;

    private List<EquipoDetalleDTO> todos = new ArrayList<>();
    private List<EquipoDetalleDTO> equipos = new ArrayList<>();
    private List<EquipoDetalleDTO> equiposFiltrados;
    private boolean soloActivos;
    private List<Cliente> clientes = new ArrayList<>();

    private Equipo equipo = new Equipo();
    private boolean edicion;

    private EquipoService servicio() {
        if (equipoService == null) {
            equipoService = new EquipoService();
        }
        return equipoService;
    }

    @PostConstruct
    public void init() {
        try {
            if (clienteDAO == null) {
                clienteDAO = new ClienteDAOImpl();
            }
            clientes = clienteDAO.listar();
        } catch (DAOException | IllegalStateException ex) {
            mensaje(FacesMessage.SEVERITY_ERROR, "No fue posible cargar los clientes", "Intente más tarde.");
        }
        cargar();
    }

    public void cargar() {
        try {
            todos = servicio().listarDetalle();
            aplicarFiltro();
        } catch (DAOException | IllegalStateException ex) {
            todos = new ArrayList<>();
            equipos = new ArrayList<>();
            mensaje(FacesMessage.SEVERITY_ERROR, "No fue posible cargar los equipos", "Intente más tarde.");
        }
    }

    /** Filtro externo por AJAX (p:toggleSwitch "Solo activos"). */
    public void aplicarFiltro() {
        equipos = soloActivos
                ? todos.stream().filter(EquipoDetalleDTO::isActivo).collect(Collectors.toList())
                : new ArrayList<>(todos);
        equiposFiltrados = null;
    }

    public void prepararNuevo() {
        equipo = new Equipo();
        edicion = false;
    }

    public void prepararEdicion(EquipoDetalleDTO fila) {
        try {
            Optional<Equipo> e = servicio().buscarPorId(fila.getIdEquipo());
            if (e.isPresent()) {
                equipo = e.get();
                edicion = true;
            } else {
                mensaje(FacesMessage.SEVERITY_ERROR, "Equipo no encontrado", "Recargue la pantalla.");
            }
        } catch (DAOException | IllegalStateException ex) {
            mensaje(FacesMessage.SEVERITY_ERROR, "No fue posible cargar el equipo", "Intente más tarde.");
        }
    }

    public void guardar() {
        try {
            boolean insertado = servicio().guardar(equipo);
            mensaje(FacesMessage.SEVERITY_INFO, insertado ? "Equipo registrado" : "Equipo actualizado",
                    equipo.getCodigoInventario());
            cargar();
            prepararNuevo();
        } catch (ServiceException ex) {
            mensaje(FacesMessage.SEVERITY_ERROR, "No fue posible guardar", ex.getMessage());
            FacesContext.getCurrentInstance().validationFailed();
        }
    }

    public void cambiarEstado(EquipoDetalleDTO fila) {
        try {
            servicio().cambiarEstado(fila.getIdEquipo(), !fila.isActivo());
            mensaje(FacesMessage.SEVERITY_INFO, fila.isActivo() ? "Equipo desactivado" : "Equipo activado",
                    fila.getCodigoInventario());
        } catch (ServiceException ex) {
            mensaje(FacesMessage.SEVERITY_ERROR, "No se cambió el estado", ex.getMessage());
        }
        cargar();
    }

    public void eliminar(EquipoDetalleDTO fila) {
        try {
            servicio().eliminar(fila.getIdEquipo());
            mensaje(FacesMessage.SEVERITY_INFO, "Equipo eliminado", fila.getCodigoInventario());
        } catch (ServiceException ex) {
            mensaje(FacesMessage.SEVERITY_ERROR, "No se eliminó", ex.getMessage());
        }
        cargar();
    }

    public long getActivos() {
        return todos.stream().filter(EquipoDetalleDTO::isActivo).count();
    }

    private static void mensaje(FacesMessage.Severity sev, String resumen, String detalle) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(sev, resumen, detalle));
    }

    public List<EquipoDetalleDTO> getEquipos() {
        return equipos;
    }

    public List<EquipoDetalleDTO> getEquiposFiltrados() {
        return equiposFiltrados;
    }

    public void setEquiposFiltrados(List<EquipoDetalleDTO> equiposFiltrados) {
        this.equiposFiltrados = equiposFiltrados;
    }

    public boolean isSoloActivos() {
        return soloActivos;
    }

    public void setSoloActivos(boolean soloActivos) {
        this.soloActivos = soloActivos;
    }

    public List<Cliente> getClientes() {
        return clientes;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public void setEquipo(Equipo equipo) {
        this.equipo = equipo;
    }

    public boolean isEdicion() {
        return edicion;
    }

    public int getTotal() {
        return todos.size();
    }
}
