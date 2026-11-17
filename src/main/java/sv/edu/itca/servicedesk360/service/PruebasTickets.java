package sv.edu.itca.servicedesk360.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import sv.edu.itca.servicedesk360.dao.ClienteDAO;
import sv.edu.itca.servicedesk360.dao.DAOException;
import sv.edu.itca.servicedesk360.dao.EquipoDAO;
import sv.edu.itca.servicedesk360.dao.SeguimientoDAO;
import sv.edu.itca.servicedesk360.dao.TicketDAO;
import sv.edu.itca.servicedesk360.dao.impl.CategoriaDAOImpl;
import sv.edu.itca.servicedesk360.dao.impl.ClienteDAOImpl;
import sv.edu.itca.servicedesk360.dao.impl.EquipoDAOImpl;
import sv.edu.itca.servicedesk360.dao.impl.PersonalTecnicoDAOImpl;
import sv.edu.itca.servicedesk360.dao.impl.SeguimientoDAOImpl;
import sv.edu.itca.servicedesk360.dao.impl.TicketDAOImpl;
import sv.edu.itca.servicedesk360.dto.ProductividadDTO;
import sv.edu.itca.servicedesk360.dto.TicketDetalleDTO;
import sv.edu.itca.servicedesk360.model.Equipo;
import sv.edu.itca.servicedesk360.model.Seguimiento;
import sv.edu.itca.servicedesk360.model.Ticket;
import sv.edu.itca.servicedesk360.persistence.ConexionBD;

/**
 * Guía 7: pruebas del proceso de tickets (sección XIII de la guía).
 *
 * <p>Requisitos: sql/01, 02, 05 y 06 ejecutados y
 * ~/.servicedesk360/db.properties configurado.
 *
 * <p>La prueba de rollback (8) no usa código de fallo dentro del
 * servicio: se construye un TicketService con un SeguimientoDAO que falla
 * después de insertar el ticket, gracias a la inyección por constructor.
 */
public class PruebasTickets {

    private static int fallidas = 0;

