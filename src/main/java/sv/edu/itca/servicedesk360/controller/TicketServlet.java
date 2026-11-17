package sv.edu.itca.servicedesk360.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sv.edu.itca.servicedesk360.dao.CategoriaDAO;
import sv.edu.itca.servicedesk360.dao.ClienteDAO;
import sv.edu.itca.servicedesk360.dao.DAOException;
import sv.edu.itca.servicedesk360.dao.EquipoDAO;
import sv.edu.itca.servicedesk360.dao.PersonalTecnicoDAO;
import sv.edu.itca.servicedesk360.dao.SeguimientoDAO;
import sv.edu.itca.servicedesk360.dao.TicketDAO;
import sv.edu.itca.servicedesk360.dao.impl.CategoriaDAOImpl;
import sv.edu.itca.servicedesk360.dao.impl.ClienteDAOImpl;
import sv.edu.itca.servicedesk360.dao.impl.EquipoDAOImpl;
import sv.edu.itca.servicedesk360.dao.impl.PersonalTecnicoDAOImpl;
import sv.edu.itca.servicedesk360.dao.impl.SeguimientoDAOImpl;
import sv.edu.itca.servicedesk360.dao.impl.TicketDAOImpl;
import sv.edu.itca.servicedesk360.dto.TicketDetalleDTO;
import sv.edu.itca.servicedesk360.model.Equipo;
import sv.edu.itca.servicedesk360.model.Ticket;
import sv.edu.itca.servicedesk360.service.ServiceException;
import sv.edu.itca.servicedesk360.service.TicketService;

/**
 * Guía 7: controlador del proceso de tickets.
 *
 * <p>Lee parámetros, carga catálogos, invoca a {@link TicketService} para
 * las operaciones de escritura y decide vista o redirección. No contiene
 * SQL ni administra commit/rollback (eso lo hace el servicio).
 *
 * <pre>
 * GET  /tickets                          listar (filtros estado/prioridad, paginación)
 * GET  /tickets?accion=nuevo             formulario con catálogos dinámicos
 * GET  /tickets?accion=equipos&idCliente= equipos del cliente en JSON (paso 19)
 * GET  /tickets?accion=ver&id=           detalle + historial (Ejercicio 2)
 * GET  /tickets?accion=productividad     tickets por técnico y estado (Ejercicio 1)
 * POST /tickets  accion=guardar          registrar ticket + seguimiento (transacción)
 * POST /tickets  accion=estado           cambiar estado con control optimista
 * </pre>
 */
@WebServlet("/tickets")
public class TicketServlet extends HttpServlet {

    private static final String VISTA_LISTADO = "/WEB-INF/views/tickets/listado.jsp";
    private static final String VISTA_FORMULARIO = "/WEB-INF/views/tickets/formulario.jsp";
    private static final String VISTA_DETALLE = "/WEB-INF/views/tickets/detalle.jsp";
    private static final String VISTA_PRODUCTIVIDAD = "/WEB-INF/views/tickets/productividad.jsp";
    private static final int LIMITE = 10;

    private TicketService ticketService;
    private TicketDAO ticketDAO;
    private SeguimientoDAO seguimientoDAO;
    private ClienteDAO clienteDAO;
    private EquipoDAO equipoDAO;
    private CategoriaDAO categoriaDAO;
    private PersonalTecnicoDAO tecnicoDAO;

    @Override
    public void init() {
        ticketService = new TicketService();
        ticketDAO = new TicketDAOImpl();
        seguimientoDAO = new SeguimientoDAOImpl();
        clienteDAO = new ClienteDAOImpl();
        equipoDAO = new EquipoDAOImpl();
        categoriaDAO = new CategoriaDAOImpl();
        tecnicoDAO = new PersonalTecnicoDAOImpl();
    }

