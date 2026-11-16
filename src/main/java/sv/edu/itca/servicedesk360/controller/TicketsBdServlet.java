package sv.edu.itca.servicedesk360.controller;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sv.edu.itca.servicedesk360.persistence.DiagnosticoBD;
import sv.edu.itca.servicedesk360.persistence.TicketsJdbc;

/**
 * Guía 5, Parte E: muestra los tickets guardados en MySQL para comprobar
 * que persisten después de reiniciar Tomcat. Es una ruta de laboratorio;
 * en la Guía 6 el listado principal usará el DAO.
 *
 * <p>Protegida por AutenticacionFilter (patrón /tickets/*).
 */
@WebServlet("/tickets/bd")
public class TicketsBdServlet extends HttpServlet {

    private final TicketsJdbc tickets = new TicketsJdbc();

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("tickets", tickets.listarTickets());
        } catch (SQLException ex) {
            request.setAttribute("mensajeError",
                    DiagnosticoBD.registrar("Listar tickets desde MySQL", ex));
        } catch (IllegalStateException ex) {
            getServletContext().log("Configuración JDBC no disponible", ex);
            request.setAttribute("mensajeError",
                    "La configuración de base de datos no está disponible.");
        }
        request.getRequestDispatcher(
                "/WEB-INF/views/tickets/persistidos.jsp")
                .forward(request, response);
    }
}
