package sv.edu.itca.servicedesk360.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sv.edu.itca.servicedesk360.dao.ClienteDAO;
import sv.edu.itca.servicedesk360.dao.DAOException;
import sv.edu.itca.servicedesk360.dao.impl.ClienteDAOImpl;
import sv.edu.itca.servicedesk360.model.Cliente;

/**
 * Guía 6: controlador del CRUD de Clientes.
 *
 * <p>Interpreta HTTP, valida el formulario, invoca al contrato
 * {@link ClienteDAO} y decide la vista o la redirección. No contiene SQL
 * ni manipula ResultSet. Las lecturas usan GET; las modificaciones usan
 * POST y terminan con Post/Redirect/Get (303).
 *
 * <p>Ruta protegida por AutenticacionFilter.
 */
@WebServlet("/clientes")
public class ClienteServlet extends HttpServlet {

    private static final String VISTA_LISTA = "/WEB-INF/views/clientes/lista.jsp";
    private static final String VISTA_FORMULARIO = "/WEB-INF/views/clientes/formulario.jsp";
    private static final Pattern CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private ClienteDAO clienteDAO;

    @Override
    public void init() {
        clienteDAO = new ClienteDAOImpl();
    }

    // ----------------------------------------------------------- lecturas

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String accion = req.getParameter("accion");
        if (accion == null) {
            accion = "listar";
        }
        try {
            switch (accion) {
                case "nuevo":
                    req.setAttribute("cliente", new Cliente());
                    req.getRequestDispatcher(VISTA_FORMULARIO).forward(req, resp);
                    break;
                case "editar":
                    mostrarEdicion(req, resp);
                    break;
                default:
                    listar(req, resp);
            }
        } catch (DAOException ex) {
            Navegacion.mostrarError(req, resp, ex.getMessage() + ". Intente más tarde.");
        } catch (IllegalStateException ex) {
            getServletContext().log("Configuración de base de datos no disponible", ex);
            Navegacion.mostrarError(req, resp,
                    "La configuración de base de datos no está disponible.");
        }
    }

    private void listar(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String criterio = Navegacion.texto(req, "q");
        boolean soloActivos = "activos".equals(req.getParameter("filtro"));

        List<Cliente> clientes;
        if (!criterio.isEmpty()) {
            clientes = clienteDAO.buscar(criterio);           // Ejercicio 1
            if (soloActivos) {
                clientes.removeIf(c -> !c.isActivo());
            }
        } else if (soloActivos) {
            clientes = clienteDAO.listarActivos();            // Ejercicio 2
        } else {
            clientes = clienteDAO.listar();
        }

        req.setAttribute("clientes", clientes);
        req.setAttribute("q", criterio);
        req.setAttribute("filtro", soloActivos ? "activos" : "todos");
        req.getRequestDispatcher(VISTA_LISTA).forward(req, resp);
    }

    private void mostrarEdicion(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long id = Navegacion.leerId(req, "id");
        Optional<Cliente> cliente = id == null ? Optional.empty() : clienteDAO.buscarPorId(id);
        if (cliente.isEmpty()) {
            // V07: id inexistente o inválido -> flujo controlado, sin traza.
            Navegacion.flashError(req, "Cliente no encontrado.");
            Navegacion.redirigir(req, resp, "/clientes");
            return;
        }
        req.setAttribute("cliente", cliente.get());
        req.getRequestDispatcher(VISTA_FORMULARIO).forward(req, resp);
    }

    // ------------------------------------------------------- escrituras

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String accion = req.getParameter("accion");
        try {
            if ("guardar".equals(accion)) {
                guardar(req, resp);
            } else if ("eliminar".equals(accion)) {
                eliminar(req, resp);
            } else if ("estado".equals(accion)) {
                cambiarEstado(req, resp);
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (DAOException ex) {
            Navegacion.flashError(req, ex.getMessage() + ".");
            Navegacion.redirigir(req, resp, "/clientes");
        } catch (IllegalStateException ex) {
            getServletContext().log("Configuración de base de datos no disponible", ex);
            Navegacion.flashError(req, "La configuración de base de datos no está disponible.");
            Navegacion.redirigir(req, resp, "/clientes");
        }
    }

    private void guardar(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long id = Navegacion.leerId(req, "idCliente");
        String nombre = Navegacion.texto(req, "nombre");
        String correo = Navegacion.texto(req, "correo").toLowerCase();

        String error = validar(nombre, correo);
        if (error == null && clienteDAO.existeCorreo(correo, id)) {
            error = "El correo ya está registrado.";               // V05
        }
        if (error != null) {
            req.setAttribute("error", error);
            req.setAttribute("cliente", new Cliente(id, nombre, correo, true));
            req.getRequestDispatcher(VISTA_FORMULARIO).forward(req, resp);
            return;
        }

        if (id == null) {
            clienteDAO.insertar(new Cliente(null, nombre, correo, true));
            Navegacion.flash(req, "Cliente registrado correctamente.");
        } else {
            Optional<Cliente> encontrado = clienteDAO.buscarPorId(id);
            if (encontrado.isEmpty()) {
                Navegacion.flashError(req, "Cliente no encontrado.");
                Navegacion.redirigir(req, resp, "/clientes");
                return;
            }
            Cliente actual = encontrado.get();
            actual.setNombre(nombre);
            actual.setCorreo(correo);
            clienteDAO.actualizar(actual);
            Navegacion.flash(req, "Cliente actualizado correctamente.");
        }
        Navegacion.redirigir(req, resp, "/clientes");
    }

    /** Reglas del formulario (sección IX.2 de la guía). */
    private String validar(String nombre, String correo) {
        if (nombre.length() < 3 || nombre.length() > 120) {
            return "El nombre debe tener entre 3 y 120 caracteres.";
        }
        if (correo.isEmpty() || correo.length() > 160 || !CORREO.matcher(correo).matches()) {
            return "Ingrese un correo válido (máximo 160 caracteres).";
        }
        return null;
    }

    private void eliminar(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Long id = Navegacion.leerId(req, "id");
        if (id == null) {
            Navegacion.flashError(req, "Cliente no encontrado.");
        } else {
            // Ejercicio 3: se cuentan las relaciones antes del DELETE físico.
            int relaciones = clienteDAO.contarRelaciones(id);
            if (relaciones > 0) {
                Navegacion.flashError(req, "No se puede eliminar: el cliente tiene "
                        + relaciones + " registro(s) relacionado(s) (equipos o tickets). "
                        + "Desactívelo para conservar su historial.");
            } else if (clienteDAO.eliminar(id)) {
                Navegacion.flash(req, "Cliente eliminado.");
            } else {
                Navegacion.flashError(req, "Cliente no encontrado.");
            }
        }
        Navegacion.redirigir(req, resp, "/clientes");
    }

    private void cambiarEstado(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Long id = Navegacion.leerId(req, "id");
        boolean activo = Boolean.parseBoolean(req.getParameter("activo"));
        if (id != null && clienteDAO.cambiarEstado(id, activo)) {
            Navegacion.flash(req, activo ? "Cliente activado." : "Cliente desactivado.");
        } else {
            Navegacion.flashError(req, "Cliente no encontrado.");
        }
        Navegacion.redirigir(req, resp, "/clientes");
    }
}