    public static void main(String[] args) throws Exception {
        ClienteDAO clienteDAO = new ClienteDAOImpl();
        EquipoDAO equipoDAO = new EquipoDAOImpl();
        TicketDAO ticketDAO = new TicketDAOImpl();
        SeguimientoDAO seguimientoDAO = new SeguimientoDAOImpl();
        TicketService service = new TicketService();

        long ana = idPorCampo("clientes", "id_cliente", "correo", "ana.lopez@demo.local");
        long carlos = idPorCampo("clientes", "id_cliente", "correo", "carlos.mendez@demo.local");
        long eqAna = idPorCampo("equipos", "id_equipo", "codigo_inventario", "EQ-0001");
        long eqCarlos = idPorCampo("equipos", "id_equipo", "codigo_inventario", "EQ-0002");
        long soft = idPorCampo("categorias", "id_categoria", "nombre", "Software");
        long retirada = idPorCampo("categorias", "id_categoria", "nombre", "Telefonía (retirada)");
        long maria = idPorCampo("tecnicos", "id_tecnico", "correo", "maria.ruiz@demo.local");
        long jorge = idPorCampo("tecnicos", "id_tecnico", "correo", "jorge.perez@demo.local");

        // 1. Catálogo de clientes activos
        verificar("1", "Carga de clientes activos", !clienteDAO.listarActivos().isEmpty(),
                clienteDAO.listarActivos().size() + " cliente(s) activo(s) desde MySQL");

        // 2. Categorías y técnicos
        boolean sinRetirada = new CategoriaDAOImpl().listarActivas().stream()
                .noneMatch(c -> c.getIdCategoria() == retirada);
        verificar("2", "Carga de categorías y técnicos",
                sinRetirada && !new PersonalTecnicoDAOImpl().listarActivos().isEmpty(),
                new CategoriaDAOImpl().listarActivas().size() + " categoría(s) habilitada(s) (sin la retirada), "
                + new PersonalTecnicoDAOImpl().listarActivos().size() + " técnico(s) activo(s)");

        // 3. Equipo pertenece al cliente
        verificar("3", "Equipo pertenece al cliente",
                equipoDAO.perteneceACliente(eqAna, ana) && !equipoDAO.perteneceACliente(eqAna, carlos),
                "EQ-0001 pertenece a Ana y no a Carlos");

        // 4. Equipo de otro cliente
        esperarRechazo("4", "Equipo de otro cliente", service,
                ticket(ana, eqCarlos, maria, soft, "Equipo ajeno", "MEDIA"), "Registro inicial",
                "no pertenece al cliente");

        // 5. Título vacío o corto
        esperarRechazo("5", "Título vacío o corto", service,
                ticket(ana, eqAna, maria, soft, "Mal", "MEDIA"), "Registro inicial", "título");

        // 6. Prioridad no permitida (también "CRÍTICA" con tilde, que no existe en la base)
        esperarRechazo("6", "Prioridad no permitida", service,
                ticket(ana, eqAna, maria, soft, "Prioridad inventada", "URGENTE"), "Registro inicial",
                "Prioridad no válida");
        esperarRechazo("6b", "Prioridad 'CRÍTICA' con tilde", service,
                ticket(ana, eqAna, jorge, soft, "Prioridad con tilde", "CRÍTICA"), "Registro inicial",
                "Prioridad no válida");

        // 7. Registro válido
        int t0 = ticketDAO.contar(null, null);
        int s0 = contarSeguimientos();
        long id = service.registrarTicketConSeguimiento(
                ticket(ana, eqAna, maria, soft, "Error al ingresar al portal", "MEDIA"),
                "Cliente reporta el error por teléfono.");
        TicketDetalleDTO det = ticketDAO.buscarDetalle(id).get();
        verificar("7", "Registro válido (ticket + seguimiento)",
                ticketDAO.contar(null, null) == t0 + 1 && contarSeguimientos() == s0 + 1
                && seguimientoDAO.listarPorTicket(id).size() == 1 && "ABIERTO".equals(det.getEstado()),
                "ticket #" + id + " ABIERTO | tickets " + t0 + "->" + (t0 + 1)
                + " | seguimientos " + s0 + "->" + contarSeguimientos());

        // 8. Fallo controlado DESPUÉS de insertar el ticket -> rollback completo
        SeguimientoDAO seguimientoQueFalla = new SeguimientoDAO() {
            @Override
            public int registrar(Connection cn, Seguimiento s) {
                throw new DAOException("Fallo controlado para probar rollback");
            }

            @Override
            public List<Seguimiento> listarPorTicket(long idTicket) {
                return List.of();
            }
        };
        TicketService serviceConFallo = new TicketService(new TicketDAOImpl(), seguimientoQueFalla,
                new EquipoDAOImpl(), new ClienteDAOImpl(), new CategoriaDAOImpl(), new PersonalTecnicoDAOImpl());
        int t1 = ticketDAO.contar(null, null);
        int s1 = contarSeguimientos();
        long maxAntes = maxIdTicket();
        String msg8 = null;
        try {
            serviceConFallo.registrarTicketConSeguimiento(
                    ticket(ana, eqAna, maria, soft, "Ticket que debe revertirse", "BAJA"), "FALLAR");
        } catch (ServiceException ex) {
            msg8 = ex.getMessage();
        }
        verificar("8", "Fallo controlado -> rollback completo",
                msg8 != null && ticketDAO.contar(null, null) == t1 && contarSeguimientos() == s1
                && maxIdTicket() == maxAntes && contarHuerfanos() == 0,
                "tickets " + t1 + "->" + ticketDAO.contar(null, null) + " | seguimientos " + s1 + "->"
                + contarSeguimientos() + " | huérfanos " + contarHuerfanos() + " | " + msg8);

        // 8b. Fallo real de MySQL en el segundo INSERT (detalle de 801 caracteres)
        String msg8b = null;
        try {
            service.registrarTicketConSeguimiento(
                    ticket(ana, eqAna, maria, soft, "Seguimiento demasiado largo", "BAJA"), "x".repeat(801));
        } catch (ServiceException ex) {
            msg8b = ex.getMessage();
        }
        verificar("8b", "MySQL rechaza el seguimiento -> rollback",
                msg8b != null && ticketDAO.contar(null, null) == t1 && contarHuerfanos() == 0,
                "tickets " + t1 + "->" + ticketDAO.contar(null, null) + " | huérfanos " + contarHuerfanos());

        // 9. JOIN de listado
        List<TicketDetalleDTO> pagina1 = ticketDAO.listarDetalle(null, null, 10, 0);
        TicketDetalleDTO primero = pagina1.get(0);
        verificar("9", "JOIN de listado con datos descriptivos",
                primero.getCliente() != null && primero.getCategoria() != null && primero.getEquipo() != null,
                "#" + primero.getIdTicket() + " | " + primero.getCliente() + " | " + primero.getEquipo()
                + " | " + primero.getCategoria() + " | " + primero.getTecnico());

        // 10. Filtro por estado
        List<TicketDetalleDTO> abiertos = ticketDAO.listarDetalle("ABIERTO", null, 100, 0);
        verificar("10", "Filtro por estado",
                !abiertos.isEmpty() && abiertos.stream().allMatch(t -> "ABIERTO".equals(t.getEstado())),
                abiertos.size() + " ticket(s), todos ABIERTO");

        // 11. Filtro por prioridad
        List<TicketDetalleDTO> criticos = ticketDAO.listarDetalle(null, "CRITICA", 100, 0);
        verificar("11", "Filtro por prioridad",
                !criticos.isEmpty() && criticos.stream().allMatch(t -> "CRITICA".equals(t.getPrioridad())),
                criticos.size() + " ticket(s), todos CRITICA");

        // 12. Paginación
        int total = ticketDAO.contar(null, null);
        List<TicketDetalleDTO> pagina2 = ticketDAO.listarDetalle(null, null, 10, 10);
        verificar("12", "Paginación (LIMIT 10)",
                total >= 15 && pagina1.size() == 10 && pagina2.size() == Math.min(10, total - 10),
                "total " + total + " | página 1: " + pagina1.size() + " | página 2: " + pagina2.size());

        // 13. Consulta parametrizada
        int inyeccion = ticketDAO.contar("ABIERTO' OR '1'='1", null);
        verificar("13", "Consulta parametrizada", inyeccion == 0,
                "estado = \"ABIERTO' OR '1'='1\" -> " + inyeccion + " filas (se trata como valor)");

        // 14. Actualización con estado esperado (concurrencia)
        service.cambiarEstado(id, "ABIERTO", "EN_PROCESO", "Primera ventana");
        String msg14 = null;
        try {
            service.cambiarEstado(id, "ABIERTO", "ASIGNADO", "Segunda ventana con datos viejos");
        } catch (ServiceException ex) {
            msg14 = ex.getMessage();
        }
        verificar("14", "Actualización con estado esperado",
                msg14 != null && msg14.contains("modificado por otra operación")
                && "EN_PROCESO".equals(ticketDAO.buscarDetalle(id).get().getEstado()),
                msg14);

        // Ejercicio 3: transición no permitida
        service.cambiarEstado(id, "EN_PROCESO", "CERRADO", "Solucionado");
        String msgE3 = null;
        try {
            service.cambiarEstado(id, "CERRADO", "ABIERTO", "Intento de reabrir");
        } catch (ServiceException ex) {
            msgE3 = ex.getMessage();
        }
        verificar("E3", "CERRADO no vuelve a ABIERTO", msgE3 != null, msgE3);

        // Ejercicio 2: historial
        List<Seguimiento> historial = seguimientoDAO.listarPorTicket(id);
        verificar("E2", "Historial del ticket", historial.size() == 3,
                historial.size() + " seguimiento(s): registro inicial + 2 cambios de estado");

        // 15. Integridad: relaciones inexistentes, inactivas o incoherentes
        esperarRechazo("15a", "Cliente inexistente", service,
                ticket(999999L, eqAna, maria, soft, "Cliente inexistente", "MEDIA"), "x", "cliente");
        esperarRechazo("15b", "Categoría deshabilitada", service,
                ticket(ana, eqAna, maria, retirada, "Categoría retirada", "MEDIA"), "x", "categoría");
        esperarRechazo("15c", "CRITICA con técnico de nivel 2", service,
                ticket(ana, eqAna, maria, soft, "Servidor caído", "CRITICA"), "x", "nivel 3");
        verificar("15", "Sin referencias huérfanas en la base", contarHuerfanos() == 0 && contarReferenciasRotas() == 0,
                "tickets sin seguimiento = " + contarHuerfanos() + " | referencias rotas = " + contarReferenciasRotas());

        // Ejercicio 1: productividad
        List<ProductividadDTO> prod = ticketDAO.productividadPorTecnico();
        boolean ordenado = true;
        for (int i = 1; i < prod.size(); i++) {
            ordenado &= prod.get(i - 1).getTotal() >= prod.get(i).getTotal();
        }
        verificar("E1", "Productividad por técnico y estado", !prod.isEmpty() && ordenado,
                prod.get(0).getTecnico() + " / " + prod.get(0).getEstado() + " = " + prod.get(0).getTotal()
                + " (primera fila, orden descendente)");

        // Proyecto: CRUD de equipos relacionado con clientes
        Equipo nuevo = new Equipo(null, carlos, "EQ-PRUEBA-" + (System.currentTimeMillis() % 100000),
                "Tablet", "Samsung", "Tab A9", true);
        long idEq = equipoDAO.insertar(nuevo);
        boolean dup = equipoDAO.existeCodigo(nuevo.getCodigoInventario().toLowerCase(), null);
        int ticketsEq1 = equipoDAO.contarTickets(eqAna);
        boolean eliminado = equipoDAO.eliminar(idEq);
        verificar("EQ", "CRUD de equipos relacionado",
                idEq > 0 && dup && ticketsEq1 > 0 && eliminado,
                "alta id " + idEq + " | código duplicado detectado | EQ-0001 tiene " + ticketsEq1
                + " ticket(s), no se elimina | equipo sin tickets eliminado");

        System.out.println();
        System.out.println(fallidas == 0
                ? "RESULTADO: todas las pruebas del proceso de tickets pasaron."
                : "RESULTADO: " + fallidas + " prueba(s) fallaron.");
        if (fallidas > 0) {
            System.exit(1);
        }
    }

