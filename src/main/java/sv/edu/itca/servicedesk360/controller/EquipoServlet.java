package sv.edu.itca.servicedesk360.controller;

import java.io.IOException;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sv.edu.itca.servicedesk360.dao.ClienteDAO;
import sv.edu.itca.servicedesk360.dao.DAOException;
import sv.edu.itca.servicedesk360.dao.EquipoDAO;
import sv.edu.itca.servicedesk360.dao.impl.ClienteDAOImpl;
import sv.edu.itca.servicedesk360.dao.impl.EquipoDAOImpl;
import sv.edu.itca.servicedesk360.model.Cliente;
import sv.edu.itca.servicedesk360.model.Equipo;

/**
 * Guía 7, aplicación al proyecto: CRUD de equipos relacionado con
 * clientes. El formulario carga el catálogo de clientes desde MySQL y un
 * equipo con tickets no se elimina (se desactiva).
 */
@WebServlet("/equipos")
public class EquipoServlet extends HttpServlet {

    private static final String VISTA_LISTA = "/WEB-INF/views/equipos/lista.jsp";
    private static final String VISTA_FORMULARIO = "/WEB-INF/views/equipos/formulario.jsp";

    private EquipoDAO equipoDAO;
    private ClienteDAO clienteDAO;

    @Override
    public void init() {
        equipoDAO = new EquipoDAOImpl();
        clienteDAO = new ClienteDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String accion = req.getParameter("accion");
        try {
            if ("nuevo".equals(accion)) {
                mostrarFormulario(req, resp, new Equipo());
            } else if ("editar".equals(accion)) {
                Long id = Navegacion.leerId(req, "id");
                Optional<Equipo> e = id == null ? Optional.empty() : equipoDAO.buscarPorId(id);
                if (e.isEmpty()) {
                    Navegacion.flashError(req, "Equipo no encontrado.");
                    Navegacion.redirigir(req, resp, "/equipos");
                    return;
                }
                mostrarFormulario(req, resp, e.get());
            } else {
                req.setAttribute("equipos", equipoDAO.listarDetalle());
                req.getRequestDispatcher(VISTA_LISTA).forward(req, resp);
            }
        } catch (DAOException ex) {
            Navegacion.mostrarError(req, resp, ex.getMessage() + ". Intente más tarde.");
        } catch (IllegalStateException ex) {
            getServletContext().log("Configuración de base de datos no disponible", ex);
            Navegacion.mostrarError(req, resp, "La configuración de base de datos no está disponible.");
        }
    }

    private void mostrarFormulario(HttpServletRequest req, HttpServletResponse resp, Equipo e)
            throws ServletException, IOException {
        req.setAttribute("equipo", e);
        req.setAttribute("clientes", clienteDAO.listar());
        req.getRequestDispatcher(VISTA_FORMULARIO).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String accion = req.getParameter("accion");
        try {
            if ("guardar".equals(accion)) {
                guardar(req, resp);
            } else if ("eliminar".equals(accion)) {
                eliminar(req);
                Navegacion.redirigir(req, resp, "/equipos");
            } else if ("estado".equals(accion)) {
                Long id = Navegacion.leerId(req, "id");
                boolean activo = Boolean.parseBoolean(req.getParameter("activo"));
                if (id != null && equipoDAO.cambiarEstado(id, activo)) {
                    Navegacion.flash(req, activo ? "Equipo activado." : "Equipo desactivado.");
                } else {
                    Navegacion.flashError(req, "Equipo no encontrado.");
                }
                Navegacion.redirigir(req, resp, "/equipos");
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (DAOException ex) {
            Navegacion.flashError(req, ex.getMessage() + ".");
            Navegacion.redirigir(req, resp, "/equipos");
        } catch (IllegalStateException ex) {
            getServletContext().log("Configuración de base de datos no disponible", ex);
            Navegacion.flashError(req, "La configuración de base de datos no está disponible.");
            Navegacion.redirigir(req, resp, "/equipos");
        }
    }

    private void guardar(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long id = Navegacion.leerId(req, "idEquipo");
        Long idCliente = Navegacion.leerId(req, "idCliente");
        String codigo = Navegacion.texto(req, "codigoInventario").toUpperCase();
        String tipo = Navegacion.texto(req, "tipo");
        String marca = Navegacion.texto(req, "marca");
        String modelo = Navegacion.texto(req, "modelo");
        Equipo e = new Equipo(id, idCliente == null ? 0 : idCliente, codigo, tipo, marca, modelo, true);

        String error = null;
        Optional<Cliente> cliente = idCliente == null ? Optional.empty() : clienteDAO.buscarPorId(idCliente);
        if (cliente.isEmpty()) {
            error = "Seleccione un cliente válido.";
        } else if (id == null && !cliente.get().isActivo()) {
            error = "No se pueden registrar equipos para un cliente inactivo.";
        } else if (codigo.length() < 3 || codigo.length() > 40) {
            error = "El código de inventario debe tener entre 3 y 40 caracteres.";
        } else if (tipo.length() < 2 || tipo.length() > 80) {
            error = "El tipo debe tener entre 2 y 80 caracteres.";
        } else if (marca.length() > 80 || modelo.length() > 80) {
            error = "Marca y modelo admiten como máximo 80 caracteres.";
        } else if (equipoDAO.existeCodigo(codigo, id)) {
            error = "El código de inventario ya está registrado.";
        }
        if (error != null) {
            req.setAttribute("error", error);
            mostrarFormulario(req, resp, e);
            return;
        }

        if (id == null) {
            equipoDAO.insertar(e);
            Navegacion.flash(req, "Equipo " + codigo + " registrado correctamente.");
        } else {
            Optional<Equipo> actual = equipoDAO.buscarPorId(id);
            if (actual.isEmpty()) {
                Navegacion.flashError(req, "Equipo no encontrado.");
                Navegacion.redirigir(req, resp, "/equipos");
                return;
            }
            e.setActivo(actual.get().isActivo());
            equipoDAO.actualizar(e);
            Navegacion.flash(req, "Equipo " + codigo + " actualizado correctamente.");
        }
        Navegacion.redirigir(req, resp, "/equipos");
    }

    private void eliminar(HttpServletRequest req) {
        Long id = Navegacion.leerId(req, "id");
        if (id == null) {
            Navegacion.flashError(req, "Equipo no encontrado.");
            return;
        }
        int tickets = equipoDAO.contarTickets(id);
        if (tickets > 0) {
            Navegacion.flashError(req, "No se puede eliminar: el equipo tiene " + tickets
                    + " ticket(s) relacionado(s). Desactívelo para conservar el historial.");
        } else if (equipoDAO.eliminar(id)) {
            Navegacion.flash(req, "Equipo eliminado.");
        } else {
            Navegacion.flashError(req, "Equipo no encontrado.");
        }
    }
}
