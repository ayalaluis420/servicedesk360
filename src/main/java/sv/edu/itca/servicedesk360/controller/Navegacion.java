package sv.edu.itca.servicedesk360.controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Utilidades de navegación compartidas por los controladores CRUD
 * (Guía 6): Post/Redirect/Get, mensajes flash y lectura segura de ids.
 * No contiene reglas de negocio ni acceso a datos.
 */
final class Navegacion {

    private Navegacion() {
    }

    /**
     * Post/Redirect/Get: responde 303 See Other para que el navegador
     * cargue la ruta con GET y un refresh no repita el POST.
     */
    static void redirigir(HttpServletRequest req, HttpServletResponse resp,
            String ruta) {
        resp.setStatus(HttpServletResponse.SC_SEE_OTHER);
        resp.setHeader("Location", req.getContextPath() + ruta);
    }

    /** Mensaje de éxito que la lista muestra una sola vez. */
    static void flash(HttpServletRequest req, String mensaje) {
        req.getSession().setAttribute("flash", mensaje);
    }

    /** Mensaje de error que la lista muestra una sola vez. */
    static void flashError(HttpServletRequest req, String mensaje) {
        req.getSession().setAttribute("flashError", mensaje);
    }

    /** Convierte el parámetro en id positivo, o null si no es válido. */
    static Long leerId(HttpServletRequest req, String parametro) {
        String valor = req.getParameter(parametro);
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            long id = Long.parseLong(valor.trim());
            return id > 0 ? id : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Error de persistencia en una lectura: página controlada con un
     * mensaje funcional. El detalle técnico ya quedó en el log del DAO.
     */
    static void mostrarError(HttpServletRequest req, HttpServletResponse resp,
            String mensaje) throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        req.setAttribute("mensajeError", mensaje);
        req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
    }

    /** Texto recortado; nunca null. */
    static String texto(HttpServletRequest req, String parametro) {
        String valor = req.getParameter(parametro);
        return valor == null ? "" : valor.trim();
    }
}