    // ============================================================ GET

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String accion = req.getParameter("accion");
        try {
            if ("nuevo".equals(accion)) {
                Ticket t = new Ticket();
                Long idCliente = Navegacion.leerId(req, "idCliente");
                if (idCliente != null) {
                    t.setIdCliente(idCliente);
                }
                req.setAttribute("ticket", t);
                cargarCatalogos(req, idCliente);
                req.getRequestDispatcher(VISTA_FORMULARIO).forward(req, resp);
            } else if ("equipos".equals(accion)) {
                responderEquiposJson(req, resp);
            } else if ("ver".equals(accion)) {
                ver(req, resp);
            } else if ("productividad".equals(accion)) {
                req.setAttribute("filas", ticketDAO.productividadPorTecnico());
                req.getRequestDispatcher(VISTA_PRODUCTIVIDAD).forward(req, resp);
            } else {
                listar(req, resp);
            }
        } catch (DAOException ex) {
            Navegacion.mostrarError(req, resp, ex.getMessage() + ". Intente más tarde.");
        } catch (IllegalStateException ex) {
            getServletContext().log("Configuración de base de datos no disponible", ex);
            Navegacion.mostrarError(req, resp, "La configuración de base de datos no está disponible.");
        }
    }

    /** Parte I: listado con JOIN, filtros por GET y paginación. */
    private void listar(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Solo se aceptan valores del catálogo: cualquier otro se ignora.
        String estado = valorDeCatalogo(req.getParameter("estado"), TicketService.ESTADOS);
        String prioridad = valorDeCatalogo(req.getParameter("prioridad"), TicketService.PRIORIDADES);

        int total = ticketDAO.contar(estado, prioridad);
        int totalPaginas = Math.max(1, (int) Math.ceil(total / (double) LIMITE));
        int pagina = Math.min(leerPagina(req), totalPaginas);
        int offset = (pagina - 1) * LIMITE;

        req.setAttribute("tickets", ticketDAO.listarDetalle(estado, prioridad, LIMITE, offset));
        req.setAttribute("estado", estado);
        req.setAttribute("prioridad", prioridad);
        req.setAttribute("pagina", pagina);
        req.setAttribute("totalPaginas", totalPaginas);
        req.setAttribute("total", total);
        req.setAttribute("estados", TicketService.ESTADOS);
        req.setAttribute("prioridades", TicketService.PRIORIDADES);
        req.getRequestDispatcher(VISTA_LISTADO).forward(req, resp);
    }

    /** Ejercicio 2: datos del ticket y todos sus seguimientos. */
    private void ver(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long id = Navegacion.leerId(req, "id");
        Optional<TicketDetalleDTO> ticket = id == null ? Optional.empty() : ticketDAO.buscarDetalle(id);
        if (ticket.isEmpty()) {
            Navegacion.flashError(req, "Ticket no encontrado.");
            Navegacion.redirigir(req, resp, "/tickets");
            return;
        }
        req.setAttribute("ticket", ticket.get());
        req.setAttribute("seguimientos", seguimientoDAO.listarPorTicket(id));
        req.setAttribute("transiciones", ticketService.transicionesDesde(ticket.get().getEstado()));
        req.getRequestDispatcher(VISTA_DETALLE).forward(req, resp);
    }

    /** Paso 17: catálogos dinámicos leídos de MySQL. */
    private void cargarCatalogos(HttpServletRequest req, Long idCliente) {
        req.setAttribute("clientes", clienteDAO.listarActivos());
        req.setAttribute("categorias", categoriaDAO.listarActivas());
        req.setAttribute("tecnicos", tecnicoDAO.listarActivos());
        req.setAttribute("prioridades", TicketService.PRIORIDADES);
        req.setAttribute("equipos", idCliente == null ? List.of() : equipoDAO.listarPorCliente(idCliente));
    }

    /** Paso 19: equipos activos del cliente como JSON simple. */
    private void responderEquiposJson(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Long idCliente = Navegacion.leerId(req, "idCliente");
        List<Equipo> equipos = idCliente == null ? List.of() : equipoDAO.listarPorCliente(idCliente);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < equipos.size(); i++) {
            Equipo e = equipos.get(i);
            String texto = e.getCodigoInventario() + " - " + e.getTipo()
                    + (e.getMarca() == null ? "" : " " + e.getMarca())
                    + (e.getModelo() == null ? "" : " " + e.getModelo());
            if (i > 0) {
                json.append(',');
            }
            json.append("{\"id\":").append(e.getIdEquipo())
                    .append(",\"texto\":\"").append(escaparJson(texto)).append("\"}");
        }
        json.append(']');
        try (PrintWriter out = resp.getWriter()) {
            out.print(json);
        }
    }

    // =========================================================== POST

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String accion = req.getParameter("accion");
        try {
            if ("guardar".equals(accion)) {
                guardar(req, resp);
            } else if ("estado".equals(accion)) {
                cambiarEstado(req, resp);
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (DAOException ex) {
            Navegacion.mostrarError(req, resp, ex.getMessage() + ". Intente más tarde.");
        } catch (IllegalStateException ex) {
            getServletContext().log("Configuración de base de datos no disponible", ex);
            Navegacion.mostrarError(req, resp, "La configuración de base de datos no está disponible.");
        }
    }

    /** Paso 21: registro del ticket con su seguimiento inicial. */
    private void guardar(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long idCliente = Navegacion.leerId(req, "idCliente");
        Ticket t = new Ticket();
        t.setIdCliente(idCliente == null ? 0 : idCliente);
        t.setIdEquipo(Navegacion.leerId(req, "idEquipo"));
        t.setIdTecnico(Navegacion.leerId(req, "idTecnico"));
        Long idCategoria = Navegacion.leerId(req, "idCategoria");
        t.setIdCategoria(idCategoria == null ? 0 : idCategoria);
        t.setTitulo(Navegacion.texto(req, "titulo"));
        t.setDescripcion(Navegacion.texto(req, "descripcion"));
        t.setPrioridad(Navegacion.texto(req, "prioridad"));
        String detalleInicial = Navegacion.texto(req, "detalleInicial");

        try {
            long id = ticketService.registrarTicketConSeguimiento(t, detalleInicial);
            Navegacion.flash(req, "Ticket #" + id + " registrado con su seguimiento inicial.");
            Navegacion.redirigir(req, resp, "/tickets");
        } catch (ServiceException ex) {
            // Mensaje funcional del servicio; se conservan los valores escritos.
            req.setAttribute("error", ex.getMessage());
            req.setAttribute("ticket", t);
            req.setAttribute("detalleInicial", detalleInicial);
            cargarCatalogos(req, idCliente);
            req.getRequestDispatcher(VISTA_FORMULARIO).forward(req, resp);
        }
    }

    /** Parte J: cambio de estado con el estado esperado (control optimista). */
    private void cambiarEstado(HttpServletRequest req, HttpServletResponse resp) {
        Long id = Navegacion.leerId(req, "id");
        if (id == null) {
            Navegacion.flashError(req, "Ticket no encontrado.");
            Navegacion.redirigir(req, resp, "/tickets");
            return;
        }
        try {
            ticketService.cambiarEstado(id, Navegacion.texto(req, "estadoEsperado"),
                    Navegacion.texto(req, "nuevoEstado"), Navegacion.texto(req, "comentario"));
            Navegacion.flash(req, "Estado actualizado a " + Navegacion.texto(req, "nuevoEstado") + ".");
        } catch (ServiceException ex) {
            Navegacion.flashError(req, ex.getMessage());
        }
        Navegacion.redirigir(req, resp, "/tickets?accion=ver&id=" + id);
    }

    // ====================================================== utilidades

    private static String valorDeCatalogo(String valor, List<String> catalogo) {
        return valor != null && catalogo.contains(valor) ? valor : null;
    }

    /** Página solicitada; un valor ausente o no numérico equivale a 1. */
    private static int leerPagina(HttpServletRequest req) {
        try {
            return Math.max(1, Integer.parseInt(req.getParameter("pagina")));
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    private static String escaparJson(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '<': sb.append("\\u003c"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}
