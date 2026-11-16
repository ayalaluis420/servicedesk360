package sv.edu.itca.servicedesk360.controller;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sv.edu.itca.servicedesk360.dao.DAOException;
import sv.edu.itca.servicedesk360.dao.PersonalTecnicoDAO;
import sv.edu.itca.servicedesk360.dao.impl.PersonalTecnicoDAOImpl;
import sv.edu.itca.servicedesk360.model.PersonalTecnico;

/**
 * Guía 6, adaptación a una entidad propia: CRUD del personal técnico.
 * Mismo flujo que ClienteServlet (GET lee, POST modifica, PRG con 303).
 */
@WebServlet("/tecnicos")
public class TecnicoServlet extends HttpServlet {

    private static final String VISTA_LISTA = "/WEB-INF/views/tecnicos/lista.jsp";
    private static final String VISTA_FORMULARIO = "/WEB-INF/views/tecnicos/formulario.jsp";
    private static final Pattern CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private PersonalTecnicoDAO tecnicoDAO;

    @Override
    public void init() {
        tecnicoDAO = new PersonalTecnicoDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String accion = req.getParameter("accion");
        try {
            if ("nuevo".equals(accion)) {
                req.setAttribute("tecnico", new PersonalTecnico());
                req.getRequestDispatcher(VISTA_FORMULARIO).forward(req, resp);
            } else if ("editar".equals(accion)) {
                Long id = Navegacion.leerId(req, "id");
                Optional<PersonalTecnico> t = id == null
                        ? Optional.empty() : tecnicoDAO.buscarPorId(id);
                if (t.isEmpty()) {
                    Navegacion.flashError(req, "Técnico no encontrado.");
                    Navegacion.redirigir(req, resp, "/tecnicos");
                    return;
                }
                req.setAttribute("tecnico", t.get());
                req.getRequestDispatcher(VISTA_FORMULARIO).forward(req, resp);
            } else {
                req.setAttribute("tecnicos", tecnicoDAO.listar());
                req.getRequestDispatcher(VISTA_LISTA).forward(req, resp);
            }
        } catch (DAOException ex) {
            Navegacion.mostrarError(req, resp, ex.getMessage() + ". Intente más tarde.");
        } catch (IllegalStateException ex) {
            getServletContext().log("Configuración de base de datos no disponible", ex);
            Navegacion.mostrarError(req, resp,
                    "La configuración de base de datos no está disponible.");
        }
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
                eliminar(req, resp);
            } else if ("estado".equals(accion)) {
                Long id = Navegacion.leerId(req, "id");
                boolean activo = Boolean.parseBoolean(req.getParameter("activo"));
                if (id != null && tecnicoDAO.cambiarEstado(id, activo)) {
                    Navegacion.flash(req, activo ? "Técnico activado." : "Técnico desactivado.");
                } else {
                    Navegacion.flashError(req, "Técnico no encontrado.");
                }
                Navegacion.redirigir(req, resp, "/tecnicos");
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (DAOException ex) {
            Navegacion.flashError(req, ex.getMessage() + ".");
            Navegacion.redirigir(req, resp, "/tecnicos");
        } catch (IllegalStateException ex) {
            getServletContext().log("Configuración de base de datos no disponible", ex);
            Navegacion.flashError(req, "La configuración de base de datos no está disponible.");
            Navegacion.redirigir(req, resp, "/tecnicos");
        }
    }

    private void guardar(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long id = Navegacion.leerId(req, "idTecnico");
        String nombre = Navegacion.texto(req, "nombre");
        String correo = Navegacion.texto(req, "correo").toLowerCase();
        String especialidad = Navegacion.texto(req, "especialidad");
        int nivel;
        try {
            nivel = Integer.parseInt(Navegacion.texto(req, "nivelSoporte"));
        } catch (NumberFormatException ex) {
            nivel = 0;
        }

        String error = null;
        if (nombre.length() < 3 || nombre.length() > 120) {
            error = "El nombre debe tener entre 3 y 120 caracteres.";
        } else if (correo.length() > 160 || !CORREO.matcher(correo).matches()) {
            error = "Ingrese un correo válido (máximo 160 caracteres).";
        } else if (especialidad.length() > 100) {
            error = "La especialidad admite como máximo 100 caracteres.";
        } else if (nivel < 1 || nivel > 3) {
            error = "El nivel de soporte debe estar entre 1 y 3.";
        } else if (tecnicoDAO.existeCorreo(correo, id)) {
            error = "El correo ya está registrado.";
        }

        if (error != null) {
            req.setAttribute("error", error);
            req.setAttribute("tecnico", new PersonalTecnico(
                    id, nombre, correo, especialidad, Math.max(nivel, 1), true));
            req.getRequestDispatcher(VISTA_FORMULARIO).forward(req, resp);
            return;
        }

        if (id == null) {
            tecnicoDAO.insertar(new PersonalTecnico(
                    null, nombre, correo, especialidad, nivel, true));
            Navegacion.flash(req, "Técnico registrado correctamente.");
        } else {
            Optional<PersonalTecnico> encontrado = tecnicoDAO.buscarPorId(id);
            if (encontrado.isEmpty()) {
                Navegacion.flashError(req, "Técnico no encontrado.");
                Navegacion.redirigir(req, resp, "/tecnicos");
                return;
            }
            PersonalTecnico actual = encontrado.get();
            actual.setNombre(nombre);
            actual.setCorreo(correo);
            actual.setEspecialidad(especialidad);
            actual.setNivelSoporte(nivel);
            tecnicoDAO.actualizar(actual);
            Navegacion.flash(req, "Técnico actualizado correctamente.");
        }
        Navegacion.redirigir(req, resp, "/tecnicos");
    }

    private void eliminar(HttpServletRequest req, HttpServletResponse resp) {
        Long id = Navegacion.leerId(req, "id");
        if (id == null) {
            Navegacion.flashError(req, "Técnico no encontrado.");
        } else {
            int tickets = tecnicoDAO.contarTicketsAsignados(id);
            if (tickets > 0) {
                Navegacion.flashError(req, "No se puede eliminar: el técnico tiene "
                        + tickets + " ticket(s) asignado(s). Desactívelo para conservar el historial.");
            } else if (tecnicoDAO.eliminar(id)) {
                Navegacion.flash(req, "Técnico eliminado.");
            } else {
                Navegacion.flashError(req, "Técnico no encontrado.");
            }
        }
        Navegacion.redirigir(req, resp, "/tecnicos");
    }
}