    // ------------------------------------------------------- auxiliares

    private static Ticket ticket(long cliente, long equipo, long tecnico, long categoria,
            String titulo, String prioridad) {
        Ticket t = new Ticket();
        t.setIdCliente(cliente);
        t.setIdEquipo(equipo);
        t.setIdTecnico(tecnico);
        t.setIdCategoria(categoria);
        t.setTitulo(titulo);
        t.setDescripcion("Descripción de prueba de la Guía 7.");
        t.setPrioridad(prioridad);
        return t;
    }

    private static void esperarRechazo(String n, String prueba, TicketService service,
            Ticket t, String detalle, String fragmento) throws SQLException {
        int antes = new TicketDAOImpl().contar(null, null);
        String msg = null;
        try {
            service.registrarTicketConSeguimiento(t, detalle);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
        }
        boolean ok = msg != null && msg.toLowerCase().contains(fragmento.toLowerCase())
                && new TicketDAOImpl().contar(null, null) == antes;
        verificar(n, prueba, ok, msg == null ? "Se registró un ticket inválido" : msg);
    }

    // Consultas de verificación (solo para este programa de pruebas).
    private static long idPorCampo(String tabla, String id, String campo, String valor) throws SQLException {
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(
                        "SELECT " + id + " FROM " + tabla + " WHERE " + campo + " = ?")) {
            ps.setString(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("Falta el dato de prueba " + valor
                            + ": ejecute sql/02 y sql/06.");
                }
                return rs.getLong(1);
            }
        }
    }

    private static int escalar(String sql) throws SQLException {
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static int contarSeguimientos() throws SQLException {
        return escalar("SELECT COUNT(*) FROM seguimientos");
    }

    private static long maxIdTicket() throws SQLException {
        return escalar("SELECT COALESCE(MAX(id_ticket), 0) FROM tickets");
    }

    private static int contarHuerfanos() throws SQLException {
        return escalar("SELECT COUNT(*) FROM tickets t LEFT JOIN seguimientos s "
                + "ON s.id_ticket = t.id_ticket WHERE s.id_seguimiento IS NULL");
    }

    /** Tickets cuyo equipo no pertenece a su cliente. */
    private static int contarReferenciasRotas() throws SQLException {
        return escalar("SELECT COUNT(*) FROM tickets t JOIN equipos e ON e.id_equipo = t.id_equipo "
                + "WHERE e.id_cliente <> t.id_cliente");
    }

    private static void verificar(String n, String prueba, boolean ok, String detalle) {
        if (!ok) {
            fallidas++;
        }
        System.out.printf("[%s] %-4s %-42s | %s%n", ok ? "PASA " : "FALLA", n, prueba, detalle);
    }
}
